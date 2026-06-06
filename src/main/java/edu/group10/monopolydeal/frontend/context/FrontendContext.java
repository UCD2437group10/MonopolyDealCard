package edu.group10.monopolydeal.frontend.context;

import edu.group10.monopolydeal.frontend.network.client.GameClient;

/**
 * Frontend context: inject dependencies before JavaFX startup.
 */
public final class FrontendContext {

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
