package edu.group10.transport;

import com.fasterxml.jackson.databind.JsonNode;
import edu.group10.common.model.*;
import edu.group10.core.GameEngineImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 消息路由器
 *
 * 职责：这是 WebSocket 消息处理的核心调度层。
 * 它接收客户端发来的 GameMessage（信封），根据 type 字段分派到对应的处理方法，
 * 然后调用 GameEngineImpl 执行业务逻辑，最后将结果发回给客户端。
 *
 * 消息流转过程（以 PLAYER_ACTION 为例）：
 *
 *   客户端                      MessageRouter                   GameEngineImpl
 *   ────                        ────────                       ────────
 *   JSON {"type":"PLAYER_ACTION",
 *         "payload":{...}}  ──→
 *                              1. decodeMessage() 解析 GameMessage
 *                              2. 判断 type == "PLAYER_ACTION"
 *                              3. decodePlayerAction(payload) 解析 PlayerAction
 *                              4. routePlayerAction()  ──────→  executeAction(gameId, action)
 *                              5. encodeResult()  ←───────────  GameActionResult
 *                              6. 发回请求者  ──→  客户端
 *                              7. 广播给同房其他玩家  ──→  其他客户端
 *
 * 为什么需要 MessageRouter 而不直接在 GameWebSocketHandler 中写逻辑？
 * - 单一职责原则：Handler 只管 WebSocket 连接生命周期（onOpen/onClose），
 *   Router 管消息分发逻辑，两者分离便于维护和测试
 * - 方便未来扩展：新增消息类型只需在 Router 中添加一个路由分支
 */
public class MessageRouter {

    private static final Logger logger = Logger.getLogger(MessageRouter.class.getName());

    /** 游戏引擎 —— 所有业务逻辑的核心入口 */
    private final GameEngineImpl gameEngine;

    /** JSON 编解码器 */
    private final JsonCodec codec = JsonCodec.getInstance();

    /** 会话管理器 */
    private final SessionManager sessionManager;

    /**
     * WebSocket 会话仓库（从 GameWebSocketHandler 传入）
     *
     * Key = sessionId, Value = javax.websocket.Session
     * 这个 Map 由 GameWebSocketHandler 维护（@OnOpen 添加，@OnClose 移除），
     * MessageRouter 只读取和发送，不修改。
     */
    private final ConcurrentHashMap<String, javax.websocket.Session> sessionMap;

    /**
     * @param gameEngine     游戏引擎实例
     * @param sessionManager 会话管理器
     * @param sessionMap     由 GameWebSocketHandler 共享的会话映射表
     */
    public MessageRouter(GameEngineImpl gameEngine,
                         SessionManager sessionManager,
                         ConcurrentHashMap<String, javax.websocket.Session> sessionMap) {
        this.gameEngine = gameEngine;
        this.sessionManager = sessionManager;
        this.sessionMap = sessionMap;
    }

    // ======================== 总入口 ========================

    /**
     * 路由入口 —— 所有从 WebSocket 收到的消息都从这里进入
     *
     * @param session     发送消息的 WebSocket 会话
     * @param rawJson     客户端发来的原始 JSON 字符串
     * @param responseSender 响应发送回调（用于把处理结果发回给客户端）
     */
    public void route(javax.websocket.Session session, String rawJson,
                      ResponseSender responseSender) {

        // 第一步：解析 JSON → GameMessage 信封
        GameMessage message = codec.decodeMessage(rawJson);
        if (message == null) {
            sendError(session, "PARSE_ERROR", "Invalid JSON format", responseSender);
            return;
        }

        String type = message.getType();
        String gameId = message.getGameId();
        String playerId = message.getPlayerId();
        JsonNode payload = message.getPayload();

        logger.info(String.format("[Router] Received: type=%s, gameId=%s, playerId=%s",
                type, gameId, playerId));

        // 第二步：根据消息类型分发
        try {
            switch (type) {
                case "CREATE_GAME":
                    handleCreateGame(session, playerId, payload, responseSender);
                    break;

                case "JOIN_GAME":
                    handleJoinGame(session, playerId, gameId, responseSender);
                    break;

                case "START_GAME":
                    handleStartGame(session, gameId, playerId, responseSender);
                    break;

                case "PLAYER_ACTION":
                    handlePlayerAction(session, gameId, playerId, payload, responseSender);
                    break;

                case "GET_STATE":
                    handleGetState(session, gameId, responseSender);
                    break;

                case "LEAVE_GAME":
                    handleLeaveGame(session, playerId, responseSender);
                    break;

                default:
                    sendError(session, "UNKNOWN_TYPE",
                            "Unknown message type: " + type, responseSender);
            }
        } catch (Exception e) {
            logger.severe("[Router] Error processing message: " + e.getMessage());
            e.printStackTrace();
            sendError(session, "INTERNAL_ERROR",
                    "Internal server error: " + e.getMessage(), responseSender);
        }
    }

