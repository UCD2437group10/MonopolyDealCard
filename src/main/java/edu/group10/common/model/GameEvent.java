package edu.group10.common.model;

import edu.group10.common.enums.EventType;
import java.util.HashMap;
import java.util.Map;

public class GameEvent {
    private String eventId;
    private EventType type;
    private String description; //e.g. A played "Sly Deal" to B
    private String sourcePlayerId;
    private String targetPlayerId;
    private Map<String, Object> data; //Extra data
    // (e.g. {"cardId": "sly_deal_001", "propertyId": "red_estate"})
    private long timestamp;

    public GameEvent() {
        this.data = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    public GameEvent(EventType type, String description) {
        this();
        this.type = type;
        this.description = description;
    }

    //Factory methods

    public static GameEvent cardPlayed(String playerId, String cardName, String targetId) {
        GameEvent event = new GameEvent(EventType.CARD_PLAYED,
                String.format("%s 打出了 %s", playerId, cardName));
        event.setSourcePlayerId(playerId);
        event.setTargetPlayerId(targetId);
        event.putData("cardName", cardName);
        return event;
    }

    public static GameEvent propertyPlayed(String playerId, String propertyId) {
        GameEvent event = new GameEvent(EventType.CARD_PLAYED,
                String.format("%s 打出了物业", playerId));
        event.setSourcePlayerId(playerId);
        event.putData("propertyId", propertyId);
        return event;
    }

    public static GameEvent moneyPlayed(String playerId, int value) {
        GameEvent event = new GameEvent(EventType.MONEY_CHANGED,
                String.format("%s 打出了 %dM 钱卡", playerId, value));
        event.setSourcePlayerId(playerId);
        event.putData("amount", value);
        event.putData("action", "play");
        return event;
    }

    public static GameEvent cardDrawn(String playerId, String cardId) {
        GameEvent event = new GameEvent(EventType.CARD_DRAWN,
                String.format("%s 摸了一张牌", playerId));
        event.setSourcePlayerId(playerId);
        event.putData("cardId", cardId);
        return event;
    }

    public static GameEvent cardDiscarded(String playerId, String cardId) {
        GameEvent event = new GameEvent(EventType.CARD_PLAYED,
                String.format("%s 弃了一张牌", playerId));
        event.setSourcePlayerId(playerId);
        event.putData("cardId", cardId);
        return event;
    }

    public static GameEvent moneyTransferred(String fromId, String toId, int amount) {
        GameEvent event = new GameEvent(EventType.MONEY_CHANGED,
                String.format("%s 向 %s 支付了 %dM", fromId, toId, amount));
        event.setSourcePlayerId(fromId);
        event.setTargetPlayerId(toId);
        event.putData("amount", amount);
        event.putData("action", "transfer");
        return event;
    }

    public static GameEvent moneyChanged(String playerId, int amount, String action) {
        String actionText = "add".equals(action) ? "增加了" : "减少了";
        GameEvent event = new GameEvent(EventType.MONEY_CHANGED,
                String.format("%s %s %dM", playerId, actionText, amount));
        event.setSourcePlayerId(playerId);
        event.putData("amount", amount);
        event.putData("action", action);
        return event;
    }

    public static GameEvent propertyTransferred(String fromId, String toId, String propertyId) {
        GameEvent event = new GameEvent(EventType.PROPERTY_TRANSFERRED,
                String.format("%s 的物业被转移给了 %s", fromId, toId));
        event.setSourcePlayerId(fromId);
        event.setTargetPlayerId(toId);
        event.putData("propertyId", propertyId);
        return event;
    }

    public static GameEvent propertyRemoved(String playerId, String propertyId) {
        GameEvent event = new GameEvent(EventType.PROPERTY_TRANSFERRED,
                String.format("%s 失去了一个物业", playerId));
        event.setSourcePlayerId(playerId);
        event.putData("propertyId", propertyId);
        event.putData("action", "remove");
        return event;
    }

    public static GameEvent propertySwapped(String player1Id, String player2Id,
                                            String propertyId1, String propertyId2) {
        GameEvent event = new GameEvent(EventType.PROPERTY_TRANSFERRED,
                String.format("%s 和 %s 交换了物业", player1Id, player2Id));
        event.setSourcePlayerId(player1Id);
        event.setTargetPlayerId(player2Id);
        event.putData("propertyId1", propertyId1);
        event.putData("propertyId2", propertyId2);
        event.putData("action", "swap");
        return event;
    }

    public static GameEvent turnEnded(String oldPlayerId, String newPlayerId) {
        GameEvent event = new GameEvent(EventType.TURN_CHANGED,
                String.format("%s 结束了回合，现在轮到 %s", oldPlayerId, newPlayerId));
        event.setSourcePlayerId(oldPlayerId);
        event.setTargetPlayerId(newPlayerId);
        event.putData("newPlayerId", newPlayerId);
        return event;
    }

    public static GameEvent gameOver(String winnerId) {
        GameEvent event = new GameEvent(EventType.GAME_OVER,
                String.format("游戏结束，胜者: %s", winnerId));
        event.setSourcePlayerId(winnerId);
        event.putData("winnerId", winnerId);
        return event;
    }

    public static GameEvent prompt(String message, String targetPlayerId) {
        GameEvent event = new GameEvent(EventType.ACTION_REQUIRED, message);
        event.setTargetPlayerId(targetPlayerId);
        event.putData("promptType", "action_required");
        return event;
    }

    public static GameEvent warning(String message) {
        return new GameEvent(EventType.ACTION_REQUIRED, message);
    }

    public static GameEvent confirm(String message) {
        GameEvent event = new GameEvent(EventType.CONFIRM, message);
        event.putData("confirmed", true);
        return event;
    }

    public static GameEvent cancel(String message) {
        GameEvent event = new GameEvent(EventType.CANCEL, message);
        event.putData("cancelled", true);
        return event;
    }

    public static GameEvent setCompleted(String playerId, String color, int count) {
        GameEvent event = new GameEvent(EventType.SET_COMPLETED,
                String.format("%s 完成了 %s 全套物业（共%d张）", playerId, color, count));
        event.setSourcePlayerId(playerId);
        event.putData("color", color);
        event.putData("count", count);
        return event;
    }

    public static GameEvent timeout(String playerId) {
        GameEvent event = new GameEvent(EventType.TURN_TIMEOUT,
                String.format("%s 回合超时", playerId));
        event.setSourcePlayerId(playerId);
        return event;
    }


    //Add extra data
    public GameEvent putData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    //Getters and setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSourcePlayerId() { return sourcePlayerId; }
    public void setSourcePlayerId(String sourcePlayerId) { this.sourcePlayerId = sourcePlayerId; }

    public String getTargetPlayerId() { return targetPlayerId; }
    public void setTargetPlayerId(String targetPlayerId) { this.targetPlayerId = targetPlayerId; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "GameEvent{" +
                "type=" + type +
                ", description='" + description + '\'' +
                ", data=" + data +
                '}';
    }
}
