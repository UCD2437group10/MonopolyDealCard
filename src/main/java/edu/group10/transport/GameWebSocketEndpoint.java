package edu.group10.transport;

import edu.group10.core.GameEngineImpl;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * WebSocket 服务端点（JSR 356 标准注解方式）
 *
 * 这是 WebSocket 通信的"接线员"——负责管理每个 WebSocket 连接的生命周期。
 *
 * @ServerEndpoint("/game") 的含义：
 * 客户端通过 ws://localhost:8080/game 连接到这个端点。
 * 每个客户端连接都会创建一个新的 GameWebSocketEndpoint 实例（注意：不是单例！）。
 * Jetty 会为每个 WebSocket 连接自动 new 一个本类对象。
 *
 * 生命周期方法（全部由 Jetty 在对应事件发生时自动回调）：
 *
 *   @OnOpen  →  连接建立时触发    （客户端连上了）
 *   @OnMessage → 收到消息时触发   （客户端发来了 JSON）
 *   @OnClose →  连接关闭时触发    （客户端断开或网络中断）
 *   @OnError →  发生错误时触发    （传输异常）
 *
 * JSR 356 (javax.websocket) 是 Java EE 的标准 WebSocket API。
 * 它的好处是：换一个 WebSocket 实现（如 Tomcat、Undertow），代码几乎不用改。
 *
 * 因为每个连接创建一个实例，所以静态成员在所有实例间共享：
 * - sessionMap：所有在线连接的集合（static）
 * - gameEngine、router、sessionManager：业务处理组件（static，全局唯一）
 */
@ServerEndpoint("/game")
public class GameWebSocketEndpoint {

    private static final Logger logger = Logger.getLogger(GameWebSocketEndpoint.class.getName());

    // ======================== 静态成员（类级别，所有连接实例共享） ========================

    /**
     * 全局会话仓库 —— 存储所有活跃的 WebSocket 连接
     *
     * Key = sessionId (String)
     * Value = Session (javax.websocket.Session)
     *
     * 为什么用 ConcurrentHashMap？
     * 多个连接可能同时在 onClose / onMessage 中修改此 Map（Jetty 是多线程处理连接的），
     * ConcurrentHashMap 保证线程安全。
     *
     * 这个 Map 由本类维护（@OnOpen 添加，@OnClose 移除），
     * 被 MessageRouter 读取（用于查找目标玩家并广播消息）。
     */
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 游戏引擎 —— 核心业务逻辑入口
     * static：全局只有一个 GameEngineImpl 实例
     */
    private static GameEngineImpl gameEngine;

    /**
     * 消息路由器 —— 分发和处理所有收到的消息
     */
    private static MessageRouter router;

    /**
     * 会话管理器 —— 维护 玩家↔游戏房间 的映射
     */
    private static SessionManager sessionManager;

    /**
     * JSON 编解码器
     */
    private static final JsonCodec codec = JsonCodec.getInstance();

    /**
     * 初始化静态组件（由 GameServer 在启动时调用一次）
     *
     * 为什么不放在 static {} 块中？
     * 因为 gameEngine 需要外部创建并传入（可能有配置参数），
     * 所以改为显式初始化的方式，更灵活。
     *
     * @param engine 游戏引擎实例
     */
    public static void initialize(GameEngineImpl engine) {
        gameEngine = engine;
        sessionManager = new SessionManager();
        router = new MessageRouter(gameEngine, sessionManager, sessionMap);
        logger.info("[WebSocket] Endpoint initialized — GameEngine and MessageRouter ready");
    }

    // ======================== WebSocket 生命周期回调 ========================

    /**
     * 连接建立回调
     *
     * 当客户端 new WebSocket("ws://server/game") 成功连接后，
     * Jetty 会自动调用此方法。
     *
     * @param session WebSocket 会话对象，代表这个连接。包含了发送消息的方法：
     *                session.getBasicRemote().sendText(message)
     */
    @OnOpen
    public void onOpen(Session session) {
        // 将新连接加入全局会话仓库
        sessionMap.put(session.getId(), session);

        // 在 SessionManager 中注册（此时还未绑定 playerId，先占个位）
        sessionManager.registerPendingSession(session);

        logger.info("[WebSocket] Connection opened: " + session.getId() +
                " (total connections: " + sessionMap.size() + ")");

        // 发送欢迎消息（可选，让客户端确认连接成功）
        sendMessage(session, "{\"type\":\"CONNECTED\",\"sessionId\":\"" +
                session.getId() + "\",\"message\":\"Welcome to Monopoly Deal!\"}");
    }

