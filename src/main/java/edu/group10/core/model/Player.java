package edu.group10.core.model;

import edu.group10.common.enums.PlayerStatus;
import edu.group10.common.model.Card;
import edu.group10.common.model.Property;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String playerId;
    private String playerName;
    private int money;
    private List<Card> hand; //Hand cards (specific card objects)
    private List<Property> properties; //Properties (whole objects)
    private PlayerStatus status;
    private int completedSets; //Number of completed properties

    public Player(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.money = 0;
        this.hand = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.status = PlayerStatus.ACTIVE;
        this.completedSets = 0;
    }

    public void addCardToHand(Card card) {
        this.hand.add(card);
    }

    public void removeCardFromHand(String cardId) {
        this.hand.removeIf(c -> c.getCardId().equals(cardId));
    }

    public boolean hasCard(String cardId) {
        return hand.stream().anyMatch(c -> c.getCardId().equals(cardId));
    }

    public void addMoney(int amount) {
        this.money += amount;
    }

    public boolean removeMoney(int amount) {
        if (this.money >= amount) {
            this.money -= amount;
            return true;
        }
        return false;
    }

    public void addProperty(Property property) {
        this.properties.add(property);
    }

    public void removeProperty(String propertyId) {
        this.properties.removeIf(p -> p.getCardId().equals(propertyId));
    }

    public Property getProperty(String propertyId) {
        return properties.stream()
                .filter(p -> p.getCardId().equals(propertyId))
                .findFirst()
                .orElse(null);
    }

    public int getHandSize() {
        return hand.size();
    }

    public List<Property> getNonCompleteSetProperties() {
        //TODO: returns properties that is not completed (used by Forced Deal, etc.)
        return new ArrayList<>();
    }

    // Getters and Setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = money; }

    public List<Card> getHand() { return hand; }
    public void setHand(List<Card> hand) { this.hand = hand; }

    public List<Property> getProperties() { return properties; }
    public void setProperties(List<Property> properties) { this.properties = properties; }

    public PlayerStatus getStatus() { return status; }
    public void setStatus(PlayerStatus status) { this.status = status; }

    public int getCompletedSets() { return completedSets; }
    public void setCompletedSets(int completedSets) { this.completedSets = completedSets; }
}
