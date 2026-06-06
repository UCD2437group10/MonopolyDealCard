package edu.group10.network.protocol;

/**
 * Serialized response wrapper used by socket transport.
 */
public record NetGameResponse(boolean success, String message, NetGameState gameState) {
}
