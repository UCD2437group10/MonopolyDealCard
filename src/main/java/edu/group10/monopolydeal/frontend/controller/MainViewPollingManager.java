package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.frontend.network.client.GameClient;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Owns automatic state polling for the main view and guards against overlap.
 */
final class MainViewPollingManager {
    private final MainViewSessionController sessionController;
    private final Label reconnectLabel;
    private final GameClient client;
    private final Supplier<String> playerIdSupplier;
    private final BooleanSupplier joinedSupplier;
    private final BooleanSupplier gameVisibleSupplier;
    private final Consumer<GameResponse> responseConsumer;

    private Timeline timeline;
    private boolean polling;

    MainViewPollingManager(MainViewSessionController sessionController, Label reconnectLabel,
                           GameClient client, Supplier<String> playerIdSupplier,
                           BooleanSupplier joinedSupplier, BooleanSupplier gameVisibleSupplier,
                           Consumer<GameResponse> responseConsumer) {
        this.sessionController = sessionController;
        this.reconnectLabel = reconnectLabel;
        this.client = client;
        this.playerIdSupplier = playerIdSupplier;
        this.joinedSupplier = joinedSupplier;
        this.gameVisibleSupplier = gameVisibleSupplier;
        this.responseConsumer = responseConsumer;
    }

    // Start the periodic state refresh loop for the main game view.
    void start() {
        stop();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> autoRefresh()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    // Skip polling unless the session and game view are ready for a refresh.
    private void autoRefresh() {
        if (!sessionController.isConnected() || !joinedSupplier.getAsBoolean() || !gameVisibleSupplier.getAsBoolean() || polling) {
            return;
        }
        polling = true;
        try {
            GameResponse response = client.send("STATE", playerIdSupplier.get(), Map.of());
            sessionController.markConnected(reconnectLabel);
            responseConsumer.accept(response);
        } catch (Exception exception) {
            sessionController.handlePollingFailure(exception, reconnectLabel);
        } finally {
            polling = false;
        }
    }
}
