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
                String.format("%s plyed %s", playerId, cardName));
        event.setSourcePlayerId(playerId);
        event.setTargetPlayerId(targetId);
        event.putData("cardName", cardName);
        return event;
    }

    public static GameEvent propertyPlayed(String playerId, String propertyId) {
        GameEvent event = new GameEvent(EventType.CARD_PLAYED,
                String.format("%s played property", playerId));
        event.setSourcePlayerId(playerId);
        event.putData("propertyId", propertyId);
        return event;
    }

    public static GameEvent moneyPlayed(String playerId, int value) {
        GameEvent event = new GameEvent(EventType.MONEY_CHANGED,
                String.format("%s played %dM money card", playerId, value));
        event.setSourcePlayerId(playerId);
        event.putData("amount", value);
        event.putData("action", "play");
        return event;
    }

    public static GameEvent cardDrawn(String playerId, String cardId) {
        GameEvent event = new GameEvent(EventType.CARD_DRAWN,
                String.format("%s drew a card", playerId));
        event.setSourcePlayerId(playerId);
        event.putData("cardId", cardId);
        return event;
    }

    public static GameEvent cardDiscarded(String playerId, String cardId) {
        GameEvent event = new GameEvent(EventType.CARD_PLAYED,
                String.format("%s discarded a card", playerId));
        event.setSourcePlayerId(playerId);
        event.putData("cardId", cardId);
        return event;
    }

    public static GameEvent moneyTransferred(String fromId, String toId, int amount) {
        GameEvent event = new GameEvent(EventType.MONEY_CHANGED,
                String.format("%s payed %dM for %s", fromId, amount, toId));
        event.setSourcePlayerId(fromId);
        event.setTargetPlayerId(toId);
        event.putData("amount", amount);
        event.putData("action", "transfer");
        return event;
    }

    public static GameEvent moneyChanged(String playerId, int amount, String action) {
        String actionText = "add".equals(action) ? "increased" : "decreased";
        GameEvent event = new GameEvent(EventType.MONEY_CHANGED,
                String.format("%s %s %dM", playerId, actionText, amount));
        event.setSourcePlayerId(playerId);
        event.putData("amount", amount);
        event.putData("action", action);
        return event;
    }

    public static GameEvent propertyTransferred(String fromId, String toId, String propertyId) {
        GameEvent event = new GameEvent(EventType.PROPERTY_TRANSFERRED,
                String.format("property of %s was transferred to %s", fromId, toId));
        event.setSourcePlayerId(fromId);
        event.setTargetPlayerId(toId);
        event.putData("propertyId", propertyId);
        return event;
    }

    public static GameEvent propertyRemoved(String playerId, String propertyId) {
        GameEvent event = new GameEvent(EventType.PROPERTY_TRANSFERRED,
                String.format("%s lost a property", playerId));
        event.setSourcePlayerId(playerId);
        event.putData("propertyId", propertyId);
        event.putData("action", "remove");
        return event;
    }

    public static GameEvent propertySwapped(String player1Id, String player2Id,
                                            String propertyId1, String propertyId2) {
        GameEvent event = new GameEvent(EventType.PROPERTY_TRANSFERRED,
                String.format("%s and %s switched property", player1Id, player2Id));
        event.setSourcePlayerId(player1Id);
        event.setTargetPlayerId(player2Id);
        event.putData("propertyId1", propertyId1);
        event.putData("propertyId2", propertyId2);
        event.putData("action", "swap");
        return event;
    }

    public static GameEvent turnEnded(String oldPlayerId, String newPlayerId) {
        GameEvent event = new GameEvent(EventType.TURN_CHANGED,
                String.format("%s ended the turn, now is %s's turn", oldPlayerId, newPlayerId));
        event.setSourcePlayerId(oldPlayerId);
        event.setTargetPlayerId(newPlayerId);
        event.putData("newPlayerId", newPlayerId);
        return event;
    }

    public static GameEvent gameOver(String winnerId) {
        GameEvent event = new GameEvent(EventType.GAME_OVER,
                String.format("Game over, the winner is: %s", winnerId));
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
                String.format("%s completed %s property suit (totally %d cards)", playerId, color, count));
        event.setSourcePlayerId(playerId);
        event.putData("color", color);
        event.putData("count", count);
        return event;
    }

    public static GameEvent timeout(String playerId) {
        GameEvent event = new GameEvent(EventType.TURN_TIMEOUT,
                String.format("%s the turn is time out", playerId));
        event.setSourcePlayerId(playerId);
        return event;
    }

    public static GameEvent houseAdded(String playerId, String propertyId) {
        GameEvent event = new GameEvent(EventType.SET_COMPLETED,
                String.format("%s add a house on %s property", playerId, propertyId));
        event.setSourcePlayerId(playerId);
        event.putData("propertyId", propertyId);
        event.putData("type", "house");
        return event;
    }

    public static GameEvent hotelAdded(String playerId, String propertyId) {
        GameEvent event = new GameEvent(EventType.SET_COMPLETED,
                String.format("%s add a hotel on %s property", playerId, propertyId));
        event.setSourcePlayerId(playerId);
        event.putData("propertyId", propertyId);
        event.putData("type", "hotel");
        return event;
    }

    public static GameEvent cardStopped(String fromPlayerId, String targetCardOwnerId) {
        GameEvent event = new GameEvent(EventType.CARD_PLAYED,
                String.format("%s used Just Say No to cancel the card", fromPlayerId));
        event.setSourcePlayerId(fromPlayerId);
        event.setTargetPlayerId(targetCardOwnerId);
        event.putData("action", "cancelled");
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
