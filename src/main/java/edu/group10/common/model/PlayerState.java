package edu.group10.common.model;

import edu.group10.common.enums.PlayerStatus;
import java.util.ArrayList;
import java.util.List;

public class PlayerState {
    private String playerId;
    private String playerName;
    private int money; //Money in bank area
    private List<String> propertyIds; //List of owned property IDs
    private int handCardCount;
    private PlayerStatus status;
    private int completedSets; //Amount of completed sets

    private List<String> bankCardIds=new ArrayList<>();


    public List<String> getBankCardIds() { return bankCardIds; }
    public void setBankCardIds(List<String> bankCardIds) { this.bankCardIds = bankCardIds; }
    public PlayerState() {
        this.propertyIds = new ArrayList<>();
        this.money = 0;
        this.handCardCount = 0;
        this.status = PlayerStatus.ACTIVE;
        this.completedSets = 0;
    }

    public PlayerState(String playerId, String playerName) {
        this();
        this.playerId = playerId;
        this.playerName = playerName;
    }

    //Getters and setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = money; }

    public List<String> getPropertyIds() { return propertyIds; }
    public void setPropertyIds(List<String> propertyIds) { this.propertyIds = propertyIds; }

    public int getHandCardCount() { return handCardCount; }
    public void setHandCardCount(int handCardCount) { this.handCardCount = handCardCount; }

    public PlayerStatus getStatus() { return status; }
    public void setStatus(PlayerStatus status) { this.status = status; }

    public int getCompletedSets() { return completedSets; }
    public void setCompletedSets(int completedSets) { this.completedSets = completedSets; }

    @Override
    public String toString() {
        return "PlayerState{" +
                "playerId='" + playerId + '\'' +
                ", playerName='" + playerName + '\'' +
                ", money=" + money +
                ", propertyIds=" + propertyIds +
                ", handCardCount=" + handCardCount +
                ", status=" + status +
                ", completedSets=" + completedSets +
                '}';
    }
}
