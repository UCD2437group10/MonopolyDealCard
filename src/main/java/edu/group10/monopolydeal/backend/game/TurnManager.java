package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Handles card drawing and turn progression rules.
 */
final class TurnManager {

    private final Map<String, PlayerState> players;
    private final Deque<Card> drawPile;
    private final Deque<Card> discardPile;
    private final List<String> turnOrder;
    private final int emptyHandBonusDraw;
    private final int turnStartDraw;
    private final int endHandLimit;

    TurnManager(
            Map<String, PlayerState> players,
            Deque<Card> drawPile,
            Deque<Card> discardPile,
            List<String> turnOrder,
            int emptyHandBonusDraw,
            int turnStartDraw,
            int endHandLimit
    ) {
        this.players = players;
        this.drawPile = drawPile;
        this.discardPile = discardPile;
        this.turnOrder = turnOrder;
        this.emptyHandBonusDraw = emptyHandBonusDraw;
        this.turnStartDraw = turnStartDraw;
        this.endHandLimit = endHandLimit;
    }

    // Reset every hand and deal the opening cards for a fresh game.
    void dealOpeningHands(int startHandCount) {
        for (String playerId : turnOrder) {
            PlayerState playerState = playerState(playerId);
            while (!playerState.hand().isEmpty()) {
                playerState.removeHandCard(0);
            }
            drawCards(playerId, startHandCount);
        }
    }

    void drawCards(String playerId, int count) {
        for (int i = 0; i < count; i++) {
            if (drawPile.isEmpty()) {
                reshuffleIfNeeded();
            }
            if (drawPile.isEmpty()) {
                throw new IllegalStateException("no cards left");
            }
            playerState(playerId).addToHand(drawPile.pop());
        }
        applyHandOverflow(playerId);
    }

    // Draw the normal start-of-turn cards for the current player.
    void applyStartOfTurnDraw(int turnIndex) {
        if (turnOrder.isEmpty()) {
            return;
        }
        String current = turnOrder.get(turnIndex);
        int count = playerState(current).hand().isEmpty() ? emptyHandBonusDraw : turnStartDraw;
        drawCards(current, count);
    }

    // End the current turn and move play to the next player.
    int advanceTurn(String playerId, int currentTurnIndex) {
        if (playerState(playerId).hand().size() > endHandLimit) {
            throw new IllegalStateException("hand size must be <= 7 before end turn");
        }
        int nextTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
        applyStartOfTurnDraw(nextTurnIndex);
        return nextTurnIndex;
    }

    private void applyHandOverflow(String playerId) {
        PlayerState state = playerState(playerId);
        while (state.hand().size() > endHandLimit) {
            state.removeHandCard(state.hand().size() - 1);
        }
    }

    private PlayerState playerState(String playerId) {
        PlayerState state = players.get(playerId);
        if (state == null) {
            throw new IllegalArgumentException("unknown player: " + playerId);
        }
        return state;
    }

    private void reshuffleIfNeeded() {
        if (discardPile.isEmpty()) {
            return;
        }
        while (!discardPile.isEmpty()) {
            drawPile.push(discardPile.pop());
        }
    }
}
