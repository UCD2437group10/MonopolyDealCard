package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.PropertySetRules;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks completed sets and win conditions.
 */
final class VictoryManager {

    // Return the first player who has three completed base-color sets.
    String findWinner(List<String> turnOrder, Map<String, PlayerState> players) {
        for (String playerId : turnOrder) {
            PlayerState playerState = players.get(playerId);
            if (playerState == null) {
                continue;
            }
            Set<String> completedBaseColors = new LinkedHashSet<>();
            for (String color : playerState.properties().keySet()) {
                if (isCompleteSet(playerState, color)) {
                    completedBaseColors.add(PropertySetRules.baseColor(color));
                }
            }
            if (completedBaseColors.size() >= 3) {
                return playerId;
            }
        }
        return "";
    }

    boolean isCompleteSet(PlayerState playerState, String color) {
        return PropertySetRules.isCompleteSet(color, playerState.propertyCount(color));
    }

    int requiredSetSize(String color) {
        return PropertySetRules.requiredSetSize(color);
    }

    String baseColor(String color) {
        return PropertySetRules.baseColor(color);
    }
}
