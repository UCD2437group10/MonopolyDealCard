package edu.group10.monopolydeal.backend.model.player;

/**
 * Stores the basic identity data for one player.
 */
public record Player(String id, String displayName, boolean bot) {
}
