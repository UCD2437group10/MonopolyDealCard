package edu.group10.monopolydeal.backend.network.protocol;

public record NetGameResponse(boolean success, String message, NetGameState gameState) {
}
