package edu.group10.monopolydeal.launcher;

import edu.group10.monopolydeal.backend.network.server.GameServer;
import edu.group10.monopolydeal.frontend.app.GameClientApp;
import edu.group10.monopolydeal.frontend.context.FrontendContext;
import edu.group10.monopolydeal.frontend.network.client.GameClient;

/**
 * Starts the local game server and launches the JavaFX client in the same process.
 */
public final class GameLauncher {

    /**
     * Prevents instantiation of this utility launcher class.
     */
    private GameLauncher() {
    }

    /**
     * Bootstraps the server and client for a local game session.
     *
     * @param args command-line arguments passed to the JavaFX application
     */
    public static void main(String[] args) {
        // Local server instance used by the desktop client.
        GameServer server = new GameServer(18080);
        server.start();

        // Shared client instance stored in the frontend context.
        GameClient client = new GameClient();
        FrontendContext.setGameClient(client);

        // Launch the JavaFX user interface.
        GameClientApp.launchApp(args);
    }
}
