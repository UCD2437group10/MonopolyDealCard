package edu.group10.monopolydeal.frontend.controller;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Owns the repeating UI polling loop and guards against overlap.
 */
final class PollingCoordinator {
    private Timeline timeline;
    private boolean polling;

    /** Starts polling with the requested interval. */
    void start(Duration interval, Runnable tickAction) {
        stop();
        timeline = new Timeline(new KeyFrame(interval, event -> tickAction.run()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /** Stops the polling timeline if one is active. */
    void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    /** Runs one poll tick only when the precondition allows it. */
    void tryPoll(BooleanSupplier precondition, ThrowingRunnable action, Consumer<Exception> onError) {
        if (!precondition.getAsBoolean() || polling) {
            return;
        }
        polling = true;
        try {
            action.run();
        } catch (Exception exception) {
            onError.accept(exception);
        } finally {
            polling = false;
        }
    }

    /** Functional interface for polling work that may throw. */
    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
