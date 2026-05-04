package edu.group10.core.handler;

import edu.group10.common.model.Card;
import edu.group10.common.model.GameEvent;
import edu.group10.common.enums.GamePhase;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * End turn handler
 */
public class EndTurnHandler {
    /**
     * Handle with the end turn
     */
    public List<GameEvent> handle(InternalGameState state, Player player) {
        List<GameEvent> events = new ArrayList<>();

        if (player.getHandSize() > 7) {
            events.add(GameEvent.warning(
                    String.format("Your hand cards exceed 7 (current %d cards), please discard cards first.", player.getHandSize())));
            return events;
        }

        state.nextTurn();

        Player newCurrentPlayer = state.getCurrentPlayer();
        int cardsToDraw = newCurrentPlayer.getHandSize() == 0 ? 5 : 2;

        for (int i = 0; i < cardsToDraw; i++) {
            Card card = drawFromDeck(state);
            if (card != null) {
                newCurrentPlayer.addCardToHand(card);
                events.add(GameEvent.cardDrawn(newCurrentPlayer.getPlayerId(), card.getCardId()));
            } else {
                events.add(GameEvent.warning("The deck is empty. The game is draw."));
                state.setPhase(GamePhase.ENDED);
                break;
            }
        }

        events.add(GameEvent.turnEnded(player.getPlayerId(), newCurrentPlayer.getPlayerId()));

        return events;
    }

    private Card drawFromDeck(InternalGameState state) {
        if (state.getDeck().isEmpty()) {
            if (!state.getDiscardPile().isEmpty()) {
                state.getDeck().reshuffleFromDiscard(state.getDiscardPile().takeAll());
            } else {
                return null;
            }
        }
        return state.getDeck().draw();
    }
}
