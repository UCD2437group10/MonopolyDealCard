package edu.group10.core.engine;

import edu.group10.common.enums.CardType;
import edu.group10.common.enums.GamePhase;
import edu.group10.common.enums.PlayerStatus;
import edu.group10.common.model.Card;
import edu.group10.common.model.Property;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;

/**
 * Rule validator
 * Check the validity of all types of operation
 */
public class RuleValidator {
    /**
     * Check if this card can be played
     */
    public boolean canPlayCard(Player player, Card card, InternalGameState state) {
        if (player == null || card == null) return false;

        if (state.getPhase() != GamePhase.PLAYING) return false;

        Player currentPlayer = state.getCurrentPlayer();
        if (currentPlayer == null || !currentPlayer.getPlayerId().equals(player.getPlayerId())) {
            return false;
        }

        if (!state.getTurnContext().hasActionsLeft()) {
            return false;
        }

        return switch (card.getCardType()) {
            case PROPERTY -> canPlayProperty(player, (Property) card, state);
            case MONEY -> true;
            case ACTION -> canPlayAction(player, card, state);
            default -> false;
        };
    }

    private boolean canPlayProperty(Player player, Property property, InternalGameState state) {
        return state.getPhase() == GamePhase.PLAYING;
    }

    private boolean canPlayAction(Player player, Card actionCard, InternalGameState state) {
        return true;
    }

    /**
     * Check if the target player is valid
     */
    public boolean isValidTarget(Player actor, String targetPlayerId, InternalGameState state) {
        if (targetPlayerId == null) return false;

        Player target = state.getPlayer(targetPlayerId);
        if (target == null) return false;

        if (actor.getPlayerId().equals(targetPlayerId)) {
            return false;
        }

        return target.getStatus() == PlayerStatus.ACTIVE;
    }

    /**
     * Check if the hand cards exceed the upper limit
     */
    public boolean isHandOverLimit(Player player) {
        return player.getHandSize() > 7;
    }

    /**
     * Check if the player is bankrupted
     */
    public boolean isBankrupt(Player player) {
        return player.getMoney() <= 0 && player.getProperties().isEmpty();
    }

    /**
     * Check if it can declare victory
     */
    public boolean canDeclareVictory(Player player) {
        return player.getCompletedSets() >= 3;
    }
}
