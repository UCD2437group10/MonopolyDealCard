package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.game.GameState;
import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.backend.network.server.GameServer;
import edu.group10.monopolydeal.frontend.context.FrontendContext;
import edu.group10.monopolydeal.frontend.network.client.GameClient;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import edu.group10.monopolydeal.frontend.view.CardImageRegistry;
import edu.group10.monopolydeal.frontend.view.GameBoardView;
import edu.group10.monopolydeal.frontend.view.GameDialogService;
import edu.group10.monopolydeal.frontend.viewmodel.GameViewModel;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Main view controller: main menu (single/multiplayer) + game interface.
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

    @FXML private Label statusLabel;
    @FXML private Label reconnectLabel;
    @FXML private Label turnLabel;
    @FXML private Label selectedCardLabel;

    @FXML private Button readyButton;
    @FXML private FlowPane myHandPane;
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
    @FXML private Button playRentButton;
    @FXML private Button playActionButton;

    private GameClient client;
    private GameState currentState;
    private Card selectedCard;
    private int selectedHandIndex = -1;
    private boolean connected;
    private boolean joined;
    private boolean botPlaying;
    private boolean gameOverHandled;
    private boolean jsnPromptShowing;
    private String selectedMode = "single";
    private final Set<String> singleBotIds = new HashSet<>();
    private String preferredTargetId = "";

    private String currentPlayerId = "p1";
    private GameServer hostedServer;
    private final GameBoardView gameBoardView = new GameBoardView();
    private final GameViewModel gameViewModel = new GameViewModel();
    private final GameDialogService gameDialogService = new GameDialogService();
    private final ActionPayloadBuilder actionPayloadBuilder = new ActionPayloadBuilder();
    private final NetworkErrorHandler networkErrorHandler = new NetworkErrorHandler();
    private final PollingCoordinator pollingCoordinator = new PollingCoordinator();

    private final Set<String> readyPlayers = new HashSet<>();

    @FXML
    public void initialize() {
        this.client = FrontendContext.gameClient();
        if (this.client == null) {
            menuStatusLabel.setText("Error: GameClient is not initialized");
            return;
        }
        setupDeckPileUI();
        setupRightLogAreaStyle();
        updateActionDisabled(true);
        applyModeUI();
        startPolling();
    }

    private void setupDeckPileUI() {
        configureDeckButton(drawPileButton);
        configureDeckButton(discardPileButton);
    }

    private void configureDeckButton(Button button) {
        if (button == null) {
            return;
        }
        button.setStyle("-fx-background-color: rgba(22,19,14,0.95);"
                + "-fx-border-color: #8b6e26; -fx-border-width: 1.2;"
                + "-fx-text-fill: #f0d57a; -fx-font-weight: bold; -fx-padding: 8;");
        button.setContentDisplay(ContentDisplay.TOP);
        button.setGraphicTextGap(6);
        Image image = CardImageRegistry.loadImage(CardImageRegistry.cardBackResource());
        if (image == null) {
            return;
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(124);
        imageView.setFitHeight(162);
        imageView.setPreserveRatio(false);
        button.setGraphic(imageView);
    }

    private void setupRightLogAreaStyle() {
        styleLogTextArea(actionLogArea);
        styleLogTextArea(playersTextArea);
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

    @FXML
    private void onEnterGame() {
        if (client == null) {
            menuStatusLabel.setText("Error: GameClient is not initialized");
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
            int botCount = chooseSingleBotCount();
            if (botCount <= 0) {
                menuStatusLabel.setText("Single-player entry canceled");
                joined = false;
                return;
            }
            if (!enterSingleMode(pid, botCount)) {
                return;
            }
        }

        joined = true;
        showGamePane();
        renderResponse(client.send("STATE", playerId(), Map.of()), true);
    }

    @FXML
    private void onBackToMenu() {
        if ("single".equals(selectedMode) && connected && joined && client != null) {
            client.send("RESET", playerId(), Map.of());
        }
        if ("multi".equals(selectedMode) && menuHostServerCheckBox != null && menuHostServerCheckBox.isSelected()) {
            hostedServer = null;
        }
        clearLocalGameState();
        menuPane.setVisible(true);
        menuPane.setManaged(true);
        gamePane.setVisible(false);
        gamePane.setManaged(false);
        menuStatusLabel.setText("Returned to main menu");
    }

    @FXML
    private void onEndGame() {
        try {
            if ("single".equals(selectedMode) && connected && joined && client != null) {
                client.send("RESET", playerId(), Map.of());
            }
        } catch (Exception ignored) {
            // Best-effort cleanup before exiting process.
        } finally {
            Platform.exit();
            System.exit(0);
        }
    }

    private void clearLocalGameState() {
        currentState = null;
        selectedCard = null;
        selectedHandIndex = -1;
        joined = false;
        botPlaying = false;
        gameOverHandled = false;
        readyPlayers.clear();
        singleBotIds.clear();
        preferredTargetId = "";
        selectedCardLabel.setText("Selected card: none");
        turnLabel.setText("Current turn: -");
        statusLabel.setText("Returned to main menu");
        reconnectLabel.setText("");
        networkErrorHandler.markDisconnected();
        connected = networkErrorHandler.isConnected();
        playersTextArea.setText("No state yet");
        actionLogArea.clear();
        myHandPane.getChildren().clear();
        myBankPane.getChildren().clear();
        myPropertyBox.getChildren().clear();
        opponentsPane.getChildren().clear();
        updateActionDisabled(true);
    }

    private void applyModeUI() {
        boolean multi = "multi".equals(selectedMode);
        multiConfigPane.setVisible(multi);
        multiConfigPane.setManaged(multi);

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
        String action = readyPlayers.contains(playerId()) ? "UNREADY" : "READY";
        renderResponse(client.send(action, playerId(), Map.of()), true);
    }

    @FXML
    private void onStart() {
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
    private void onEndTurn() {
        renderResponse(client.send("END_TURN", playerId(), Map.of()), true);
    }

    @FXML
    private void onPlayMoney() {
        if (!ensureCardSelected()) {
            return;
        }
        playCardWithCenterAnimation(() -> client.send("PLAY_MONEY", playerId(), Map.of("handIndex", String.valueOf(selectedHandIndex))));
    }

    @FXML
    private void onPlayProperty() {
        if (!ensureCardSelected()) {
            return;
        }
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("handIndex", String.valueOf(selectedHandIndex));
        putIfNotBlank(payload, "colorChoice", gameDialogService.choosePropertyColor(selectedCard, gamePane));
        playCardWithCenterAnimation(() -> client.send("PLAY_PROPERTY", playerId(), payload));
    }

    @FXML
    private void onPlayRent() {
        if (!ensureCardSelected()) {
            return;
        }
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("handIndex", String.valueOf(selectedHandIndex));
        putIfNotBlank(payload, "colorChoice", gameDialogService.chooseRentColor(selectedCard, findById(playerId()), gamePane));
        payload.put("doubleRentCount", gameDialogService.chooseDoubleRentCount(gamePane));
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
        playCardWithCenterAnimation(() -> client.send("PLAY_RENT", playerId(), payload));
    }

    @FXML
    private void onPlayAction() {
        if (!ensureCardSelected()) {
            return;
        }
        Map<String, String> payload = actionPayloadBuilder.build(
                selectedHandIndex,
                selectedCard,
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
        playCardWithCenterAnimation(() -> client.send("PLAY_ACTION", playerId(), payload));
    }

    private boolean ensureCardSelected() {
        if (selectedHandIndex < 0) {
            statusLabel.setText("Please select a hand card first");
            return false;
        }
        return true;
    }

    private void renderResponse(GameResponse response, boolean showStatus) {
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
        syncView();
        maybeHandleJsnPrompt();
        maybeHandleGameOver();
    }

    private void syncView() {
        renderMyHand();
        renderMyBank();
        renderMyProperties();
        renderOpponents();
        renderPlayers();
        updatePileButtons();
        updateTurnAndDisable();
        updateActionFormBySelectedCard();
    }

    private void updatePileButtons() {
        if (drawPileButton == null || discardPileButton == null) {
            return;
        }
        int draw = currentState == null ? 0 : currentState.drawPileCount();
        int discard = currentState == null ? 0 : currentState.discardPileCount();
        drawPileButton.setText("Draw Pile\n" + draw);
        discardPileButton.setText("Discard Pile\n" + discard);
    }

    @FXML
    private void onShowDrawPile() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        gameDialogService.styleDialog(alert, gamePane);
        int draw = currentState == null ? 0 : currentState.drawPileCount();
        alert.setTitle("Draw Pile");
        alert.setHeaderText("Draw pile info");
        alert.setContentText("Remaining cards: " + draw + "\n\nThis pile is hidden by rules; card details are not shown.");
        alert.showAndWait();
    }

    @FXML
    private void onShowDiscardPile() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        gameDialogService.styleDialog(alert, gamePane);
        int discard = currentState == null ? 0 : currentState.discardPileCount();
        alert.setTitle("Discard Pile");
        alert.setHeaderText("Discard pile info");
        alert.setContentText("Current cards: " + discard + "\n\nDetailed discard list is not exposed in this assignment build.");
        alert.showAndWait();
    }

    private void playCardWithCenterAnimation(Supplier<GameResponse> action) {
        Button source = selectedHandButton();
        if (source == null || centerBoardPane == null || centerBoardPane.getScene() == null) {
            renderResponse(action.get(), true);
            return;
        }
        Bounds sourceScene = source.localToScene(source.getBoundsInLocal());
        Bounds centerScene = centerBoardPane.localToScene(centerBoardPane.getBoundsInLocal());
        if (sourceScene == null || centerScene == null) {
            renderResponse(action.get(), true);
            return;
        }

        ImageView flying = selectedCard == null ? null : CardImageRegistry.buildCardImageView(selectedCard);
        if (flying == null) {
            renderResponse(action.get(), true);
            return;
        }
        flying.setManaged(false);
        flying.setMouseTransparent(true);
        flying.setFitWidth(source.getWidth() - 10);
        flying.setFitHeight(source.getHeight() - 10);

        double startX = sourceScene.getMinX() - centerScene.getMinX();
        double startY = sourceScene.getMinY() - centerScene.getMinY();
        flying.setTranslateX(startX);
        flying.setTranslateY(startY);
        centerBoardPane.getChildren().add(flying);

        double targetX = (centerBoardPane.getWidth() - sourceScene.getWidth()) / 2.0;
        double targetY = (centerBoardPane.getHeight() - sourceScene.getHeight()) / 2.0;

        TranslateTransition move = new TranslateTransition(Duration.millis(260), flying);
        move.setToX(targetX);
        move.setToY(targetY);
        FadeTransition fade = new FadeTransition(Duration.millis(260), flying);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        ParallelTransition animation = new ParallelTransition(move, fade);
        animation.setOnFinished(event -> {
            centerBoardPane.getChildren().remove(flying);
            renderResponse(action.get(), true);
        });
        animation.play();
    }

    private Button selectedHandButton() {
        if (selectedHandIndex < 0 || selectedHandIndex >= myHandPane.getChildren().size()) {
            return null;
        }
        var node = myHandPane.getChildren().get(selectedHandIndex);
        return node instanceof Button button ? button : null;
    }

    private void renderMyHand() {
        myHandPane.getChildren().clear();
        PlayerState me = gameViewModel.findMe();
        if (me == null) {
            selectedHandIndex = -1;
            selectedCard = null;
            selectedCardLabel.setText("Selected card: none");
            return;
        }
        if (selectedHandIndex >= me.hand().size()) {
            selectedHandIndex = -1;
            selectedCard = null;
            selectedCardLabel.setText("Selected card: none");
        }
        for (int i = 0; i < me.hand().size(); i++) {
            Card card = me.hand().get(i);
            Button button = new Button();
            button.setPrefSize(170, 240);
            button.setMinSize(170, 240);
            button.setMaxSize(170, 240);
            button.setStyle(gameBoardView.handImageStyle(i == selectedHandIndex));
            ImageView imageView = CardImageRegistry.buildCardImageView(card);
            if (imageView != null) {
                button.setGraphic(imageView);
            }
            final int idx = i;
            button.setOnAction(event -> {
                selectedHandIndex = idx;
                selectedCard = card;
                selectedCardLabel.setText("Selected card: #" + idx + " " + card.name());
                renderMyHand();
                updateActionFormBySelectedCard();
            });
            myHandPane.getChildren().add(button);
        }
    }

    private void renderMyBank() {
        gameBoardView.renderMyBank(myBankPane, gameViewModel.findMe());
    }

    private void renderMyProperties() {
        gameBoardView.renderMyProperties(
                myPropertyBox,
                gameViewModel.findMe(),
                color -> gameViewModel.requiredSetSize(color),
                this::showMyPropertyGroupDialog);
    }

    private void renderOpponents() {
        gameBoardView.renderOpponents(
                opponentsPane,
                currentState == null ? List.of() : currentState.players(),
                playerId(),
                id -> preferredTargetId = id,
                this::showPlayerDetailsDialog);
    }

    private void showMyPropertyGroupDialog(String color, List<Card> cards, int house, int hotel) {
        String detail = gameViewModel.propertyGroupDetail(color, cards, house, hotel);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        gameDialogService.styleDialog(alert, gamePane);
        alert.setTitle("Property Details");
        alert.setHeaderText("My Property Group");
        alert.setContentText(detail);
        alert.showAndWait();
    }

    private void showPlayerDetailsDialog(PlayerState p) {
        String detail = gameViewModel.playerDetail(p);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        gameDialogService.styleDialog(alert, gamePane);
        alert.setTitle("Player Asset Details");
        alert.setHeaderText("View " + p.player().displayName());
        alert.setContentText(detail);
        alert.showAndWait();
    }

    private void renderPlayers() {
        playersTextArea.setText(gameViewModel.playersSummaryText());
    }


    private void updateTurnAndDisable() {
        boolean isMyTurn = gameViewModel.isMyTurn();
        turnLabel.setText(gameViewModel.turnText());
        updateActionDisabled(!isMyTurn);
    }

    private void updateActionDisabled(boolean disabled) {
        endTurnButton.setDisable(disabled);
        playMoneyButton.setDisable(disabled);
        playPropertyButton.setDisable(disabled);
        playRentButton.setDisable(disabled);
        playActionButton.setDisable(disabled);
    }

    private void updateActionFormBySelectedCard() {
        String name = selectedCard == null ? "" : selectedCard.name();
        boolean isRent = selectedCard != null && "RENT".equals(String.valueOf(selectedCard.type()));
        boolean isAction = selectedCard != null && "ACTION".equals(String.valueOf(selectedCard.type()));
        boolean canPlayAsMoney = selectedCard != null
                && (selectedCard.type() == CardType.MONEY
                || selectedCard.type() == CardType.ACTION
                || selectedCard.type() == CardType.RENT)
                && selectedCard.bankValue() > 0;
        boolean actionForbiddenActivePlay = "Just Say No".equals(name) || "Double The Rent".equals(name);

        playRentButton.setDisable(!gameViewModel.isMyTurn() || !isRent);
        playActionButton.setDisable(!gameViewModel.isMyTurn() || !isAction || actionForbiddenActivePlay);
        playPropertyButton.setDisable(!gameViewModel.isMyTurn() || selectedCard == null
                || !("PROPERTY".equals(String.valueOf(selectedCard.type()))
                || "MULTI_PROPERTY".equals(String.valueOf(selectedCard.type()))));
        playMoneyButton.setDisable(!gameViewModel.isMyTurn() || !canPlayAsMoney);
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
        String target = gameDialogService.chooseTargetPlayerId(
                currentState == null ? List.of() : currentState.players(),
                playerId(),
                preferredTargetId,
                gamePane);
        if (target == null) {
            statusLabel.setText("No target player available");
            return null;
        }
        preferredTargetId = target;
        return target;
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
                onBackToMenu();
            }
        });
    }

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
