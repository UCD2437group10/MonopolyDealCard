package edu.group10.monopolydeal.backend.network.protocol;

import edu.group10.monopolydeal.backend.game.GameState;

/**
 * 后端返回结果。
 */
public record GameResponse(boolean success, String message, GameState gameState) {
}
