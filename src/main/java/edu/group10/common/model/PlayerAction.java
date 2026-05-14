package edu.group10.common.model;

import edu.group10.common.enums.ActionType;
import edu.group10.common.enums.PropertyColor;

import java.util.ArrayList;
import java.util.List;

public class PlayerAction {
    private String gameId;
    private String playerId;
    private ActionType type;
    private String cardId; //ID of currently played card
    private String targetPlayerId; //ID of target player (when choosing a player)
    private List<String> selectedCardIds; //Multiple-selected scenes (discarding cards, paying)
    private String selectedPropertyId; //ID of selected property (steel/exchange)
    private PropertyColor selectedColor; //Selected color for dual-color and wild property cards
    private long timestamp;
    private int actionSequence; //Sequence number of action (avoid out-of-sequence)
    private Integer rentAmount;

    public PlayerAction() {
        this.selectedCardIds = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }

    //Getters and setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public ActionType getType() { return type; }
    public void setType(ActionType type) { this.type = type; }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public String getTargetPlayerId() { return targetPlayerId; }
    public void setTargetPlayerId(String targetPlayerId) { this.targetPlayerId = targetPlayerId; }

    public List<String> getSelectedCardIds() { return selectedCardIds; }
    public void setSelectedCardIds(List<String> selectedCardIds) { this.selectedCardIds = selectedCardIds; }

    public String getSelectedPropertyId() { return selectedPropertyId; }
    public void setSelectedPropertyId(String selectedPropertyId) { this.selectedPropertyId = selectedPropertyId; }

    public PropertyColor getSelectedColor() { return selectedColor; }
    public void setSelectedColor(PropertyColor selectedColor) { this.selectedColor = selectedColor; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getActionSequence() { return actionSequence; }
    public void setActionSequence(int actionSequence) { this.actionSequence = actionSequence; }

    public Integer getRentAmount() { return rentAmount; }
    public void setRentAmount(Integer rentAmount) { this.rentAmount = rentAmount; }

    @Override
    public String toString() {
        return "PlayerAction{" +
                "gameId='" + gameId + '\'' +
                ", playerId='" + playerId + '\'' +
                ", type=" + type +
                ", cardId='" + cardId + '\'' +
                ", targetPlayerId='" + targetPlayerId + '\'' +
                ", selectedCardIds=" + selectedCardIds +
                ", selectedPropertyId='" + selectedPropertyId + '\'' +
                ", selectedColor=" + selectedColor +
                '}';
    }
}
