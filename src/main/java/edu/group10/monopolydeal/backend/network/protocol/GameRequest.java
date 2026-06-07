package edu.group10.monopolydeal.backend.network.protocol;

import java.util.Map;

/**
 * Command request sent from the client to the backend.
 */
public record GameRequest(String action, String playerId, Map<String, String> payload) {
}
