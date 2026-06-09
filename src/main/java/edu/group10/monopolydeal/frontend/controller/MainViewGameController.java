package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.game.GameState;
import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.backend.service.CardPropertyRules;
import edu.group10.monopolydeal.frontend.network.client.GameClient;
import edu.group10.monopolydeal.frontend.view.AudioFeedbackService;
import edu.group10.monopolydeal.frontend.view.GameDialogService;
import edu.group10.monopolydeal.frontend.viewmodel.GameViewModel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
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
 * Handles in-game actions and game-driven interaction flows.
 */
final class MainViewGameController {
    private final GameClient client;
    private final MainViewUiSynchronizer uiSynchronizer;
    private final MainViewHandController handController;
    private final MainViewBoardCoordinator boardCoordinator;
    private final MainViewEffects effects;
    private final GameDialogService gameDialogService;
    private final AudioFeedbackService audioFeedbackService;
    private final ActionPayloadBuilder actionPayloadBuilder;
    private final GameViewModel gameViewModel;
    private final VBox gamePane;
    private final Label statusLabel;
    private final Label menuStatusLabel;
    private final BooleanSupplier connectedSupplier;
    private final BooleanSupplier joinedSupplier;
    private final Supplier<String> playerIdSupplier;
    private final Supplier<String> selectedModeSupplier;
    private final Runnable restoreInitialMenuState;
    private final Runnable backToMenu;
    private final Runnable botTurnRunner;

    private boolean gameOverHandled;
    private boolean jsnPromptShowing;

    MainViewGameController(GameClient client, MainViewUiSynchronizer uiSynchronizer,
                           MainViewHandController handController, MainViewBoardCoordinator boardCoordinator,
                           MainViewEffects effects, GameDialogService gameDialogService,
                           AudioFeedbackService audioFeedbackService, ActionPayloadBuilder actionPayloadBuilder,
                           GameViewModel gameViewModel, VBox gamePane, Label statusLabel,
                           Label menuStatusLabel, BooleanSupplier connectedSupplier,
                           BooleanSupplier joinedSupplier, Supplier<String> playerIdSupplier,
                           Supplier<String> selectedModeSupplier, Runnable restoreInitialMenuState,
                           Runnable backToMenu, Runnable botTurnRunner) {
        this.client = client;
        this.uiSynchronizer = uiSynchronizer;
        this.handController = handController;
        this.boardCoordinator = boardCoordinator;
        this.effects = effects;
        this.gameDialogService = gameDialogService;
        this.audioFeedbackService = audioFeedbackService;
        this.actionPayloadBuilder = actionPayloadBuilder;
        this.gameViewModel = gameViewModel;
        this.gamePane = gamePane;
        this.statusLabel = statusLabel;
        this.menuStatusLabel = menuStatusLabel;
        this.connectedSupplier = connectedSupplier;
        this.joinedSupplier = joinedSupplier;
        this.playerIdSupplier = playerIdSupplier;
        this.selectedModeSupplier = selectedModeSupplier;
        this.restoreInitialMenuState = restoreInitialMenuState;
        this.backToMenu = backToMenu;
        this.botTurnRunner = botTurnRunner;
    }

    // Clear transient dialog and end-state flags for a fresh local flow.
    void resetLocalFlowState() {
        gameOverHandled = false;
        jsnPromptShowing = false;
    }

    // Toggle the local player's ready state in the current room.
    void onReadyToggle() {
        if ("single".equals(selectedModeSupplier.get())) {
            return;
        }
        String action = uiSynchronizer.readyPlayers().contains(playerIdSupplier.get()) ? "UNREADY" : "READY";
        renderAndHandle(client.send(action, playerIdSupplier.get(), Map.of()), true);
    }

    // Ask the host to start the match once everyone is ready.
    void onStart() {
        if ("single".equals(selectedModeSupplier.get())) {
            statusLabel.setText("Single-player starts automatically");
            return;
        }
        if (!gameViewModel.allHumanReady()) {
            statusLabel.setText("Please make all non-BOT players ready first");
            return;
        }
        renderAndHandle(client.send("START", playerIdSupplier.get(), Map.of()), true);
    }

    void onRefresh() {
        renderAndHandle(client.send("STATE", playerIdSupplier.get(), Map.of()), true);
    }

