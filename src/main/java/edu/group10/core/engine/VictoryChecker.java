package edu.group10.core.engine;

import edu.group10.common.enums.PropertyColor;
import edu.group10.common.model.Property;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Victory checker
 * Check if the player completed the condition of victory (complete 3 whole property sets)
 */
public class VictoryChecker {
    /**
     * Check and return ID of the winner
     * @return ID of the winner, null if there is no winner
     */
    public String checkAndGetWinner(InternalGameState state) {
        if (state == null) return null;

        for (Player player : state.getActivePlayers()) {
            if (isWinner(player, state)) {
                return player.getPlayerId();
            }
        }

        return null;
    }

    /**
     * Check if a single player wins
     */
    public boolean isWinner(Player player, InternalGameState state) {
        if (player == null) return false;

        int completedSets = countCompletedSets(player);
        player.setCompletedSets(completedSets);

        return completedSets >= 3;
    }

    /**
     * Calculate the completed sets of properties of the players
     */
    public int countCompletedSets(Player player) {
        if (player == null) return 0;

        Map<PropertyColor, Integer> colorCount = new HashMap<>();

        for (Property property : player.getProperties()) {
            PropertyColor color = property.getCurrentColor();

            if (color == PropertyColor.WILD) {
                continue;
            }

            colorCount.put(color, colorCount.getOrDefault(color, 0) + 1);
        }

        int completedSets = 0;

        for (Map.Entry<PropertyColor, Integer> entry : colorCount.entrySet()) {
            PropertyColor color = entry.getKey();
            int ownedCount = entry.getValue();
            int requiredCount = getSetSize(color);

            if (ownedCount >= requiredCount) {
                completedSets++;
            }
        }

        int wildCount = (int) player.getProperties().stream()
                .filter(p -> p.getCurrentColor() == PropertyColor.WILD)
                .count();

        completedSets += Math.min(wildCount, 3 - completedSets);

        return Math.min(completedSets, 3);
    }

    /**
     * Get the numbers of cards needed to complete property sets in every color
     */
    private int getSetSize(PropertyColor color) {
        return switch (color) {
            case BROWN -> 2;
            case LIGHT_BLUE -> 3;
            case PINK -> 3;
            case ORANGE -> 3;
            case RED -> 3;
            case YELLOW -> 3;
            case GREEN -> 3;
            case DARK_BLUE -> 2;
            case RAILROAD -> 4;
            case UTILITY -> 2;
            case WILD -> 1;
            default -> 1;
        };
    }

    /**
     * Check and update the numbers of completed property sets of every player
     */
    public void updateAllPlayersCompletedSets(InternalGameState state) {
        for (Player player : state.getPlayers()) {
            int completedSets = countCompletedSets(player);
            player.setCompletedSets(completedSets);
        }
    }
}
