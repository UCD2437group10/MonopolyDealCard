package edu.group10.core.engine;

import edu.group10.common.model.Command;
import edu.group10.common.model.GameEvent;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;
import edu.group10.common.model.Property;
import edu.group10.common.model.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Command executor
 * Execute Command returned from Logic model
 */
public class CommandExecutor {
    /**
     * Execute a single command
     */
    public List<GameEvent> execute(InternalGameState state, Command cmd) {
        List<GameEvent> events = new ArrayList<>();

        if (cmd == null || cmd.getType() == null) {
            return events;
        }

        switch (cmd.getType()) {
            case TRANSFER_MONEY:
                events.add(transferMoney(state, cmd));
                break;

            case REMOVE_MONEY:
                events.add(removeMoney(state, cmd));
                break;

            case TRANSFER_PROPERTY:
                events.add(transferProperty(state, cmd));
                break;

            case SWAP_PROPERTY:
                events.addAll(swapProperty(state, cmd));
                break;

            case DRAW_CARD:
                events.addAll(drawCards(state, cmd));
                break;

            case ADD_HOUSE:
                events.add(addHouse(state, cmd));
                break;

            case ADD_HOTEL:
                events.add(addHotel(state, cmd));
                break;

            case CHANGE_OWN:
                events.add(changeOwnership(state, cmd));
                break;

            case STOP_CARD:
                events.add(stopCard(state, cmd));
                break;

            default:
                System.out.println("[CommandExecutor] unhandled command type: " + cmd.getType());
        }

        return events;
    }

    /**
     * Execute multiple commands
     */
    public List<GameEvent> executeAll(InternalGameState state, List<Command> commands) {
        List<GameEvent> allEvents = new ArrayList<>();
        if (commands == null) return allEvents;

        for (Command cmd : commands) {
            allEvents.addAll(execute(state, cmd));
        }
        return allEvents;
    }

    /**
     * Transfer money (from one player to another)
     */
    private GameEvent transferMoney(InternalGameState state, Command cmd) {
        Player from = state.getPlayer(cmd.getFromPlayerId());
        Player to = state.getPlayer(cmd.getToPlayerId());

        if (from == null || to == null) return null;

        int amount = cmd.getAmount();
        if (from.getMoney() >= amount) {
            from.removeMoney(amount);
            to.addMoney(amount);
        } else {
            int actualAmount = from.getMoney();
            from.removeMoney(actualAmount);
            to.addMoney(actualAmount);
        }

        return GameEvent.moneyTransferred(from.getPlayerId(), to.getPlayerId(), amount);
    }

    /**
     * Remove money (remove from one player, not transfer to any other player)
     */
    private GameEvent removeMoney(InternalGameState state, Command cmd) {
        Player player = state.getPlayer(cmd.getFromPlayerId());
        if (player == null) return null;

        int amount = cmd.getAmount();
        if (player.getMoney() >= amount) {
            player.removeMoney(amount);
        } else {
            amount = player.getMoney();
            player.removeMoney(amount);
        }

        return GameEvent.moneyChanged(player.getPlayerId(), amount, "remove");
    }

    /**
     * Transfer properties
     */
    private GameEvent transferProperty(InternalGameState state, Command cmd) {
        Player from = state.getPlayer(cmd.getFromPlayerId());
        Player to = state.getPlayer(cmd.getToPlayerId());

        if (from == null || to == null) return null;

        String propertyId = cmd.getPropertyId();
        Property property = from.getProperty(propertyId);

        if (property != null) {
            from.removeProperty(propertyId);
            to.addProperty(property);
            return GameEvent.propertyTransferred(from.getPlayerId(), to.getPlayerId(), propertyId);
        }

        return null;
    }

    /**
     * Exchange properties (Forced Deal)
     */
    private List<GameEvent> swapProperty(InternalGameState state, Command cmd) {
        List<GameEvent> events = new ArrayList<>();

        Player player1 = state.getPlayer(cmd.getFromPlayerId());
        Player player2 = state.getPlayer(cmd.getToPlayerId());

        if (player1 == null || player2 == null) return events;

        String propertyId1 = cmd.getPropertyId();
        String propertyId2 = cmd.getExtraPropertyId();

        Property prop1 = player1.getProperty(propertyId1);
        Property prop2 = player2.getProperty(propertyId2);

        if (prop1 != null && prop2 != null) {
            player1.removeProperty(propertyId1);
            player2.removeProperty(propertyId2);
            player1.addProperty(prop2);
            player2.addProperty(prop1);

            events.add(GameEvent.propertySwapped(player1.getPlayerId(), player2.getPlayerId(),
                    propertyId1, propertyId2));
        }

        return events;
    }

    /**
     * Draw cards (Pass Go etc.)
     */
    private List<GameEvent> drawCards(InternalGameState state, Command cmd) {
        List<GameEvent> events = new ArrayList<>();
        Player player = state.getPlayer(cmd.getToPlayerId());

        if (player == null) return events;

        int amount = cmd.getAmount();
        for (int i = 0; i < amount; i++) {
            if (state.getDeck().isEmpty()) {
                if (!state.getDiscardPile().isEmpty()) {
                    state.getDeck().reshuffleFromDiscard(state.getDiscardPile().takeAll());
                } else {
                    break;
                }
            }
            Card card = state.getDeck().draw();
            if (card != null) {
                player.addCardToHand(card);
                events.add(GameEvent.cardDrawn(player.getPlayerId(), card.getCardId()));
            }
        }

        return events;
    }

    /**
     * Add house
     */
    private GameEvent addHouse(InternalGameState state, Command cmd) {
        Player player = state.getPlayer(cmd.getToPlayerId());
        if (player == null) return null;

        String propertyId = cmd.getPropertyId();
        Property property = player.getProperty(propertyId);

        if (property != null) {
            property.setHasHouse(true);
            return GameEvent.houseAdded(player.getPlayerId(), propertyId);
        }

        return null;
    }

    /**
     * Add hotel
     */
    private GameEvent addHotel(InternalGameState state, Command cmd) {
        Player player = state.getPlayer(cmd.getToPlayerId());
        if (player == null) return null;

        String propertyId = cmd.getPropertyId();
        Property property = player.getProperty(propertyId);

        if (property != null) {
            property.setHasHotel(true);
            return GameEvent.hotelAdded(player.getPlayerId(), propertyId);
        }

        return null;
    }

    /**
     * Change the ownership (Deal Breaker)
     */
    private GameEvent changeOwnership(InternalGameState state, Command cmd) {
        return transferProperty(state, cmd);
    }

    /**
     * Stop the effect of a card (Just Say No)
     */
    private GameEvent stopCard(InternalGameState state, Command cmd) {
        return GameEvent.cardStopped(cmd.getFromPlayerId(), cmd.getToPlayerId());
    }
}
