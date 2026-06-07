package edu.group10.monopolydeal.frontend.context;

import edu.group10.monopolydeal.frontend.network.client.GameClient;

/**
 * Stores frontend dependencies before JavaFX controllers are created.
 */
public final class FrontendContext {

    /** Shared client injected before the FXML controller is loaded. */
    private static GameClient gameClient;

    private FrontendContext() {
    }

    public static void setGameClient(GameClient client) {
        gameClient = client;
    }

    public static GameClient gameClient() {
        return gameClient;
    }
}
