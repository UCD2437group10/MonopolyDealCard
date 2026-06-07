package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.game.GameState;
import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.backend.network.server.GameServer;
import edu.group10.monopolydeal.backend.service.CardPropertyRules;
import edu.group10.monopolydeal.frontend.context.FrontendContext;
import edu.group10.monopolydeal.frontend.network.client.GameClient;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import edu.group10.monopolydeal.frontend.view.AudioFeedbackService;
import edu.group10.monopolydeal.frontend.view.GameBoardView;
import edu.group10.monopolydeal.frontend.view.GameDialogService;
import edu.group10.monopolydeal.frontend.viewmodel.GameViewModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controls the main menu and the in-game JavaFX screen.
 */
public class MainViewController {
    @FXML private VBox menuPane;
    @FXML private VBox gamePane;
    @FXML private VBox multiConfigPane;

    @FXML private Button singleModeButton;
    @FXML private Button multiModeButton;
    @FXML private Label menuStatusLabel;

    @FXML private TextField menuHostField;
    @FXML private TextField menuPortField;
    @FXML private TextField menuPlayerIdField;
    @FXML private TextField menuPlayerNameField;
    @FXML private CheckBox menuBotCheckBox;
    @FXML private CheckBox menuHostServerCheckBox;
    @FXML private Slider menuVolumeSlider;
    @FXML private Label menuVolumeLabel;

    @FXML private Label statusLabel;
    @FXML private Label reconnectLabel;
    @FXML private Label turnLabel;
    @FXML private Label selectedCardLabel;

    @FXML private Button readyButton;
    @FXML private Button startButton;
    @FXML private ScrollPane myHandScrollPane;
    @FXML private HBox myHandPane;
    @FXML private FlowPane myBankPane;
    @FXML private FlowPane opponentsPane;
    @FXML private VBox myPropertyBox;
    @FXML private StackPane centerBoardPane;
    @FXML private Button drawPileButton;
    @FXML private Button discardPileButton;
    @FXML private TextArea playersTextArea;
    @FXML private TextArea actionLogArea;

    @FXML private Button endTurnButton;
    @FXML private Button playMoneyButton;
    @FXML private Button playPropertyButton;
    @FXML private Button changePropertyColorButton;
    @FXML private Button playRentButton;
    @FXML private Button playActionButton;

    /** Shared client used for all frontend requests. */
    private GameClient client;
    /** Latest snapshot returned by the backend. */
    private GameState currentState;
    private boolean connected;
    private boolean joined;
    private boolean botPlaying;
    private boolean gameOverHandled;
    private boolean jsnPromptShowing;
    private String selectedMode = "single";
    private int singleBotCount = 1;
    private final Set<String> singleBotIds = new HashSet<>();

    private String currentPlayerId = "p1";
    private GameServer hostedServer;
    /** Helper services used to render and coordinate the UI. */
    private final GameBoardView gameBoardView = new GameBoardView();
    private final GameViewModel gameViewModel = new GameViewModel();
    private final GameDialogService gameDialogService = new GameDialogService();
    private final AudioFeedbackService audioFeedbackService = new AudioFeedbackService();
    private final ActionPayloadBuilder actionPayloadBuilder = new ActionPayloadBuilder();
    private final NetworkErrorHandler networkErrorHandler = new NetworkErrorHandler();
    private final PollingCoordinator pollingCoordinator = new PollingCoordinator();
    private MainViewHandController handController;
    private MainViewBoardCoordinator boardCoordinator;
    private MainViewEffects effects;

    private final Set<String> readyPlayers = new HashSet<>();

    /** Initializes the controller after the FXML tree is loaded. */
    @FXML
    public void initialize() {
        this.client = FrontendContext.gameClient();
        if (this.client == null) {
            menuStatusLabel.setText("Error: GameClient is not initialized");
            return;
        }
        handController = new MainViewHandController(gameBoardView, audioFeedbackService, myHandPane, selectedCardLabel);
        boardCoordinator = new MainViewBoardCoordinator(
                gameBoardView,
                gameViewModel,
                gameDialogService,
                myBankPane,
                opponentsPane,
                myPropertyBox,
                gamePane,
                playersTextArea,
                turnLabel,
                endTurnButton,
                playMoneyButton,
                playPropertyButton,
                changePropertyColorButton,
                playRentButton,
                playActionButton);
        effects = new MainViewEffects(
                gameDialogService,
                audioFeedbackService,
                gamePane,
                centerBoardPane,
                drawPileButton,
                discardPileButton,
                opponentsPane,
                myHandPane);
        effects.setupDeckPileUI();
        setupRightLogAreaStyle();
        setupHandPaneUI();
        setupVolumeControl();
        updateActionDisabled(true);
        applyModeUI();
        startPolling();
    }

