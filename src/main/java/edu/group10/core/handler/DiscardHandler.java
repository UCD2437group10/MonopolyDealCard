package edu.group10.core.handler;

import edu.group10.common.model.Card;
import edu.group10.common.model.GameEvent;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Discard cards handler
 */
public class DiscardHandler {
    /**
     * Handle with discarded cards
     */
    public List<GameEvent> handle(InternalGameState state, Player player, List<String> cardIds) {
        List<GameEvent> events = new ArrayList<>();

        for (String cardId : cardIds) {
            if (player.hasCard(cardId)) {
                Card card = getCardFromHand(player, cardId);
                player.removeCardFromHand(cardId);
                state.getDiscardPile().add(card);
                events.add(GameEvent.cardDiscarded(player.getPlayerId(), cardId));
            }
        }

        if (player.getHandSize() > 7) {
            events.add(GameEvent.warning(
                    String.format("You still have %d cards, please discard cards.", player.getHandSize())));
        }

        return events;
    }

    private Card getCardFromHand(Player player, String cardId) {
        return player.getHand().stream()
                .filter(c -> c.getCardId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
