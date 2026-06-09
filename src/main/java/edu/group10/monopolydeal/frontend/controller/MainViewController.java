package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.frontend.context.FrontendContext;
import edu.group10.monopolydeal.frontend.network.client.GameClient;
import edu.group10.monopolydeal.frontend.view.AudioFeedbackService;
import edu.group10.monopolydeal.frontend.view.GameBoardView;
import edu.group10.monopolydeal.frontend.view.GameDialogService;
import edu.group10.monopolydeal.frontend.view.PaymentSelectionDialog;
import edu.group10.monopolydeal.frontend.viewmodel.GameViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Wires the main menu and in-game JavaFX screen together through focused collaborators.
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

    private final GameBoardView gameBoardView = new GameBoardView();
    private final GameViewModel gameViewModel = new GameViewModel();
    private final GameDialogService gameDialogService = new GameDialogService();
    private final AudioFeedbackService audioFeedbackService = new AudioFeedbackService();
    private final ActionPayloadBuilder actionPayloadBuilder = new ActionPayloadBuilder();
    private final PaymentSelectionDialog paymentSelectionDialog = new PaymentSelectionDialog(gameDialogService);

    private MainViewHandController handController;
    private MainViewBoardCoordinator boardCoordinator;
    private MainViewEffects effects;
    private MainViewMenuController menuController;
    private MainViewBotCoordinator botCoordinator;
    private MainViewSessionController sessionController;
    private MainViewUiSynchronizer uiSynchronizer;
    private MainViewGameController gameController;
    private MainViewPollingManager pollingManager;

    @FXML
    public void initialize() {
        GameClient client = FrontendContext.gameClient();
        if (client == null) {
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
        menuController = new MainViewMenuController(
                menuPane,
                gamePane,
                multiConfigPane,
                singleModeButton,
                multiModeButton,
                menuStatusLabel,
                menuHostField,
                menuPortField,
                menuPlayerIdField,
                menuPlayerNameField,
                menuBotCheckBox,
                menuHostServerCheckBox);
        botCoordinator = new MainViewBotCoordinator(client, gameDialogService, gamePane);
        sessionController = new MainViewSessionController(client, menuController, botCoordinator);
        uiSynchronizer = new MainViewUiSynchronizer(
                gameViewModel,
                handController,
                boardCoordinator,
                effects,
                statusLabel,
                reconnectLabel,
                turnLabel,
                selectedCardLabel,
                actionLogArea,
                readyButton,
                sessionController::markDisconnected,
                sessionController::playerId,
                this::applyModeUi,
                this::clearAuxState);
        gameController = new MainViewGameController(
                client,
                uiSynchronizer,
                handController,
                boardCoordinator,
                effects,
                gameDialogService,
                paymentSelectionDialog,
                audioFeedbackService,
                actionPayloadBuilder,
                gameViewModel,
                gamePane,
                statusLabel,
                menuStatusLabel,
                sessionController::isConnected,
                sessionController::joined,
                sessionController::playerId,
                menuController::selectedMode,
                this::restoreInitialMenuState,
                menuController::backToMenu,
                this::maybeRunBotTurn);
        pollingManager = new MainViewPollingManager(
                sessionController,
                reconnectLabel,
                client,
                sessionController::playerId,
                sessionController::joined,
                () -> gamePane.isVisible(),
                response -> {
                    uiSynchronizer.renderResponse(response, false);
                    if (!gameController.handleStateDrivenInteractions()) {
                        maybeRunBotTurn();
                    }
                });

        effects.setupDeckPileUI();
        setupVolumeControl();
        setupLogAreaStyle();
        setupHandPaneUi();
        updateActionDisabled(true);
        applyModeUi();
        pollingManager.start();
    }

    @FXML
    private void onChooseSingle() {
        menuController.selectSingleMode();
        applyModeUi();
    }

    @FXML
    private void onChooseMulti() {
        menuController.selectMultiMode();
        applyModeUi();
    }

    @FXML
    private void onEnterGame() {
        MainViewSessionController.EnterGameResult result = sessionController.enterGame(
                uiSynchronizer.readyPlayers(),
                uiSynchronizer::appendActionLog,
                reconnectLabel);
        if (!result.success()) {
            return;
        }
        menuController.showGamePane(statusLabel, uiSynchronizer::appendActionLog, reconnectLabel);
        gameController.onRefresh();
    }

    @FXML
    private void onBackToMenu() {
        menuController.backToMenu();
    }

    @FXML
    private void onEndGame() {
        try {
            if ("single".equals(menuController.selectedMode()) && sessionController.isConnected() && sessionController.joined()) {
                gameController.onResetGame();
            }
        } catch (Exception ignored) {
            // Best-effort cleanup before exiting the application.
        } finally {
            Platform.exit();
            System.exit(0);
        }
    }

    @FXML
    private void onReadyToggle() {
        gameController.onReadyToggle();
    }

    @FXML
    private void onStart() {
        gameController.onStart();
    }

    @FXML
    private void onRefresh() {
        gameController.onRefresh();
    }

    @FXML
    private void onResetGame() {
        gameController.onResetGame();
    }

    @FXML
    private void onEndTurn() {
        gameController.onEndTurn();
    }

    @FXML
    private void onPlayMoney() {
        gameController.onPlayMoney();
    }

    @FXML
    private void onPlayProperty() {
        gameController.onPlayProperty();
    }

    @FXML
    private void onChangePropertyColor() {
        gameController.onChangePropertyColor();
    }

    @FXML
    private void onPlayRent() {
        gameController.onPlayRent();
    }

    @FXML
    private void onPlayAction() {
        gameController.onPlayAction();
    }

    @FXML
    private void onShowDrawPile() {
        gameController.onShowDrawPile();
    }

    @FXML
    private void onShowDiscardPile() {
        gameController.onShowDiscardPile();
    }

    private void maybeRunBotTurn() {
        botCoordinator.maybeRunBotTurn(menuController.selectedMode(), uiSynchronizer.currentState(),
                response -> {
                    uiSynchronizer.renderResponse(response, true);
                    if (!gameController.handleStateDrivenInteractions()) {
                        maybeRunBotTurn();
                    }
                });
    }

    private void clearAuxState() {
        botCoordinator.clear();
        gameController.resetLocalFlowState();
    }

    private void updateActionDisabled(boolean disabled) {
        uiSynchronizer.updateActionDisabled(disabled);
    }

    private void applyModeUi() {
        menuController.applyModeUi(uiSynchronizer.currentState(), readyButton, startButton);
    }

    private void restoreInitialMenuState() {
        sessionController.restoreInitialState();
        menuController.restoreInitialMenuState();
        applyModeUi();
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

    private void setupLogAreaStyle() {
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

    private void setupHandPaneUi() {
        if (myHandScrollPane != null) {
            myHandScrollPane.setPannable(true);
            myHandScrollPane.setFitToWidth(false);
            myHandScrollPane.setFitToHeight(true);
        }
    }
}