    /**
     * 收到消息回调（最核心的回调方法）
     *
     * 当客户端通过 WebSocket 发送文本消息时，Jetty 自动调用此方法。
     *
     * 消息格式：JSON 字符串，对应 GameMessage 信封结构
     * 示例：{"type":"PLAYER_ACTION","gameId":"room-1","playerId":"p1","payload":{...}}
     *
     * @param session 发送此消息的 WebSocket 会话
     * @param message 消息内容（原始 JSON 字符串）
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        logger.info("[WebSocket] Message from " + session.getId() +
                ": " + (message.length() > 200 ? message.substring(0, 200) + "..." : message));

        // 将消息路由给 MessageRouter 处理
        // ResponseSender 是一个回调接口，在这里实现为 "通过 WebSocket 发送文本"
        if (router != null) {
            router.route(session, message, (targetSession, response) -> {
                sendMessage(targetSession, response);
            });
        } else {
            sendMessage(session, "{\"type\":\"ERROR\",\"success\":false," +
                    "\"errorMessage\":\"Server not ready yet\"}");
        }
    }

    /**
     * 连接关闭回调
     *
     * 触发场景：
     * 1. 客户端主动关闭连接（浏览器关闭 Tab、调用 websocket.close()）
     * 2. 网络中断（Jetty 检测到 TCP 连接断开）
     * 3. 服务器主动关闭连接
     *
     * @param session 关闭的 WebSocket 会话
     * @param reason  关闭原因（包含状态码和描述）
     */
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        String sessionId = session.getId();

        // 通知 SessionManager：该连接已断开
        sessionManager.onDisconnect(session);

        // 从全局会话仓库中移除
        sessionMap.remove(sessionId);

        logger.info("[WebSocket] Connection closed: " + sessionId +
                " — reason: " + reason.getReasonPhrase() +
                " (total connections: " + sessionMap.size() + ")");
    }

    /**
     * 错误回调
     *
     * 当 WebSocket 通信发生异常时触发（如消息格式错误、传输中断等）。
     * 注意：onError 之后通常会紧接着触发 onClose。
     *
     * @param session 发生错误的会话
     * @param error   异常对象
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.severe("[WebSocket] Error on session " + session.getId() +
                ": " + error.getMessage());
        error.printStackTrace();

        // 尝试发送错误消息给客户端（连接可能已断，所以忽略发送失败）
        try {
            session.getBasicRemote().sendText("{\"type\":\"ERROR\",\"success\":false," +
                    "\"errorMessage\":\"" + escapeJson(error.getMessage()) + "\"}");
        } catch (IOException e) {
            // 连接很可能已经断了，忽略此异常
        }
    }

    // ======================== 辅助方法 ========================

    /**
     * 向指定会话发送文本消息
     *
     * 封装了 session.getBasicRemote().sendText() 的异常处理，
     * 让调用方不需要关心 WebSocket 底层 API。
     *
     * getBasicRemote() vs getAsyncRemote()：
     * - getBasicRemote()：同步发送，简单可靠，适合游戏这种消息量不大的场景
     * - getAsyncRemote()：异步发送，适合高并发场景（如聊天室广播）
     * 这里用同步版本即可。
     *
     * @param session 目标 WebSocket 会话
     * @param message 要发送的消息（JSON 字符串）
     */
    private void sendMessage(Session session, String message) {
        if (session == null || !session.isOpen()) {
            logger.warning("[WebSocket] Cannot send — session is null or closed");
            return;
        }
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.severe("[WebSocket] Failed to send message to " +
                    session.getId() + ": " + e.getMessage());
        }
    }

    /**
     * JSON 字符串简单转义
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    // ======================== 公开的静态查询方法 ========================

    /**
     * 获取在线连接总数（调试/监控用）
     */
    public static int getConnectionCount() {
        return sessionMap.size();
    }
}