    void onResetGame() {
        if (!connectedSupplier.getAsBoolean() || !joinedSupplier.getAsBoolean()) {
            menuStatusLabel.setText("Please enter a room before resetting the game");
            return;
        }

        GameResponse resetResponse = client.send("RESET", playerIdSupplier.get(), Map.of());
        if (!resetResponse.success()) {
            if (uiSynchronizer.currentState() != null) {
                renderAndHandle(resetResponse, true);
            } else {
                menuStatusLabel.setText("Reset failed: " + resetResponse.message());
            }
            return;
        }

        uiSynchronizer.clearLocalGameState();
        restoreInitialMenuState.run();
        menuStatusLabel.setText("Game reset to startup state");
        uiSynchronizer.appendActionLog("Game reset to startup state");
    }

    void onEndTurn() {
        renderAndHandle(client.send("END_TURN", playerIdSupplier.get(), Map.of()), true);
    }

    void onPlayMoney() {
        if (!ensureCardSelected()) {
            return;
        }
        playCardWithCenterAnimation(
                () -> client.send("PLAY_MONEY", playerIdSupplier.get(), Map.of("handIndex", String.valueOf(handController.selectedHandIndex()))),
                true);
    }

    void onPlayProperty() {
        if (!ensureCardSelected()) {
            return;
        }
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("handIndex", String.valueOf(handController.selectedHandIndex()));
        Card selectedCard = handController.selectedCard();
        String colorChoice = gameDialogService.choosePropertyColor(selectedCard, gamePane);
        if (selectedCard != null && selectedCard.type() == CardType.MULTI_PROPERTY && colorChoice == null) {
            statusLabel.setText("Property play canceled");
            return;
        }
        putIfNotBlank(payload, "colorChoice", colorChoice);
        playCardWithCenterAnimation(() -> client.send("PLAY_PROPERTY", playerIdSupplier.get(), payload), true);
    }

    void onChangePropertyColor() {
        if (!gameViewModel.isMyTurn()) {
            statusLabel.setText("You can only change property color during your own turn");
            return;
        }
        PlayerState me = gameViewModel.findMe();
        if (me == null || me.properties().isEmpty()) {
            statusLabel.setText("You have no properties to adjust");
            return;
        }

        List<RecolorCandidate> candidates = me.properties().entrySet().stream()
                .flatMap(entry -> {
                    String fromColor = entry.getKey();
                    List<Card> cards = entry.getValue();
                    return cards.stream()
                            .filter(card -> card.type() == CardType.MULTI_PROPERTY)
                            .map(card -> new RecolorCandidate(fromColor, cards.indexOf(card), card));
                })
                .toList();
        if (candidates.isEmpty()) {
            statusLabel.setText("You have no dual-color or wild property to recolor");
            return;
        }

        int candidateIndex;
        try {
            List<String> options = candidates.stream()
                    .map(candidate -> buildRecolorOptionText(candidate.fromColor(), candidate.card()))
                    .toList();
            candidateIndex = gameDialogService.chooseOptionIndex(
                    "Select which color-changing property to adjust",
                    "Select Property",
                    options,
                    gamePane);
        } catch (IllegalStateException exception) {
            statusLabel.setText(exception.getMessage());
            return;
        }
        RecolorCandidate candidate = candidates.get(candidateIndex);
        String colorChoice = gameDialogService.choosePropertyColorForMove(candidate.card(), candidate.fromColor(), gamePane);
        if (colorChoice == null) {
            statusLabel.setText("Property color change canceled");
            return;
        }

        renderAndHandle(client.send("CHANGE_PROPERTY_COLOR", playerIdSupplier.get(), Map.of(
                "fromColor", candidate.fromColor(),
                "propertyIndex", String.valueOf(candidate.propertyIndex()),
                "colorChoice", colorChoice
        )), true);
    }

    void onPlayRent() {
        if (!ensureCardSelected()) {
            return;
        }
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("handIndex", String.valueOf(handController.selectedHandIndex()));
        Card selectedCard = handController.selectedCard();
        String colorChoice = gameDialogService.chooseRentColor(selectedCard, gameViewModel.findById(playerIdSupplier.get()), gamePane);
        boolean rentNeedsColorChoice = selectedCard != null
                && selectedCard.color() != null
                && ("Any".equalsIgnoreCase(selectedCard.color()) || selectedCard.color().contains("/"));
        if (rentNeedsColorChoice && colorChoice == null) {
            statusLabel.setText("Rent play canceled");
            return;
        }
        putIfNotBlank(payload, "colorChoice", colorChoice);
        String doubleRentCount = gameDialogService.chooseDoubleRentCount(gamePane);
        if (doubleRentCount == null) {
            statusLabel.setText("Rent play canceled");
            return;
        }
        payload.put("doubleRentCount", doubleRentCount);
        boolean allOpponentsRent = selectedCard != null
                && selectedCard.color() != null
                && selectedCard.color().contains("/");
        if (!allOpponentsRent) {
            String targetPlayerId = boardCoordinator.chooseTargetPlayerId(uiSynchronizer.currentState(), playerIdSupplier.get(), statusLabel);
            if (targetPlayerId == null) {
                return;
            }
            payload.put("targetPlayerId", targetPlayerId);
        }
        playCardWithCenterAnimation(() -> client.send("PLAY_RENT", playerIdSupplier.get(), payload), true);
    }

