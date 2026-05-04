package edu.group10.core.handler;

import edu.group10.common.model.Card;
import edu.group10.common.model.GameEvent;
import edu.group10.common.model.PlayerAction;
import edu.group10.core.GameEngineException;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Money cards handler
 * Handle with played money cards
 */
public class MoneyHandler {
    /**
     * Handle with played money cards
     *
     * @param state game state
     * @param player player who played cards
     * @param moneyCard money cards
     * @param action players' actions
     * @return event list
     */
    public List<GameEvent> handle(InternalGameState state, Player player,
                                  Card moneyCard, PlayerAction action)
            throws GameEngineException {

        List<GameEvent> events = new ArrayList<>();

        int value = moneyCard.getCardValue();
        player.addMoney(value);

        events.add(GameEvent.moneyPlayed(player.getPlayerId(), value));

        return events;
    }
}