    // ======================== 各消息类型的处理方法 ========================

    /**
     * 处理 CREATE_GAME 消息
     *
     * 客户端发来的 payload 格式：
     * {
     *   "playerIds": ["p1", "p2", "p3"],
     *   "playerNames": ["Alice", "Bob", "Charlie"]
     * }
     *
     * 处理流程：
     * 1. 解析 playerIds 和 playerNames
     * 2. 调用 GameEngineImpl.createGame()
     * 3. 将发起者加入房间
     * 4. 返回 gameId 给客户端
     */
    private void handleCreateGame(javax.websocket.Session session, String playerId,
                                  JsonNode payload, ResponseSender sender) {
        if (payload == null) {
            sendError(session, "MISSING_PAYLOAD",
                    "CREATE_GAME requires payload with playerIds and playerNames", sender);
            return;
        }

        // 解析玩家列表
        List<String> playerIds = new ArrayList<>();
        List<String> playerNames = new ArrayList<>();

        JsonNode idsNode = payload.get("playerIds");
        JsonNode namesNode = payload.get("playerNames");

        if (idsNode == null || namesNode == null ||
                !idsNode.isArray() || !namesNode.isArray()) {
            sendError(session, "INVALID_PAYLOAD",
                    "playerIds and playerNames must be arrays", sender);
            return;
        }

        for (JsonNode id : idsNode) {
            playerIds.add(id.asText());
        }
        for (JsonNode name : namesNode) {
            playerNames.add(name.asText());
        }

        // 生成游戏房间 ID
        String gameId = "game-" + System.currentTimeMillis();

        // 调用引擎创建游戏
        boolean created = gameEngine.createGame(gameId, playerIds, playerNames);
        if (!created) {
            sendError(session, "CREATE_FAILED",
                    "Failed to create game (player count must be 2-5, or game already exists)", sender);
            return;
        }

        // 将创建者注册并加入房间
        sessionManager.registerPlayer(session, playerId);
        sessionManager.joinGame(playerId, gameId);

        // 返回创建成功消息（包含 gameId，客户端需要记住它来邀请其他人）
        String response = "{\"type\":\"GAME_CREATED\",\"gameId\":\"" + gameId +
                "\",\"success\":true}";
        sender.send(session, response);

        logger.info("[Router] Game created: " + gameId + " by " + playerId);
    }

    /**
     * 处理 JOIN_GAME 消息
     *
     * 客户端发来：gameId（在信封中），playerId（在信封中）
     *
     * 处理流程：
     * 1. 验证游戏房间是否存在
     * 2. 将玩家注册到会话管理器
     * 3. 加入游戏房间
     * 4. 广播 PLAYER_JOINED 通知给房间内其他玩家
     */
    private void handleJoinGame(javax.websocket.Session session, String playerId,
                                String gameId, ResponseSender sender) {

        if (gameId == null || gameId.isEmpty()) {
            sendError(session, "MISSING_GAME_ID",
                    "JOIN_GAME requires gameId", sender);
            return;
        }

        // 验证游戏是否存在
        GameState state = gameEngine.getGameState(gameId);
        if (state == null) {
            sendError(session, "GAME_NOT_FOUND",
                    "Game not found: " + gameId, sender);
            return;
        }

        // 注册并加入
        sessionManager.registerPlayer(session, playerId);
        sessionManager.joinGame(playerId, gameId);

        // 回复加入成功
        String response = "{\"type\":\"JOINED_GAME\",\"gameId\":\"" + gameId +
                "\",\"playerId\":\"" + playerId + "\",\"success\":true}";
        sender.send(session, response);

        // 广播给房间内其他玩家：有新玩家加入
        broadcastToGame(gameId, playerId, "{\"type\":\"PLAYER_JOINED\"," +
                "\"gameId\":\"" + gameId + "\",\"playerId\":\"" + playerId + "\"}", sender);

        logger.info("[Router] " + playerId + " joined game " + gameId);
    }

