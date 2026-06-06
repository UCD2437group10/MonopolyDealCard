package edu.group10.core.handler;

import edu.group10.common.model.Card;
import edu.group10.common.model.GameEvent;
import edu.group10.common.enums.GamePhase;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Draw card handler
 */
public class DrawCardHandler {
    /**
     * Handle with drawing cards
     */
    public List<GameEvent> handle(InternalGameState state, Player player) {
        List<GameEvent> events = new ArrayList<>();

        Card card = drawFromDeck(state);

        if (card == null) {
            events.add(GameEvent.warning("The deck is empty, you cannot draw cards."));
            state.setPhase(GamePhase.ENDED);
            return events;
        }

        player.addCardToHand(card);
        events.add(GameEvent.cardDrawn(player.getPlayerId(), card.getCardId()));

        return events;
    }

    /**
     * Draw multiple cards
     */
    public List<GameEvent> handleMultiple(InternalGameState state, Player player, int count) {
        List<GameEvent> events = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Card card = drawFromDeck(state);
            if (card == null) {
                events.add(GameEvent.warning("The deck is empty, you cannot draw cards." + count + "cards"));
                break;
            }
            player.addCardToHand(card);
            events.add(GameEvent.cardDrawn(player.getPlayerId(), card.getCardId()));
        }

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
