package edu.group10.common.model;

import edu.group10.common.enums.GamePhase;
import edu.group10.common.enums.PropertyColor;
import edu.group10.core.card.action.Suit;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SkillContext {
    private String gameId;
    private String actorId; //ID of player who used skills
    private String targetId; //Target player (could be null)
    private Card skillCard; //Current used skill card
    private Map<String, PlayerState> players; //State of all players (read-only view)
    private GamePhase currentPhase;
    private int currentTurnPlayerIndex;
    private List<Property> targetPlayerProperties; //Property list of target player (advance getting)
    private int targetPlayerMoney;
    private PropertyColor selectedColor;
    private int baseRent;
    private Suit lastRentSuit;

    public SkillContext() {}

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public Card getSkillCard() { return skillCard; }
    public void setSkillCard(Card skillCard) { this.skillCard = skillCard; }

    public Map<String, PlayerState> getPlayers() { return players; }
    public void setPlayers(Map<String, PlayerState> players) { this.players = players; }

    public GamePhase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(GamePhase currentPhase) { this.currentPhase = currentPhase; }

    public int getCurrentTurnPlayerIndex() { return currentTurnPlayerIndex; }
    public void setCurrentTurnPlayerIndex(int currentTurnPlayerIndex) { this.currentTurnPlayerIndex = currentTurnPlayerIndex; }

    public List<Property> getTargetPlayerProperties() { return targetPlayerProperties; }
    public void setTargetPlayerProperties(List<Property> targetPlayerProperties) { this.targetPlayerProperties = targetPlayerProperties; }

    public int getTargetPlayerMoney() { return targetPlayerMoney; }
    public void setTargetPlayerMoney(int targetPlayerMoney) { this.targetPlayerMoney = targetPlayerMoney; }

    public PropertyColor getSelectedColor() { return selectedColor; }
    public void setSelectedColor(PropertyColor selectedColor) { this.selectedColor = selectedColor; }

    public int getBaseRent() { return baseRent; }
    public void setBaseRent(int baseRent) { this.baseRent = baseRent; }

    public Suit getLastRentSuit() { return lastRentSuit; }
    public void setLastRentSuit(Suit lastRentSuit) { this.lastRentSuit = lastRentSuit; }

    //Get PlayerState of target player
    public PlayerState getTargetPlayerState() {
        if (targetId == null || players == null) return null;
        return players.get(targetId);
    }

    //Get PlayerState of the actor
    public PlayerState getActorPlayerState() {
        if (actorId == null || players == null) return null;
        return players.get(actorId);
    }

    //Get list of other players' ID
    public List<String> getOtherPlayerIds() {
        if (players == null || actorId == null) return List.of();
        return players.keySet().stream()
                .filter(id -> !id.equals(actorId))
                .collect(Collectors.toList());
    }

    //Get String of other players' ID
    public String[] getOtherPlayerIdsAsArray() {
        List<String> others = getOtherPlayerIds();
        return others.toArray(new String[0]);
    }

    //Get list of other players' PlayerState
    public List<PlayerState> getOtherPlayers() {
        if (players == null || actorId == null) return List.of();
        return players.values().stream()
                .filter(p -> !p.getPlayerId().equals(actorId))
                .collect(Collectors.toList());
    }

    //Get list of all players' ID
    public List<String> getAllPlayerIds() {
        if (players == null) return List.of();
        return players.keySet().stream().collect(Collectors.toList());
    }

    //Check if there is a target player
    public boolean hasTarget() {
        return targetId != null && !targetId.isEmpty();
    }

    //Check if the target player is the actor himself
    public boolean isTargetSelf() {
        return targetId != null && targetId.equals(actorId);
    }

    @Override
    public String toString() {
        return "SkillContext{" +
                "gameId='" + gameId + '\'' +
                ", actorId='" + actorId + '\'' +
                ", targetId='" + targetId + '\'' +
                ", skillCard=" + skillCard +
                ", targetPlayerMoney=" + targetPlayerMoney +
                '}';
    }
}