    /**
     * 处理 START_GAME 消息
     *
     * 只有已在房间中的玩家才能发起开始游戏的请求。
     * 游戏开始后：洗牌、发牌、确定先手，然后把初始 GameState 广播给所有玩家。
     */
    private void handleStartGame(javax.websocket.Session session, String gameId,
                                 String playerId, ResponseSender sender) {

        if (gameId == null || gameId.isEmpty()) {
            sendError(session, "MISSING_GAME_ID",
                    "START_GAME requires gameId", sender);
            return;
        }

        // 验证玩家是否在该房间中
        if (!sessionManager.isPlayerInGame(playerId)) {
            sendError(session, "NOT_IN_GAME",
                    "You are not in this game room", sender);
            return;
        }

        // 启动游戏（洗牌发牌、确定先手）
        boolean started = gameEngine.startGame(gameId);
        if (!started) {
            sendError(session, "START_FAILED",
                    "Failed to start game (game may not exist or already started)", sender);
            return;
        }

        // 获取初始游戏状态
        GameState initialState = gameEngine.getGameState(gameId);
        String stateJson = codec.encodeState(initialState);

        // 广播 GAME_STARTED + 初始 GameState 给房间所有玩家
        String broadcastMsg = "{\"type\":\"GAME_STARTED\",\"gameId\":\"" + gameId +
                "\",\"state\":" + stateJson + "}";
        broadcastToGame(gameId, null, broadcastMsg, sender);

        logger.info("[Router] Game started: " + gameId);
    }

    /**
     * 处理 PLAYER_ACTION 消息（最核心的处理方法）
     *
     * 这是游戏过程中最频繁调用的方法。客户端每次操作（出牌、结束回合、摸牌等）
     * 都会发送此类型的消息。
     *
     * 处理流程：
     * 1. 将 payload 反序列化为 PlayerAction 对象
     * 2. 设置 gameId 和 playerId（由信封提供，而非 payload 内部）
     * 3. 调用 GameEngineImpl.executeAction() 执行操作
     * 4. 将 GameActionResult 发回请求者
     * 5. 如果操作成功，广播 STATE_UPDATE 给房间其他玩家
     */
    private void handlePlayerAction(javax.websocket.Session session, String gameId,
                                    String playerId, JsonNode payload,
                                    ResponseSender sender) {

        if (gameId == null || playerId == null) {
            sendError(session, "MISSING_FIELDS",
                    "PLAYER_ACTION requires gameId and playerId", sender);
            return;
        }

        // 解析 PlayerAction
        PlayerAction action = codec.decodePlayerAction(payload);
        if (action == null) {
            sendError(session, "INVALID_ACTION",
                    "Failed to parse player action from payload", sender);
            return;
        }

        // 用信封中的 gameId/playerId 覆盖（信封是权威来源，payload 中的可能不准确）
        action.setGameId(gameId);
        action.setPlayerId(playerId);

        // 调用引擎执行业务逻辑
        GameActionResult result = gameEngine.executeAction(gameId, action);
        String resultJson = codec.encodeResult(result);

        // 发回操作结果给请求者
        String response = "{\"type\":\"ACTION_RESULT\",\"gameId\":\"" + gameId +
                "\",\"result\":" + resultJson + "}";
        sender.send(session, response);

        // 如果操作成功，广播状态更新给房间中其他玩家
        if (result.isSuccess() && result.getNewState() != null) {
            String stateJson = codec.encodeState(result.getNewState());
            String broadcastMsg = "{\"type\":\"STATE_UPDATE\",\"gameId\":\"" + gameId +
                    "\",\"state\":" + stateJson + "}";
            broadcastToGame(gameId, playerId, broadcastMsg, sender);
        }

        logger.info(String.format("[Router] Action processed: gameId=%s, playerId=%s, type=%s, success=%s",
                gameId, playerId, action.getType(), result.isSuccess()));
    }

    /**
     * 处理 GET_STATE 消息
     *
     * 使用场景：断线重连后，客户端需要获取最新的游戏状态来恢复 UI。
     *
     * 直接调用 gameEngine.getGameState() 获取当前状态并返回。
     */
    private void handleGetState(javax.websocket.Session session, String gameId,
                                ResponseSender sender) {

        if (gameId == null || gameId.isEmpty()) {
            sendError(session, "MISSING_GAME_ID",
                    "GET_STATE requires gameId", sender);
            return;
        }

        GameState state = gameEngine.getGameState(gameId);
        if (state == null) {
            sendError(session, "GAME_NOT_FOUND",
                    "Game not found: " + gameId, sender);
            return;
        }

        String stateJson = codec.encodeState(state);
        String response = "{\"type\":\"GAME_STATE\",\"gameId\":\"" + gameId +
                "\",\"state\":" + stateJson + "}";
        sender.send(session, response);
    }

