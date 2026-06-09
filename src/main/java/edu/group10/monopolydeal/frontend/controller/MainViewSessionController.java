package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.backend.network.server.GameServer;
import edu.group10.monopolydeal.frontend.network.client.GameClient;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javafx.scene.control.Label;

/**
 * Owns room entry, reconnect restore, and local session state.
 */
final class MainViewSessionController {
    private final GameClient client;
    private final MainViewMenuController menuController;
    private final MainViewBotCoordinator botCoordinator;

    private boolean connected;
    private boolean joined;
    private String currentPlayerId = "p1";
    private GameServer hostedServer;

    MainViewSessionController(GameClient client, MainViewMenuController menuController,
                              MainViewBotCoordinator botCoordinator) {
        this.client = client;
        this.menuController = menuController;
        this.botCoordinator = botCoordinator;
    }

    boolean isConnected() {
        return connected;
    }

    boolean joined() {
        return joined;
    }

    String playerId() {
        return nonEmpty(currentPlayerId, "p1");
    }

    // Connect, join the room, and bootstrap single-player bots when needed.
    EnterGameResult enterGame(Set<String> readyPlayers, Consumer<String> actionLogSink, Label reconnectLabel) {
        if (client == null) {
            menuController.setMenuStatus("Error: GameClient is not initialized");
            return EnterGameResult.failed();
        }

        if (connected && joined) {
            return EnterGameResult.restored();
        }

        MainViewMenuController.EntryConfig config = menuController.buildEntryConfig();
        if (config.hostServer()) {
            hostedServer = new GameServer(config.port());
            hostedServer.start();
        }

        try {
            client.connect(config.host(), config.port());
            markConnected(reconnectLabel);
        } catch (Exception exception) {
            handleConnectFailure(exception, menuController.menuStatusLabel());
            return EnterGameResult.failed();
        }

        currentPlayerId = config.playerId();
        GameResponse joinResponse = client.send("JOIN", config.playerId(), Map.of(
                "name", config.playerName(),
                "bot", String.valueOf(config.bot())
        ));
        if (!joinResponse.success()) {
            joined = false;
            menuController.setMenuStatus("Join failed: " + joinResponse.message());
            actionLogSink.accept("Join failed: " + joinResponse.message());
            return EnterGameResult.failed();
        }

        if ("single".equals(menuController.selectedMode())
                && !botCoordinator.enterSingleMode(config.playerId(), menuController::setMenuStatus, readyPlayers)) {
            joined = false;
            return EnterGameResult.failed();
        }

        joined = true;
        return EnterGameResult.entered();
    }

    // Reset local session flags and hosted resources.
    void restoreInitialState() {
        connected = false;
        joined = false;
        currentPlayerId = "p1";
    }

    // Mark the network session as healthy and hide reconnect hints.
    void markConnected(Label reconnectLabel) {
        connected = true;
        if (reconnectLabel != null) {
            reconnectLabel.setText("");
        }
    }

    // Mark the network session as unavailable without clearing room data.
    void markDisconnected() {
        connected = false;
    }

    // Surface connect errors in the menu and clear the local session state.
    void handleConnectFailure(Exception exception, Label menuStatusLabel) {
        connected = false;
        if (menuStatusLabel != null) {
            menuStatusLabel.setText("Connection failed: " + exception.getMessage());
        }
    }

    // Surface polling errors and keep the UI ready for reconnection.
    void handlePollingFailure(Exception exception, Label reconnectLabel) {
        connected = false;
        if (reconnectLabel != null) {
            reconnectLabel.setText("Connection error, retrying: " + exception.getMessage());
        }
    }

    record EnterGameResult(boolean success, boolean restoredSession) {
        static EnterGameResult restored() {
            return new EnterGameResult(true, true);
        }

        static EnterGameResult entered() {
            return new EnterGameResult(true, false);
        }

        static EnterGameResult failed() {
            return new EnterGameResult(false, false);
        }
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
