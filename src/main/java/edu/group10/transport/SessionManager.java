package edu.group10.transport;

import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话管理器
 *
 * 职责：维护"谁在哪个游戏房间"以及"谁连着哪个 WebSocket 连接"的映射关系。
 *
 * 为什么需要这个组件？
 * 1. 广播消息：当一个玩家做出操作后，需要通知同房间的其他玩家更新状态
 * 2. 断线重连：玩家断开后重新连接，需要能找回之前的游戏状态
 * 3. 玩家列表：前端需要展示当前房间有哪些人在线
 *
 * 核心数据结构（两个双向映射）：
 *
 *   sessionId ──────────> playerId         （通过 session 找玩家）
 *        │                    │
 *        └──── gameId <──────┘             （通过玩家或 session 找游戏房间）
 *
 * 线程安全：使用 ConcurrentHashMap，因为 WebSocket 的 onOpen/onClose/onMessage
 * 可能由 Jetty 的不同线程调用，需要保证并发安全。
 */
public class SessionManager {

    // ======================== 核心映射表 ========================

    /** sessionId → playerId：通过 WebSocket 会话找到对应的玩家 */
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();

    /** playerId → sessionId：通过玩家找到对应的 WebSocket 会话 */
    private final Map<String, String> playerToSession = new ConcurrentHashMap<>();

    /** playerId → gameId：通过玩家找到其所在的游戏房间 */
    private final Map<String, String> playerToGame = new ConcurrentHashMap<>();

    /** gameId → playerId 集合：一个游戏房间中有哪些玩家 */
    private final Map<String, Set<String>> gameToPlayers = new ConcurrentHashMap<>();

    /** sessionId → playerId 的额外映射（用于在玩家未加入游戏前也能找到） */
    private final Map<String, String> sessionToPendingPlayer = new ConcurrentHashMap<>();

    // ======================== 玩家-会话 绑定 ========================

    /**
     * 注册 WebSocket 连接与玩家的绑定关系
     *
     * 调用时机：玩家发送 JOIN_GAME 消息后，或 CREATE_GAME 成功后
     *
     * @param session  WebSocket 会话对象
     * @param playerId 玩家 ID
     */
    public void registerPlayer(Session session, String playerId) {
        String sessionId = session.getId();

        // 如果该玩家之前有其他连接，先解绑旧连接
        String oldSessionId = playerToSession.get(playerId);
        if (oldSessionId != null && !oldSessionId.equals(sessionId)) {
            sessionToPlayer.remove(oldSessionId);
        }

        sessionToPlayer.put(sessionId, playerId);
        playerToSession.put(playerId, sessionId);
    }

    /**
     * 将玩家加入游戏房间
     *
     * 调用时机：CREATE_GAME 或 JOIN_GAME 成功后
     *
     * @param playerId 玩家 ID
     * @param gameId   游戏房间 ID
     */
    public void joinGame(String playerId, String gameId) {
        playerToGame.put(playerId, gameId);
        gameToPlayers.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(playerId);
    }

    /**
     * 将玩家移出游戏房间（不关闭 WebSocket 连接）
     *
     * 调用时机：玩家主动离开游戏，或游戏结束清理
     *
     * @param playerId 玩家 ID
     */
    public void leaveGame(String playerId) {
        String gameId = playerToGame.remove(playerId);
        if (gameId != null) {
            Set<String> players = gameToPlayers.get(gameId);
            if (players != null) {
                players.remove(playerId);
                // 如果房间空了，清理房间记录
                if (players.isEmpty()) {
                    gameToPlayers.remove(gameId);
                }
            }
        }
    }

    /**
     * 处理 WebSocket 连接断开
     *
     * 调用时机：onClose 事件触发时
     *
     * 注意：断开连接时只解绑 session ↔ player 的关系，
     * 不把玩家移出游戏房间 —— 这样玩家可以重连回到同一局游戏。
     *
     * @param session 断开的 WebSocket 会话
     */
    public void onDisconnect(Session session) {
        String sessionId = session.getId();
        String playerId = sessionToPlayer.remove(sessionId);
        sessionToPendingPlayer.remove(sessionId);

        if (playerId != null) {
            // 不移除 gameToPlayers 中的记录，保留重连的可能
            // 只移除 playerToSession，因为该连接已断
            playerToSession.remove(playerId);
        }
    }

