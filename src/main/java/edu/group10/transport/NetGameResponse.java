package edu.group10.transport;

/**
 * Serialized response wrapper used by socket transport.
 */
public record NetGameResponse(boolean success, String message, NetGameState gameState) {
}