    void onPlayAction() {
        if (!ensureCardSelected()) {
            return;
        }
        Map<String, String> payload = actionPayloadBuilder.build(
                handController.selectedHandIndex(),
                handController.selectedCard(),
                () -> boardCoordinator.chooseTargetPlayerId(uiSynchronizer.currentState(), playerIdSupplier.get(), statusLabel),
                gameViewModel::findById,
                gameViewModel::findMe,
                new ActionPayloadBuilder.DialogOps() {
                    @Override
                    public String chooseColor(String title, List<String> colors) {
                        return gameDialogService.chooseColorFromList(title, colors, gamePane);
                    }

                    @Override
                    public int choosePropertyIndex(String title, List<Card> cards) {
                        return gameDialogService.choosePropertyIndex(title, cards, gamePane);
                    }
                },
                gameViewModel::requiredSetSize,
                statusLabel::setText);
        if (payload == null) {
            return;
        }
        playCardWithCenterAnimation(() -> client.send("PLAY_ACTION", playerIdSupplier.get(), payload), true);
    }

    void onShowDrawPile() {
        effects.showDrawPile(uiSynchronizer.currentState());
    }

    void onShowDiscardPile() {
        effects.showDiscardPile(uiSynchronizer.currentState());
    }

    // Run any follow-up UI flow required by the latest synced state.
    boolean handleStateDrivenInteractions() {
        if (maybeHandleJsnPrompt()) {
            return true;
        }
        if (maybeHandleGameOver()) {
            return true;
        }
        return false;
    }

    private boolean ensureCardSelected() {
        return handController.ensureCardSelected(statusLabel);
    }

    private void playCardWithCenterAnimation(Supplier<GameResponse> action, boolean playSoundOnSuccess) {
        effects.playCardWithCenterAnimation(
                handController.selectedHandButton(),
                handController.selectedCard(),
                action,
                response -> renderAndHandle(response, true),
                playSoundOnSuccess);
    }

    // Sync the response into the view model, then handle any follow-up prompts.
    private void renderAndHandle(GameResponse response, boolean showStatus) {
        uiSynchronizer.renderResponse(response, showStatus);
        if (!handleStateDrivenInteractions()) {
            botTurnRunner.run();
        }
    }

    // Show a Just Say No prompt when the backend is waiting on this player.
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
                renderAndHandle(client.send("RESPOND_JSN", playerIdSupplier.get(), Map.of("useCard", String.valueOf(useCard))), true);
            } catch (Exception exception) {
                uiSynchronizer.appendActionLog("JSN response failed: " + exception.getMessage());
            } finally {
                jsnPromptShowing = false;
            }
        });
        return true;
    }

    // Show the winner dialog once and route the UI back to the menu if needed.
    private boolean maybeHandleGameOver() {
        GameState currentState = uiSynchronizer.currentState();
        if (currentState == null || !currentState.gameOver() || gameOverHandled) {
            return false;
        }
        gameOverHandled = true;
        final String winnerId = nonEmpty(currentState.winnerPlayerId(), "Unknown player");
        final boolean host = isHostPlayer(currentState);
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

    private String buildRecolorOptionText(String currentColor, Card card) {
        if (card == null) {
            return currentColor;
        }
        if ("Wild".equalsIgnoreCase(card.color())) {
            return currentColor + " -> any other color";
        }
        List<String> targets = CardPropertyRules.allowedPropertyColors(card).stream()
                .filter(color -> !color.equals(currentColor))
                .toList();
        if (targets.isEmpty()) {
            return currentColor;
        }
        return currentColor + " -> " + String.join(" / ", targets);
    }

    private void putIfNotBlank(Map<String, String> payload, String key, String value) {
        if (value != null && !value.isBlank()) {
            payload.put(key, value.trim());
        }
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private record RecolorCandidate(String fromColor, int propertyIndex, Card card) {
    }
}