    private void setupVolumeControl() {
        if (menuVolumeSlider == null || menuVolumeLabel == null) {
            return;
        }
        audioFeedbackService.setVolume(menuVolumeSlider.getValue() / 100.0);
        updateVolumeLabel();
        menuVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            audioFeedbackService.setVolume(newValue.doubleValue() / 100.0);
            updateVolumeLabel();
        });
    }

    private void updateVolumeLabel() {
        if (menuVolumeSlider == null || menuVolumeLabel == null) {
            return;
        }
        menuVolumeLabel.setText(Math.round(menuVolumeSlider.getValue()) + "%");
    }

    private void setupRightLogAreaStyle() {
        styleLogTextArea(actionLogArea);
        styleLogTextArea(playersTextArea);
    }

    private void setupHandPaneUI() {
        if (myHandScrollPane != null) {
            myHandScrollPane.setPannable(true);
            myHandScrollPane.setFitToWidth(false);
            myHandScrollPane.setFitToHeight(true);
        }
    }

    private void styleLogTextArea(TextArea area) {
        if (area == null) {
            return;
        }
        area.setStyle("-fx-control-inner-background: #15120d;"
                + "-fx-background-color: #15120d;"
                + "-fx-text-fill: #e3cf8a;"
                + "-fx-highlight-fill: #8b6e26;"
                + "-fx-highlight-text-fill: #fff4cf;");
    }

    private void startPolling() {
        pollingCoordinator.start(Duration.seconds(1), this::autoRefresh);
    }

    private void autoRefresh() {
        pollingCoordinator.tryPoll(
                () -> networkErrorHandler.isConnected() && joined && gamePane.isVisible(),
                () -> {
                    GameResponse response = client.send("STATE", playerId(), Map.of());
                    networkErrorHandler.markConnected(reconnectLabel);
                    connected = networkErrorHandler.isConnected();
                    renderResponse(response, false);
                    maybeRunBotTurn();
                },
                exception -> {
                    networkErrorHandler.handlePollingFailure(exception, reconnectLabel);
                    connected = networkErrorHandler.isConnected();
                });
    }

    @FXML
    private void onChooseSingle() {
        selectedMode = "single";
        applyModeUI();
        menuStatusLabel.setText("Selected: Single Player (default local 127.0.0.1:18080)");
    }

    @FXML
    private void onChooseMulti() {
        selectedMode = "multi";
        applyModeUI();
        menuStatusLabel.setText("Selected: Multiplayer (host locally or join as client)");
    }

    /** Enters the selected mode and joins or restores a room. */
    @FXML
    private void onEnterGame() {
        if (client == null) {
            menuStatusLabel.setText("Error: GameClient is not initialized");
            return;
        }

        if (connected && joined) {
            showGamePane();
            renderResponse(client.send("STATE", playerId(), Map.of()), true);
            return;
        }

        String host = "127.0.0.1";
        int port = 18080;
        String pid = "p1";
        String name = "Player1";
        boolean bot = false;

        if ("multi".equals(selectedMode)) {
            host = nonEmpty(menuHostField.getText(), "127.0.0.1");
            port = parseInt(menuPortField.getText(), 18080);
            pid = nonEmpty(menuPlayerIdField.getText(), "p1");
            name = nonEmpty(menuPlayerNameField.getText(), pid);
            bot = menuBotCheckBox.isSelected();
            boolean asServer = menuHostServerCheckBox != null && menuHostServerCheckBox.isSelected();
            if (asServer) {
                hostedServer = new GameServer(port);
                hostedServer.start();
            }
        }

        try {
            client.connect(host, port);
            networkErrorHandler.markConnected(reconnectLabel);
            connected = networkErrorHandler.isConnected();
        } catch (Exception exception) {
            networkErrorHandler.handleConnectFailure(exception, menuStatusLabel);
            connected = networkErrorHandler.isConnected();
            return;
        }

        this.currentPlayerId = pid;
        GameResponse joinResponse = client.send("JOIN", pid, Map.of(
                "name", name,
                "bot", String.valueOf(bot)
        ));
        if (!joinResponse.success()) {
            joined = false;
            menuStatusLabel.setText("Join failed: " + joinResponse.message());
            appendActionLog("Join failed: " + joinResponse.message());
            return;
        }

        if ("single".equals(selectedMode)) {
            singleBotCount = chooseSingleBotCount();
            if (singleBotCount <= 0) {
                menuStatusLabel.setText("Single-player entry canceled");
                joined = false;
                return;
            }
            if (!enterSingleMode(pid, singleBotCount)) {
                return;
            }
        }

        joined = true;
        showGamePane();
        renderResponse(client.send("STATE", playerId(), Map.of()), true);
    }

    @FXML
    private void onBackToMenu() {
        menuPane.setVisible(true);
        menuPane.setManaged(true);
        gamePane.setVisible(false);
        gamePane.setManaged(false);
        menuStatusLabel.setText("Returned to lobby");
    }

    @FXML
    private void onEndGame() {
        try {
            if ("single".equals(selectedMode) && connected && joined && client != null) {
                client.send("RESET", playerId(), Map.of());
            }
        } catch (Exception ignored) {
            // Best-effort cleanup before exiting the application.
        } finally {
            Platform.exit();
            System.exit(0);
        }
    }

    private void clearLocalGameState() {
        currentState = null;
        joined = false;
        botPlaying = false;
        gameOverHandled = false;
        readyPlayers.clear();
        singleBotIds.clear();
        selectedCardLabel.setText("Selected card: none");
        turnLabel.setText("Current turn: -");
        statusLabel.setText("Returned to main menu");
        reconnectLabel.setText("");
        networkErrorHandler.markDisconnected();
        connected = networkErrorHandler.isConnected();
        actionLogArea.clear();
        handController.clear();
        boardCoordinator.clear();
        updateActionDisabled(true);
    }

    private void applyModeUI() {
        boolean multi = "multi".equals(selectedMode);
        boolean showLobbyControls = multi && (currentState == null || !currentState.started());
        multiConfigPane.setVisible(multi);
        multiConfigPane.setManaged(multi);
        if (readyButton != null) {
            readyButton.setVisible(showLobbyControls);
            readyButton.setManaged(showLobbyControls);
        }
        if (startButton != null) {
            startButton.setVisible(showLobbyControls);
            startButton.setManaged(showLobbyControls);
        }
        singleModeButton.setStyle(multi ? "" : "-fx-font-weight: bold;");
        multiModeButton.setStyle(multi ? "-fx-font-weight: bold;" : "");
    }

    private void showGamePane() {
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        gamePane.setVisible(true);
        gamePane.setManaged(true);
        statusLabel.setText("Success: entered game view");
        appendActionLog("Entered game successfully");
        reconnectLabel.setText("");
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

    private boolean enterSingleMode(String humanId, int botCount) {
        singleBotIds.clear();
        for (int i = 1; i <= botCount; i++) {
            String botId = humanId + "_bot" + i;
            GameResponse botJoin = client.send("JOIN", botId, Map.of(
                    "name", "SimpleBot" + i,
                    "bot", "true"
            ));
            if (!botJoin.success()) {
                menuStatusLabel.setText("Failed to create BOTs in single player: " + botJoin.message());
                joined = false;
                return false;
            }
            singleBotIds.add(botId);
        }

        readyPlayers.add(humanId);
        GameResponse readyResp = client.send("READY", humanId, Map.of());
        if (!readyResp.success()) {
            menuStatusLabel.setText("Single-player ready failed: " + readyResp.message());
            joined = false;
            return false;
        }

        GameResponse startResp = client.send("START", humanId, Map.of());
        if (!startResp.success()) {
            menuStatusLabel.setText("Single-player start failed: " + startResp.message());
            joined = false;
            return false;
        }
        return true;
    }

    private void maybeRunBotTurn() {
        if (!"single".equals(selectedMode) || botPlaying || currentState == null || !currentState.started() || currentState.gameOver()) {
            return;
        }
        String currentId = currentState.currentPlayerId();
        if (!singleBotIds.contains(currentId)) {
            return;
        }
        botPlaying = true;
        try {
            GameResponse botResp = client.send("BOT_TURN", currentId, Map.of());
            renderResponse(botResp, true);
        } finally {
            botPlaying = false;
        }
    }

    @FXML
    private void onReadyToggle() {
        if ("single".equals(selectedMode)) {
            return;
        }
        String action = readyPlayers.contains(playerId()) ? "UNREADY" : "READY";
        renderResponse(client.send(action, playerId(), Map.of()), true);
    }

    @FXML
    private void onStart() {
        if ("single".equals(selectedMode)) {
            statusLabel.setText("Single-player starts automatically");
            return;
        }
        if (!allHumanReady()) {
            statusLabel.setText("Please make all non-BOT players ready first");
            return;
        }
        renderResponse(client.send("START", playerId(), Map.of()), true);
    }

    @FXML
    private void onRefresh() {
        renderResponse(client.send("STATE", playerId(), Map.of()), true);
    }

    @FXML
    private void onResetGame() {
        if (client == null || !connected || !joined) {
            menuStatusLabel.setText("Please enter a room before resetting the game");
            return;
        }

        GameResponse resetResponse = client.send("RESET", playerId(), Map.of());
        if (!resetResponse.success()) {
            if (gamePane.isVisible()) {
                renderResponse(resetResponse, true);
            } else {
                menuStatusLabel.setText("Reset failed: " + resetResponse.message());
            }
            return;
        }

        clearLocalGameState();
        restoreInitialMenuState();
        menuStatusLabel.setText("Game reset to startup state");
        appendActionLog("Game reset to startup state");
    }

    @FXML
    private void onEndTurn() {
        renderResponse(client.send("END_TURN", playerId(), Map.of()), true);
    }

    /** Sends the currently selected card to the bank area. */
    @FXML
    private void onPlayMoney() {
        if (!ensureCardSelected()) {
            return;
        }
        playCardWithCenterAnimation(() -> client.send("PLAY_MONEY", playerId(), Map.of("handIndex", String.valueOf(handController.selectedHandIndex()))), true);
    }

    /** Plays the currently selected property card. */
    @FXML
    private void onPlayProperty() {
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
        playCardWithCenterAnimation(() -> client.send("PLAY_PROPERTY", playerId(), payload), true);
    }

    /** Changes the color assignment of an already played multi-property card. */
    @FXML
    private void onChangePropertyColor() {
        if (!gameViewModel.isMyTurn()) {
            statusLabel.setText("You can only change property color during your own turn");
            return;
        }
        PlayerState me = findMe();
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

        renderResponse(client.send("CHANGE_PROPERTY_COLOR", playerId(), Map.of(
                "fromColor", candidate.fromColor(),
                "propertyIndex", String.valueOf(candidate.propertyIndex()),
                "colorChoice", colorChoice
        )), true);
    }

    /** Plays the currently selected rent card. */
    @FXML
    private void onPlayRent() {
        if (!ensureCardSelected()) {
            return;
        }
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("handIndex", String.valueOf(handController.selectedHandIndex()));
        Card selectedCard = handController.selectedCard();
        String colorChoice = gameDialogService.chooseRentColor(selectedCard, findById(playerId()), gamePane);
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
            String targetPlayerId = chooseTargetPlayerId();
            if (targetPlayerId == null) {
                return;
            }
            payload.put("targetPlayerId", targetPlayerId);
        }
        playCardWithCenterAnimation(() -> client.send("PLAY_RENT", playerId(), payload), true);
    }

    /** Plays the currently selected action card with any extra payload. */
    @FXML
    private void onPlayAction() {
        if (!ensureCardSelected()) {
            return;
        }
        Map<String, String> payload = actionPayloadBuilder.build(
                handController.selectedHandIndex(),
                handController.selectedCard(),
                this::chooseTargetPlayerId,
                this::findById,
                this::findMe,
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
                message -> statusLabel.setText(message));
        if (payload == null) {
            return;
        }
        playCardWithCenterAnimation(() -> client.send("PLAY_ACTION", playerId(), payload), true);
    }

    private boolean ensureCardSelected() {
        return handController.ensureCardSelected(statusLabel);
    }

    /** Applies one backend response to the controller state and UI. */
    private void renderResponse(GameResponse response, boolean showStatus) {
        GameState previousState = currentState;
        if (showStatus) {
            statusLabel.setText((response.success() ? "Success: " : "Failed: ") + response.message());
            appendActionLog((response.success() ? "Success: " : "Failed: ") + response.message());
        }
        this.currentState = response.gameState();
        readyPlayers.clear();
        if (currentState != null && currentState.readyPlayerIds() != null) {
            readyPlayers.addAll(currentState.readyPlayerIds());
        }
        gameViewModel.update(currentState, readyPlayers, playerId());
        readyButton.setText(readyPlayers.contains(playerId()) ? "Unready" : "Ready");
        applyModeUI();
        syncView();
        effects.maybeAnimateDraws(previousState, currentState, playerId());
        maybeHandleJsnPrompt();
        maybeHandleGameOver();
    }

    /** Refreshes all visual sections from the current view model. */
    private void syncView() {
        handController.render(gameViewModel, this::updateActionFormBySelectedCard);
        boardCoordinator.renderBoard(currentState, playerId());
        effects.updatePileButtons(currentState);
        boardCoordinator.updateTurnAndDisable();
        boardCoordinator.updateActionFormBySelectedCard(handController.selectedCard());
    }

    @FXML
    private void onShowDrawPile() {
        effects.showDrawPile(currentState);
    }

    @FXML
    private void onShowDiscardPile() {
        effects.showDiscardPile(currentState);
    }

    /** Animates a played card from the hand area to the center board. */
    private void playCardWithCenterAnimation(Supplier<GameResponse> action, boolean playSoundOnSuccess) {
        effects.playCardWithCenterAnimation(
                handController.selectedHandButton(),
                handController.selectedCard(),
                action,
                response -> renderResponse(response, true),
                playSoundOnSuccess);
    }

    private void updateActionDisabled(boolean disabled) {
        if (boardCoordinator == null) {
            return;
        }
        if (disabled) {
            endTurnButton.setDisable(true);
            playMoneyButton.setDisable(true);
            playPropertyButton.setDisable(true);
            changePropertyColorButton.setDisable(true);
            playRentButton.setDisable(true);
            playActionButton.setDisable(true);
            return;
        }
        boardCoordinator.updateTurnAndDisable();
    }

    private void updateActionFormBySelectedCard() {
        if (boardCoordinator != null) {
            boardCoordinator.updateActionFormBySelectedCard(handController.selectedCard());
        }
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

    private record RecolorCandidate(String fromColor, int propertyIndex, Card card) {
    }

    private boolean allHumanReady() {
        return gameViewModel.allHumanReady();
    }

    private PlayerState findMe() {
        return gameViewModel.findMe();
    }

    private PlayerState findById(String id) {
        return gameViewModel.findById(id);
    }

    private String chooseTargetPlayerId() {
        return boardCoordinator.chooseTargetPlayerId(currentState, playerId(), statusLabel);
    }

    private void maybeHandleGameOver() {
        if (currentState == null || !currentState.gameOver() || gameOverHandled) {
            return;
        }
        gameOverHandled = true;
        final String winnerId = nonEmpty(currentState.winnerPlayerId(), "Unknown player");
        final boolean host = isHostPlayer();
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
                    client.send("RESET", playerId(), Map.of());
                }
            } catch (Exception exception) {
                appendActionLog("Game-over handling failed: " + exception.getMessage());
            } finally {
                clearLocalGameState();
                onBackToMenu();
            }
        });
    }

    /** Handles the local Just Say No prompt when the player is targeted. */
    private void maybeHandleJsnPrompt() {
        if (jsnPromptShowing || currentState == null) {
            return;
        }
        String responder = nonEmpty(currentState.jsnResponderPlayerId(), "");
        if (!playerId().equals(responder)) {
            return;
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
                GameResponse response = client.send("RESPOND_JSN", playerId(), Map.of("useCard", String.valueOf(useCard)));
                renderResponse(response, true);
                maybeRunBotTurn();
            } catch (Exception exception) {
                appendActionLog("JSN response failed: " + exception.getMessage());
            } finally {
                jsnPromptShowing = false;
            }
        });
    }

    private boolean isHostPlayer() {
        if (currentState == null || currentState.players() == null || currentState.players().isEmpty()) {
            return false;
        }
        String hostId = currentState.players().get(0).player().id();
        return hostId != null && hostId.equals(playerId());
    }



    private void appendActionLog(String line) {
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

    private void restoreInitialMenuState() {
        selectedMode = "single";
        singleBotCount = 1;
        currentPlayerId = "p1";
        if (menuHostField != null) {
            menuHostField.setText("127.0.0.1");
        }
        if (menuPortField != null) {
            menuPortField.setText("18080");
        }
        if (menuPlayerIdField != null) {
            menuPlayerIdField.setText("p1");
        }
        if (menuPlayerNameField != null) {
            menuPlayerNameField.setText("Player1");
        }
        if (menuBotCheckBox != null) {
            menuBotCheckBox.setSelected(false);
        }
        if (menuHostServerCheckBox != null) {
            menuHostServerCheckBox.setSelected(false);
        }
        onBackToMenu();
        applyModeUI();
    }

    private void putIfNotBlank(Map<String, String> payload, String key, String value) {
        if (value != null && !value.isBlank()) {
            payload.put(key, value.trim());
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String playerId() {
        return nonEmpty(currentPlayerId, "p1");
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
