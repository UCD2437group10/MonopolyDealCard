package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.game.GameState;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.frontend.network.client.GameClient;
import edu.group10.monopolydeal.frontend.view.AudioFeedbackService;
import edu.group10.monopolydeal.frontend.view.GameDialogService;
import edu.group10.monopolydeal.frontend.view.PaymentSelectionDialog;
import edu.group10.monopolydeal.frontend.viewmodel.GameViewModel;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Owns state-driven popups such as pending payment, Just Say No, and game over.
 */
final class MainViewInteractionCoordinator {
    private final GameClient client;
    private final MainViewUiSynchronizer uiSynchronizer;
    private final PaymentSelectionDialog paymentSelectionDialog;
    private final GameDialogService gameDialogService;
    private final AudioFeedbackService audioFeedbackService;
    private final GameViewModel gameViewModel;
    private final VBox gamePane;
    private final Label statusLabel;
    private final Supplier<String> playerIdSupplier;
    private final Runnable restoreInitialMenuState;
    private final Runnable backToMenu;
    private final Consumer<GameResponse> responseHandler;

    private boolean gameOverHandled;
    private boolean jsnPromptShowing;
    private boolean paymentPromptShowing;

    MainViewInteractionCoordinator(GameClient client,
                                   MainViewUiSynchronizer uiSynchronizer,
                                   PaymentSelectionDialog paymentSelectionDialog,
                                   GameDialogService gameDialogService,
                                   AudioFeedbackService audioFeedbackService,
                                   GameViewModel gameViewModel,
                                   VBox gamePane,
                                   Label statusLabel,
                                   Supplier<String> playerIdSupplier,
                                   Runnable restoreInitialMenuState,
                                   Runnable backToMenu,
                                   Consumer<GameResponse> responseHandler) {
        this.client = client;
        this.uiSynchronizer = uiSynchronizer;
        this.paymentSelectionDialog = paymentSelectionDialog;
        this.gameDialogService = gameDialogService;
        this.audioFeedbackService = audioFeedbackService;
        this.gameViewModel = gameViewModel;
        this.gamePane = gamePane;
        this.statusLabel = statusLabel;
        this.playerIdSupplier = playerIdSupplier;
        this.restoreInitialMenuState = restoreInitialMenuState;
        this.backToMenu = backToMenu;
        this.responseHandler = responseHandler;
    }

    void resetLocalFlowState() {
        gameOverHandled = false;
        jsnPromptShowing = false;
        paymentPromptShowing = false;
    }

    boolean handleStateDrivenInteractions() {
        if (maybeHandlePendingPayment()) {
            return true;
        }
        if (maybeHandleJsnPrompt()) {
            return true;
        }
        if (maybeHandleGameOver()) {
            return true;
        }
        return false;
    }

    private boolean maybeHandlePendingPayment() {
        GameState currentState = uiSynchronizer.currentState();
        if (paymentPromptShowing || currentState == null) {
            return false;
        }
        String payerId = nonEmpty(currentState.pendingPaymentPayerPlayerId(), "");
        if (!playerIdSupplier.get().equals(payerId)) {
            return false;
        }
        PlayerState payer = gameViewModel.findById(payerId);
        if (payer == null) {
            return false;
        }
        paymentPromptShowing = true;
        Platform.runLater(() -> {
            try {
                Map<String, String> payload = paymentSelectionDialog.show(
                        payer,
                        nonEmpty(currentState.pendingPaymentCollectorPlayerId(), "Unknown"),
                        currentState.pendingPaymentAmount(),
                        nonEmpty(currentState.pendingPaymentSourceAction(), "Payment"),
                        gamePane
                );
                if (payload == null) {
                    statusLabel.setText("Payment selection canceled");
                    return;
                }
                responseHandler.accept(client.send("SUBMIT_PAYMENT", playerIdSupplier.get(), payload));
            } catch (Exception exception) {
                uiSynchronizer.appendActionLog("Payment selection failed: " + exception.getMessage());
            } finally {
                paymentPromptShowing = false;
            }
        });
        return true;
    }

    private boolean maybeHandleJsnPrompt() {
        GameState currentState = uiSynchronizer.currentState();
        if (jsnPromptShowing || currentState == null) {
            return false;
        }
        String responder = nonEmpty(currentState.jsnResponderPlayerId(), "");
        if (!playerIdSupplier.get().equals(responder)) {
            return false;
        }
        jsnPromptShowing = true;
        Platform.runLater(() -> {
            try {
                ChoiceDialog<String> dialog = new ChoiceDialog<>("No", List.of("No", "Yes"));
                gameDialogService.styleDialog(dialog, gamePane);
                dialog.setTitle("Just Say No");
                dialog.setHeaderText("Action card: " + nonEmpty(currentState.jsnSourceAction(), "Unknown") + " targets you. Use Just Say No?");
                Timeline timeout = new Timeline(new KeyFrame(Duration.seconds(10), event -> dialog.setResult("No")));
                timeout.setCycleCount(1);
                timeout.play();
                String result = dialog.showAndWait().orElse("No");
                timeout.stop();
                boolean useCard = "Yes".equals(result);
                responseHandler.accept(client.send("RESPOND_JSN", playerIdSupplier.get(), Map.of("useCard", String.valueOf(useCard))));
            } catch (Exception exception) {
                uiSynchronizer.appendActionLog("JSN response failed: " + exception.getMessage());
            } finally {
                jsnPromptShowing = false;
            }
        });
        return true;
    }

    private boolean maybeHandleGameOver() {
        GameState currentState = uiSynchronizer.currentState();
        if (currentState == null || !currentState.gameOver() || gameOverHandled) {
            return false;
        }
        gameOverHandled = true;
        String winnerId = nonEmpty(currentState.winnerPlayerId(), "Unknown player");
        boolean host = isHostPlayer(currentState);
        Platform.runLater(() -> {
            try {
                audioFeedbackService.playVictory();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                gameDialogService.styleDialog(alert, gamePane);
                alert.setTitle("Game Over");
                alert.setHeaderText("Winner");
                alert.setContentText(winnerId + " wins");
                alert.showAndWait();
                if (host) {
                    client.send("RESET", playerIdSupplier.get(), Map.of());
                }
            } catch (Exception exception) {
                uiSynchronizer.appendActionLog("Game-over handling failed: " + exception.getMessage());
            } finally {
                uiSynchronizer.clearLocalGameState();
                restoreInitialMenuState.run();
                backToMenu.run();
            }
        });
        return true;
    }

    private boolean isHostPlayer(GameState currentState) {
        if (currentState.players() == null || currentState.players().isEmpty()) {
            return false;
        }
        String hostId = currentState.players().get(0).player().id();
        return hostId != null && hostId.equals(playerIdSupplier.get());
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