    /**
     * 处理 LEAVE_GAME 消息
     *
     * 将玩家从房间中移除，并广播 PLAYER_LEFT 给其他玩家。
     */
    private void handleLeaveGame(javax.websocket.Session session, String playerId,
                                 ResponseSender sender) {

        String gameId = sessionManager.getGameIdByPlayer(playerId);
        sessionManager.leaveGame(playerId);

        String response = "{\"type\":\"LEFT_GAME\",\"success\":true}";
        sender.send(session, response);

        // 广播离开消息
        if (gameId != null) {
            broadcastToGame(gameId, playerId,
                    "{\"type\":\"PLAYER_LEFT\",\"gameId\":\"" + gameId +
                            "\",\"playerId\":\"" + playerId + "\"}", sender);
        }

        logger.info("[Router] " + playerId + " left game " + gameId);
    }

    // ======================== 辅助方法 ========================

    /**
     * 向游戏房间中所有其他玩家广播消息
     *
     * @param gameId        目标游戏房间
     * @param excludePlayer 排除的玩家 ID（不向此玩家发送，通常是操作者本人）
     * @param message       要发送的 JSON 消息字符串
     * @param sender        响应发送器
     */
    private void broadcastToGame(String gameId, String excludePlayer,
                                 String message, ResponseSender sender) {
        Set<String> playerIds = sessionManager.getPlayersInGame(gameId);
        for (String pid : playerIds) {
            // 不发给被排除的玩家（操作者本人）
            if (excludePlayer != null && pid.equals(excludePlayer)) {
                continue;
            }
            // 通过 sessionMap 找到玩家的 WebSocket 连接
            javax.websocket.Session targetSession = findSessionByPlayerId(pid);
            if (targetSession != null && targetSession.isOpen()) {
                sender.send(targetSession, message);
            }
        }
    }

    /**
     * 通过 playerId 查找 WebSocket 会话
     *
     * 遍历 sessionMap 查找，因为 SessionManager 不持有 Session 引用。
     * sessionMap 中的 key 是 sessionId，value 是 Session。
     * 我们需要先通过 sessionManager 找到 sessionId，再从 sessionMap 中取 Session。
     */
    private javax.websocket.Session findSessionByPlayerId(String playerId) {
        for (java.util.Map.Entry<String, javax.websocket.Session> entry : sessionMap.entrySet()) {
            javax.websocket.Session sess = entry.getValue();
            String sid = sessionManager.getPlayerIdBySessionId(entry.getKey());
            if (playerId.equals(sid)) {
                return sess;
            }
        }
        // 降级方案：直接从 sessionToPlayer 反向查
        for (java.util.Map.Entry<String, javax.websocket.Session> entry : sessionMap.entrySet()) {
            javax.websocket.Session sess = entry.getValue();
            String foundPlayerId = sessionManager.getPlayerIdBySession(sess);
            if (playerId.equals(foundPlayerId)) {
                return sess;
            }
        }
        return null;
    }

    /**
     * 向指定会话发送错误消息
     */
    private void sendError(javax.websocket.Session session, String errorCode,
                           String errorMessage, ResponseSender sender) {
        String errorJson = "{\"type\":\"ERROR\",\"success\":false," +
                "\"errorCode\":\"" + errorCode + "\"," +
                "\"errorMessage\":\"" + escapeJson(errorMessage) + "\"}";
        sender.send(session, errorJson);
        logger.warning("[Router] Error sent to session " + session.getId() +
                ": [" + errorCode + "] " + errorMessage);
    }

    /**
     * 简单的 JSON 字符串转义
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    // ======================== 回调接口 ========================

    /**
     * 响应发送器（函数式接口）
     *
     * 把"如何发送 WebSocket 消息"的细节从 MessageRouter 中解耦出来。
     * 这样 MessageRouter 不需要知道 javax.websocket.Session 的 getBasicRemote() 细节。
     *
     * 实际实现在 GameWebSocketHandler 中，就是简单的：
     * session.getBasicRemote().sendText(message)
     */
    @FunctionalInterface
    public interface ResponseSender {
        /**
         * 向指定会话发送文本消息
         *
         * @param session 目标 WebSocket 会话
         * @param message 要发送的 JSON 字符串
         */
        void send(javax.websocket.Session session, String message);
    }
}