    /**
     * 处理玩家重连（新的 WebSocket 连接绑定到已有的玩家）
     *
     * @param session  新的 WebSocket 会话
     * @param playerId 重连的玩家 ID
     * @return 玩家之前所在的游戏房间 ID，如果找不到返回 null
     */
    public String onReconnect(Session session, String playerId) {
        registerPlayer(session, playerId);
        return playerToGame.get(playerId);  // 返回之前的游戏房间
    }

    // ======================== 查询方法 ========================

    /**
     * 通过 WebSocket 会话查找玩家 ID
     */
    public String getPlayerIdBySession(Session session) {
        return sessionToPlayer.get(session.getId());
    }

    /**
     * 通过 WebSocket 会话 ID 查找玩家 ID
     */
    public String getPlayerIdBySessionId(String sessionId) {
        return sessionToPlayer.get(sessionId);
    }

    /**
     * 通过玩家 ID 查找 WebSocket 会话
     */
    public Session getSessionByPlayerId(String playerId) {
        // Session 对象由 Jetty 管理，这里只返回已注册的
        // 实际使用时需要配合 GameWebSocketHandler 中维护的 sessionMap
        return null; // 由外部 sessionMap 提供查询
    }

    /**
     * 通过玩家 ID 查找其所在的游戏房间
     */
    public String getGameIdByPlayer(String playerId) {
        return playerToGame.get(playerId);
    }

    /**
     * 通过 WebSocket 会话查找游戏房间
     */
    public String getGameIdBySession(Session session) {
        String playerId = sessionToPlayer.get(session.getId());
        if (playerId == null) {
            return null;
        }
        return playerToGame.get(playerId);
    }

    /**
     * 获取某游戏房间中所有玩家的 ID 列表
     *
     * 用于广播消息 —— MessageRouter 拿到此列表后，
     * 遍历每个玩家，通过其 WebSocket 会话发送消息。
     *
     * @param gameId 游戏房间 ID
     * @return 该房间中所有玩家的 ID 集合（不可修改的副本）
     */
    public Set<String> getPlayersInGame(String gameId) {
        Set<String> players = gameToPlayers.get(gameId);
        if (players == null) {
            return Collections.emptySet();
        }
        // 返回副本，防止外部修改内部集合
        return new HashSet<>(players);
    }

    /**
     * 获取游戏房间的玩家数量
     */
    public int getPlayerCountInGame(String gameId) {
        Set<String> players = gameToPlayers.get(gameId);
        return players == null ? 0 : players.size();
    }

    /**
     * 检查玩家是否在某个游戏房间中
     */
    public boolean isPlayerInGame(String playerId) {
        return playerToGame.containsKey(playerId);
    }

    /**
     * 临时存储还未绑定 playerId 的会话
     * （在玩家发送第一个 JOIN_GAME 消息之前，session 需要先有个临时标识）
     */
    public void registerPendingSession(Session session) {
        sessionToPendingPlayer.put(session.getId(), null);
    }

    /**
     * 移除所有与指定游戏房间相关的数据
     * 调用时机：游戏结束后清理
     */
    public void cleanupGame(String gameId) {
        Set<String> players = gameToPlayers.remove(gameId);
        if (players != null) {
            for (String playerId : players) {
                playerToGame.remove(playerId);
                String sessionId = playerToSession.remove(playerId);
                if (sessionId != null) {
                    sessionToPlayer.remove(sessionId);
                }
            }
        }
    }

    // ======================== 调试方法 ========================

    /**
     * 打印当前所有映射关系的快照（调试用）
     */
    public String dumpState() {
        return "SessionManager{" +
                "sessionToPlayer=" + sessionToPlayer +
                ", playerToSession=" + playerToSession +
                ", playerToGame=" + playerToGame +
                ", gameToPlayers=" + gameToPlayers +
                '}';
    }
}