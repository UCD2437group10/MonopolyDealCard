package edu.group10.monopolydeal.frontend.context;

import edu.group10.monopolydeal.frontend.network.client.GameClient;

/**
 * 前端上下文：在 JavaFX 启动前注入依赖。
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
