package edu.group10.core.model;

import edu.group10.common.enums.GamePhase;
import edu.group10.common.enums.PlayerStatus;
import edu.group10.common.model.PlayerState;
import edu.group10.common.model.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal game state
 * Including the whole info (detailed content of hand cards, etc.), cannot be exposed to the front end
 */

public class InternalGameState {
    private String gameId;
    private List<Player> players; //Whole player objects (including hand cards)
    private Deck deck;
    private DiscardPile discardPile;
    private int currentPlayerIndex;
    private TurnContext turnContext;
    private GamePhase phase;
    private String winnerId;

    public InternalGameState(String gameId) {
        this.gameId = gameId;
        this.players = new ArrayList<>();
        this.deck = new Deck();
        this.discardPile = new DiscardPile();
        this.currentPlayerIndex = 0;
        this.turnContext = new TurnContext();
        this.phase = GamePhase.WAITING;
    }

    public Player getCurrentPlayer() {
        if (players.isEmpty() || currentPlayerIndex >= players.size()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    public Player getPlayer(String playerId) {
        return players.stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public List<Player> getActivePlayers() {
        return players.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .toList();
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        turnContext.startNewTurn();
    }

    /**
     * Change to outward GameState (hiding detailed content of hand cards)
     */
    public edu.group10.common.model.GameState toExternalGameState() {
        edu.group10.common.model.GameState external = new edu.group10.common.model.GameState();
        external.setGameId(gameId);
        external.setPhase(phase);
        external.setCurrentPlayerIndex(currentPlayerIndex);
        external.setDeckRemaining(deck.size());
        external.setDiscardPileCount(discardPile.size());
        external.setWinnerId(winnerId);
        external.setLastUpdateTime(System.currentTimeMillis());

        Map<String, PlayerState> externalPlayers = new HashMap<>();
        for (Player p : players) {
            PlayerState ps = new PlayerState();
            ps.setPlayerId(p.getPlayerId());
            ps.setPlayerName(p.getPlayerName());
            ps.setMoney(p.getMoney());
            ps.setHandCardCount(p.getHandSize());
            ps.setStatus(p.getStatus());
            ps.setCompletedSets(p.getCompletedSets());

            List<String> propertyIds = p.getProperties().stream()
                    .map(Property::getCardId)
                    .toList();
            ps.setPropertyIds(propertyIds);

            externalPlayers.put(p.getPlayerId(), ps);
        }
        external.setPlayers(externalPlayers);

        return external;
    }

    // Getters and Setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }

    public Deck getDeck() { return deck; }
    public void setDeck(Deck deck) { this.deck = deck; }

    public DiscardPile getDiscardPile() { return discardPile; }
    public void setDiscardPile(DiscardPile discardPile) { this.discardPile = discardPile; }

    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public void setCurrentPlayerIndex(int currentPlayerIndex) { this.currentPlayerIndex = currentPlayerIndex; }

    public TurnContext getTurnContext() { return turnContext; }
    public void setTurnContext(TurnContext turnContext) { this.turnContext = turnContext; }

    public GamePhase getPhase() { return phase; }
    public void setPhase(GamePhase phase) { this.phase = phase; }

    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
}
