package edu.group10.core.handler;

import edu.group10.common.model.GameEvent;
import edu.group10.common.model.Property;
import edu.group10.common.model.PlayerAction;
import edu.group10.common.enums.PropertyColor;
import edu.group10.core.GameEngineException;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Property handler
 * Handle with played property cards
 */
public class PropertyHandler {
    /**
     * Handle with played property cards
     *
     * @param state game state
     * @param player player who played cards
     * @param property property cards
     * @param action players' action (could include the selected colors)
     * @return event list
     */
    public List<GameEvent> handle(InternalGameState state, Player player,
                                  Property property, PlayerAction action)
            throws GameEngineException {

        List<GameEvent> events = new ArrayList<>();

        if (property.isDualColor() && action.getSelectedColor() != null) {
            property.switchColor(action.getSelectedColor());
        }

        if (property.getPrimaryColor() == PropertyColor.WILD && action.getSelectedColor() != null) {
            property.switchColor(action.getSelectedColor());
        }

        player.addProperty(property);
 
        events.add(GameEvent.propertyPlayed(player.getPlayerId(), property.getCardId()));

        return events;
    }
}
