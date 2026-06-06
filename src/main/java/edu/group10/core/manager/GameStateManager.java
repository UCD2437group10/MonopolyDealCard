package edu.group10.core.manager;

import edu.group10.common.enums.GamePhase;
import edu.group10.common.model.Card;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameStateManager {
    private final Map<String, InternalGameState> games;
    private final CardManager cardManager;

    public GameStateManager(CardManager cardManager) {
        this.games = new ConcurrentHashMap<>();
        this.cardManager = cardManager;
    }

    /**
     * Create new game
     */
    public InternalGameState createGame(String gameId, List<String> playerIds, List<String> playerNames) {
        InternalGameState state = new InternalGameState(gameId);

        //Create players
        for (int i = 0; i < playerIds.size(); i++) {
            Player player = new Player(playerIds.get(i), playerNames.get(i));
            state.getPlayers().add(player);
        }

        games.put(gameId, state);
        return state;
    }

    /**
     * Start the game (shuffling and dealing cards)
     */
    public void startGame(String gameId) {
        InternalGameState state = games.get(gameId);
        if (state == null) return;

        state.getDeck().initialize(cardManager.getShuffledDeck());

        for (Player player : state.getPlayers()) {
            for (int i = 0; i < 5; i++) {
                drawCard(state, player);
            }
        }

        //Randomly choose the player to start
        int firstPlayerIndex = (int) (Math.random() * state.getPlayers().size());
        state.setCurrentPlayerIndex(firstPlayerIndex);
        state.getTurnContext().startNewTurn();
        state.setPhase(GamePhase.PLAYING);
    }

    /**
     * Draw cards
     */
    public Card drawCard(InternalGameState state, Player player) {
        if (state.getDeck().isEmpty()) {
            if (!state.getDiscardPile().isEmpty()) {
                state.getDeck().reshuffleFromDiscard(state.getDiscardPile().takeAll());
            } else {
                return null; //Being draw if both the deck and the discard pile is empty
            }
        }

        Card card = state.getDeck().draw();
        if (card != null) {
            player.addCardToHand(card);
        }
        return card;
    }

    public InternalGameState getGame(String gameId) {
        return games.get(gameId);
    }

    public void removeGame(String gameId) {
        games.remove(gameId);
    }

    public boolean gameExists(String gameId) {
        return games.containsKey(gameId);
    }

    //Used when reconnecting: save snapshot (can be extended to be persisted)
    public void saveSnapshot(String gameId) {
        //Not being implemented currently, can be extended in future
    }
}
