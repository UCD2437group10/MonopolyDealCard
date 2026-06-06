package edu.group10.common.model;

import edu.group10.common.enums.GamePhase;
import java.util.HashMap;
import java.util.Map;

public class GameState {
    private String gameId;
    private GamePhase phase;
    private int currentPlayerIndex;
    private Map<String, PlayerState> players; //PlayerId → PlayerState
    private int deckRemaining; //Remaining deck number
    private int discardPileCount;
    private String winnerId; //Winner ID (when the game is over)
    private long lastUpdateTime;

    public GameState() {
        this.players = new HashMap<>();
        this.phase = GamePhase.WAITING;
        this.currentPlayerIndex = 0;
    }

    public GameState(String gameId) {
        this();
        this.gameId = gameId;
    }

    //Get player of the current turn
    public PlayerState getCurrentPlayer() {
        if (players.isEmpty() || currentPlayerIndex >= players.size()) {
            return null;
        }
        return players.values().stream()
                .skip(currentPlayerIndex)
                .findFirst()
                .orElse(null);
    }

    //Getters and setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public GamePhase getPhase() { return phase; }
    public void setPhase(GamePhase phase) { this.phase = phase; }

    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public void setCurrentPlayerIndex(int currentPlayerIndex) { this.currentPlayerIndex = currentPlayerIndex; }

    public Map<String, PlayerState> getPlayers() { return players; }
    public void setPlayers(Map<String, PlayerState> players) { this.players = players; }

    public int getDeckRemaining() { return deckRemaining; }
    public void setDeckRemaining(int deckRemaining) { this.deckRemaining = deckRemaining; }

    public int getDiscardPileCount() { return discardPileCount; }
    public void setDiscardPileCount(int discardPileCount) { this.discardPileCount = discardPileCount; }

    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }

    public long getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }

    @Override
    public String toString() {
        return "GameState{" +
                "gameId='" + gameId + '\'' +
                ", phase=" + phase +
                ", currentPlayerIndex=" + currentPlayerIndex +
                ", players=" + players +
                ", deckRemaining=" + deckRemaining +
                ", winnerId='" + winnerId + '\'' +
                '}';
    }
}
