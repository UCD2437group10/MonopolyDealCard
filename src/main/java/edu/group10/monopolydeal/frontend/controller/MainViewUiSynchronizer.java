package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.game.GameState;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.frontend.viewmodel.GameViewModel;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Applies backend responses to local state and refreshes the visible game UI.
 */
final class MainViewUiSynchronizer {
    private final GameViewModel gameViewModel;
    private final MainViewHandController handController;
    private final MainViewBoardCoordinator boardCoordinator;
    private final MainViewEffects effects;
    private final Label statusLabel;
    private final Label reconnectLabel;
    private final Label turnLabel;
    private final Label selectedCardLabel;
    private final TextArea actionLogArea;
    private final Button readyButton;
    private final Runnable disconnectAction;
    private final Supplier<String> playerIdSupplier;
    private final Runnable applyModeUi;
    private final Runnable clearAuxState;

    private final Set<String> readyPlayers = new HashSet<>();
    private GameState currentState;

    MainViewUiSynchronizer(GameViewModel gameViewModel, MainViewHandController handController,
                           MainViewBoardCoordinator boardCoordinator, MainViewEffects effects,
                           Label statusLabel, Label reconnectLabel, Label turnLabel,
                           Label selectedCardLabel, TextArea actionLogArea, Button readyButton,
                           Runnable disconnectAction, Supplier<String> playerIdSupplier,
                           Runnable applyModeUi, Runnable clearAuxState) {
        this.gameViewModel = gameViewModel;
        this.handController = handController;
        this.boardCoordinator = boardCoordinator;
        this.effects = effects;
        this.statusLabel = statusLabel;
        this.reconnectLabel = reconnectLabel;
        this.turnLabel = turnLabel;
        this.selectedCardLabel = selectedCardLabel;
        this.actionLogArea = actionLogArea;
        this.readyButton = readyButton;
        this.disconnectAction = disconnectAction;
        this.playerIdSupplier = playerIdSupplier;
        this.applyModeUi = applyModeUi;
        this.clearAuxState = clearAuxState;
    }

    GameState currentState() {
        return currentState;
    }

    Set<String> readyPlayers() {
        return readyPlayers;
    }

    // Apply the latest server response to local state and visible controls.
    void renderResponse(GameResponse response, boolean showStatus) {
        GameState previousState = currentState;
        if (showStatus) {
            String line = (response.success() ? "Success: " : "Failed: ") + response.message();
            statusLabel.setText(line);
            appendActionLog(line);
        }
        currentState = response.gameState();
        readyPlayers.clear();
        if (currentState != null && currentState.readyPlayerIds() != null) {
            readyPlayers.addAll(currentState.readyPlayerIds());
        }
        gameViewModel.update(currentState, readyPlayers, playerIdSupplier.get());
        readyButton.setText(readyPlayers.contains(playerIdSupplier.get()) ? "Unready" : "Ready");
        applyModeUi.run();
        syncView(previousState);
    }

    // Clear game-only state after disconnects, resets, or returns to the menu.
    void clearLocalGameState() {
        currentState = null;
        readyPlayers.clear();
        clearAuxState.run();
        selectedCardLabel.setText("Selected card: none");
        turnLabel.setText("Current turn: -");
        statusLabel.setText("Returned to main menu");
        reconnectLabel.setText("");
        disconnectAction.run();
        actionLogArea.clear();
        handController.clear();
        boardCoordinator.clear();
        updateActionDisabled(true);
    }

    // Enable or disable play actions based on the latest state snapshot.
    void updateActionDisabled(boolean disabled) {
        if (boardCoordinator == null) {
            return;
        }
        if (disabled) {
            boardCoordinator.setActionDisabled(true);
            return;
        }
        boardCoordinator.updateTurnAndDisable();
    }

    // Rebuild action controls after the selected hand card changes.
    void updateActionFormBySelectedCard() {
        if (boardCoordinator != null) {
            boardCoordinator.updateActionFormBySelectedCard(handController.selectedCard());
        }
    }

    void appendActionLog(String line) {
        if (actionLogArea == null || line == null || line.isBlank()) {
            return;
        }
        String old = actionLogArea.getText();
        if (old.length() > 6000) {
            old = old.substring(old.length() - 4000);
        }
        actionLogArea.setText(old + (old.isEmpty() ? "" : "\n") + line);
        actionLogArea.positionCaret(actionLogArea.getText().length());
    }

    private void syncView(GameState previousState) {
        handController.render(gameViewModel, this::updateActionFormBySelectedCard);
        boardCoordinator.renderBoard(currentState, playerIdSupplier.get());
        effects.updatePileButtons(currentState);
        boardCoordinator.updateTurnAndDisable();
        boardCoordinator.updateActionFormBySelectedCard(handController.selectedCard());
        effects.maybeAnimateDraws(previousState, currentState, playerIdSupplier.get());
    }
}
