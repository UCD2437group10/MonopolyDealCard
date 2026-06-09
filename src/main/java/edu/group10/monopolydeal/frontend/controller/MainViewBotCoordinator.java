package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.game.GameState;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.frontend.network.client.GameClient;
import edu.group10.monopolydeal.frontend.view.GameDialogService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.VBox;

/**
 * Manages single-player bot setup and automated bot turns.
 */
final class MainViewBotCoordinator {
    private final GameClient client;
    private final GameDialogService gameDialogService;
    private final VBox gamePane;

    private final Set<String> singleBotIds = new HashSet<>();
    private boolean botPlaying;

    MainViewBotCoordinator(GameClient client, GameDialogService gameDialogService, VBox gamePane) {
        this.client = client;
        this.gameDialogService = gameDialogService;
        this.gamePane = gamePane;
    }

    void clear() {
        singleBotIds.clear();
        botPlaying = false;
    }

    // Join bot players and auto-start a local single-player room.
    boolean enterSingleMode(String humanId, Consumer<String> menuStatusSink, Set<String> readyPlayers) {
        int singleBotCount = chooseSingleBotCount();
        if (singleBotCount <= 0) {
            menuStatusSink.accept("Single-player entry canceled");
            return false;
        }

        singleBotIds.clear();
        for (int i = 1; i <= singleBotCount; i++) {
            String botId = humanId + "_bot" + i;
            GameResponse botJoin = client.send("JOIN", botId, Map.of(
                    "name", "SimpleBot" + i,
                    "bot", "true"
            ));
            if (!botJoin.success()) {
                menuStatusSink.accept("Failed to create BOTs in single player: " + botJoin.message());
                return false;
            }
            singleBotIds.add(botId);
        }

        readyPlayers.add(humanId);
        GameResponse readyResp = client.send("READY", humanId, Map.of());
        if (!readyResp.success()) {
            menuStatusSink.accept("Single-player ready failed: " + readyResp.message());
            return false;
        }

        GameResponse startResp = client.send("START", humanId, Map.of());
        if (!startResp.success()) {
            menuStatusSink.accept("Single-player start failed: " + startResp.message());
            return false;
        }
        return true;
    }

    // Trigger the backend bot turn when the active player is one of our local bots.
    void maybeRunBotTurn(String selectedMode, GameState currentState, Consumer<GameResponse> responseConsumer) {
        if (!"single".equals(selectedMode) || botPlaying || currentState == null
                || !currentState.started() || currentState.gameOver()) {
            return;
        }
        String currentId = currentState.currentPlayerId();
        if (!singleBotIds.contains(currentId)) {
            return;
        }
        botPlaying = true;
        try {
            GameResponse botResp = client.send("BOT_TURN", currentId, Map.of());
            responseConsumer.accept(botResp);
        } finally {
            botPlaying = false;
        }
    }

    private int chooseSingleBotCount() {
        List<Integer> options = List.of(1, 2, 3, 4);
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(1, options);
        gameDialogService.styleDialog(dialog, gamePane);
        dialog.setTitle("Single Player BOT Count");
        dialog.setHeaderText("Select number of BOTs to add (1-4)");
        dialog.setContentText("BOT count:");
        return dialog.showAndWait().orElse(0);
    }
}
