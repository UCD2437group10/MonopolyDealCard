package edu.group10.monopolydeal.launcher;

import edu.group10.monopolydeal.backend.network.server.GameServer;
import edu.group10.monopolydeal.frontend.app.GameClientApp;
import edu.group10.monopolydeal.frontend.context.FrontendContext;
import edu.group10.monopolydeal.frontend.network.client.GameClient;

/**
 * Startup entry: start server within the same process and launch JavaFX client.
 */
public final class GameLauncher {

    private GameLauncher() {
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(18080);
        server.start();
        GameClient client = new GameClient();
        FrontendContext.setGameClient(client);
        GameClientApp.launchApp(args);
    }
}
