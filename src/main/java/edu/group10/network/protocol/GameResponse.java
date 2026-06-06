package edu.group10.network.protocol;

import edu.group10.monopolydeal.backend.game.GameState;

/**
 * Backend response containing status text and a game snapshot.
 */
public record GameResponse(boolean success, String message, GameState gameState) {
}
