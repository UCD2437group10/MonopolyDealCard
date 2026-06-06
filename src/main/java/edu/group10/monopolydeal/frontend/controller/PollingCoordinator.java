package edu.group10.monopolydeal.frontend.controller;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

final class PollingCoordinator {
    private Timeline timeline;
    private boolean polling;

    void start(Duration interval, Runnable tickAction) {
        stop();
        timeline = new Timeline(new KeyFrame(interval, event -> tickAction.run()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

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

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
