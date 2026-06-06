package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.frontend.network.client.GameClient;
import java.util.Map;

/**
 * Lobby controller.
 */
public class LobbyController {

    private final GameClient gameClient;

    public LobbyController(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    public void connect(String host, int port) {
        gameClient.connect(host, port);
    }

    public GameResponse join(String playerId, String playerName, boolean bot) {
        return gameClient.send("JOIN", playerId, Map.of("name", playerName, "bot", String.valueOf(bot)));
    }

    public GameResponse startGame(String operatorId) {
        return gameClient.send("START", operatorId, Map.of());
    }

    public GameResponse refreshState(String playerId) {
        return gameClient.send("STATE", playerId, Map.of());
    }
}
