package edu.group10.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * WebSocket 通信的消息信封（Envelope Pattern）
 *
 * 所有通过 WebSocket 传输的 JSON 消息都使用这个统一格式包装。
 * 这样设计的好处：
 * 1. 客户端和服务器只需解析一种顶层结构
 * 2. type 字段让 MessageRouter 能快速判断路由目标
 * 3. payload 存放各类型消息的具体数据，灵活可变
 *
 * JSON 格式示例（客户端 → 服务器）：
 * {
 *   "type": "PLAYER_ACTION",
 *   "gameId": "room-001",
 *   "playerId": "player-1",
 *   "payload": {
 *     "type": "PLAY_CARD",
 *     "cardId": "sly_deal_1",
 *     "targetPlayerId": "player-2"
 *   }
 * }
 *
 * 消息类型（type 字段的取值）：
 * | type              | 说明                                 | payload 内容              |
 * |-------------------|--------------------------------------|---------------------------|
 * | CREATE_GAME       | 创建新游戏房间                        | { playerIds, playerNames }|
 * | JOIN_GAME         | 加入已有游戏房间                      | null                      |
 * | START_GAME        | 开始游戏（所有玩家就绪后由房主发起）    | null                      |
 * | PLAYER_ACTION     | 玩家操作（出牌、结束回合等）            | PlayerAction 对象的 JSON   |
 * | GET_STATE         | 查询当前游戏状态（断线重连时用）        | null                      |
 * | LEAVE_GAME        | 离开游戏房间                          | null                      |
 *
 * 消息类型（服务器 → 客户端）：
 * | type              | 说明                                 | payload 内容              |
 * |-------------------|--------------------------------------|---------------------------|
 * | ACTION_RESULT     | 操作结果反馈                          | GameActionResult 的 JSON   |
 * | STATE_UPDATE      | 游戏状态更新（广播给同房间所有玩家）    | GameState 的 JSON          |
 * | ERROR             | 错误消息                              | { code, message }         |
 * | GAME_STARTED      | 游戏已开始通知                        | GameState 的 JSON          |
 * | PLAYER_JOINED     | 有新玩家加入                          | { playerId, playerName }  |
 * | PLAYER_LEFT       | 有玩家离开                            | { playerId }              |
 */
@JsonIgnoreProperties(ignoreUnknown = true)  // 忽略未知字段，保证向前兼容
public class GameMessage {

    /** 消息类型：CREATE_GAME / JOIN_GAME / START_GAME / PLAYER_ACTION / GET_STATE 等 */
    private String type;

    /** 游戏房间 ID */
    private String gameId;

    /** 发送此消息的玩家 ID */
    private String playerId;

    /**
     * 消息负载 —— 根据 type 不同，存放不同的子对象
     * 使用 JsonNode 而非 Object 的原因是：
     * JsonNode 是 Jackson 的树模型，可以延迟解析（先读 type 再决定用什么类反序列化 payload）
     */
    private JsonNode payload;

    // ======================== 构造器 ========================

    public GameMessage() {}

    public GameMessage(String type, String gameId, String playerId, JsonNode payload) {
        this.type = type;
        this.gameId = gameId;
        this.playerId = playerId;
        this.payload = payload;
    }

    // ======================== Getters & Setters ========================

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "GameMessage{" +
                "type='" + type + '\'' +
                ", gameId='" + gameId + '\'' +
                ", playerId='" + playerId + '\'' +
                ", payload=" + payload +
                '}';
    }
}