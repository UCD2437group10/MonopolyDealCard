package edu.group10.monopolydeal.launcher;

import edu.group10.monopolydeal.backend.network.server.GameServer;
import edu.group10.monopolydeal.frontend.app.GameClientApp;
import edu.group10.monopolydeal.frontend.context.FrontendContext;
import edu.group10.monopolydeal.frontend.network.client.GameClient;

/**
 * 启动入口：同进程内启动服务端并拉起 JavaFX 客户端。
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
