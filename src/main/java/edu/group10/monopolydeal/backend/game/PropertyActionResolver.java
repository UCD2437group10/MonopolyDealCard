package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.Map;

/**
 * Resolves property-stealing and swapping action cards.
 */
final class PropertyActionResolver {

    private final Map<String, PlayerState> players;
    private final VictoryManager victoryManager;

    PropertyActionResolver(Map<String, PlayerState> players, VictoryManager victoryManager) {
        this.players = players;
        this.victoryManager = victoryManager;
    }

    // Move one stealable property card from the target to the actor.
    void stealSingleProperty(String actorId, String targetId, String color, int propertyIndex) {
        if (actorId.equals(targetId)) {
            throw new IllegalArgumentException("target must be another player");
        }
        PlayerState target = playerState(targetId);
        if (victoryManager.isCompleteSet(target, color)) {
            throw new IllegalStateException("cannot steal from complete set");
        }
        Card card = target.removeProperty(color, propertyIndex);
        playerState(actorId).addProperty(color, card);
    }

    // Swap one incomplete-set property from each player.
    void forcedSwapProperty(String actorId, String targetId, String myColor, int myIndex, String targetColor, int targetIndex) {
        if (actorId.equals(targetId)) {
            throw new IllegalArgumentException("target must be another player");
        }
        if (victoryManager.isCompleteSet(playerState(actorId), myColor)
                || victoryManager.isCompleteSet(playerState(targetId), targetColor)) {
            throw new IllegalStateException("forced deal cannot use complete-set property");
        }
        PlayerState actor = playerState(actorId);
        PlayerState target = playerState(targetId);
        Card mine = actor.removeProperty(myColor, myIndex);
        Card targetCard = target.removeProperty(targetColor, targetIndex);
        actor.addPropertyToExactGroup(targetColor, targetCard);
        target.addPropertyToExactGroup(myColor, mine);
    }

    // Transfer a full completed set, including house and hotel markers.
    void stealCompleteSet(String actorId, String targetId, String color) {
        if (actorId.equals(targetId)) {
            throw new IllegalArgumentException("target must be another player");
        }
        PlayerState target = playerState(targetId);
        PlayerState actor = playerState(actorId);
        if (!victoryManager.isCompleteSet(target, color)) {
            throw new IllegalStateException("target color is not complete set");
        }

        actor.setAllProperties(color, target.removeAllProperties(color));
        if (target.clearHouse(color) > 0) {
            actor.addHouse(color);
        }
        if (target.clearHotel(color) > 0) {
            actor.addHotel(color);
        }
    }

    private PlayerState playerState(String playerId) {
        PlayerState state = players.get(playerId);
        if (state == null) {
            throw new IllegalArgumentException("unknown player: " + playerId);
        }
        return state;
    }
}
