package edu.group10.FrontEnd.controller;

import edu.group10.common.enums.ActionType;
import edu.group10.common.enums.GamePhase;
import edu.group10.common.enums.PlayerStatus;
import edu.group10.common.model.*;
import edu.group10.common.model.GameActionResult;
import edu.group10.core.GameEngine;
import edu.group10.core.GameEngineImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    // FXML bindings (wait for UIA to set fx:id)
    @FXML private Button drawCardButton;
    @FXML private Button endTurnButton;
    @FXML private VBox handArea;
    @FXML private VBox propertyArea;
    @FXML private VBox moneyArea;
    @FXML private Label phaseLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label winnerLabel;
    @FXML private VBox playersArea;

    private GameEngine gameEngine;
    private String gameId;
    private String playerId;
    private String selectedCardId;

    public GameController() {
        this.gameEngine = new GameEngineImpl();
    }

    // Called by Infra to set room and player info
    public void setGameInfo(String gameId, String playerId) {
        this.gameId = gameId;
        this.playerId = playerId;
        refreshUI();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (drawCardButton != null) {
            drawCardButton.setOnAction(e -> onDrawCard());
        }
        if (endTurnButton != null) {
            endTurnButton.setOnAction(e -> onEndTurn());
        }
    }

    //Player Actions

    private void onDrawCard() {
        PlayerAction action = createAction(ActionType.DRAW_CARD);
        executeAction(action);
    }

    private void onEndTurn() {
        PlayerAction action = createAction(ActionType.END_TURN);
        executeAction(action);
    }

    public void onPlayCard(String cardId) {
        this.selectedCardId = cardId;
        PlayerAction action = createAction(ActionType.PLAY_CARD);
        action.setCardId(cardId);
        executeAction(action);
    }

    public void onSelectTarget(String targetPlayerId) {
        PlayerAction action = createAction(ActionType.SELECT_TARGET);
        action.setTargetPlayerId(targetPlayerId);
        executeAction(action);
    }

    public void onConfirm(List<String> selectedCardIds) {
        PlayerAction action = createAction(ActionType.CONFIRM);
        action.setSelectedCardIds(selectedCardIds);
        executeAction(action);
    }

    public void onCancel() {
        PlayerAction action = createAction(ActionType.CANCEL);
        executeAction(action);
    }

    private PlayerAction createAction(ActionType type) {
        PlayerAction action = new PlayerAction();
        action.setGameId(gameId);
        action.setPlayerId(playerId);
        action.setType(type);
        return action;
    }

    private void executeAction(PlayerAction action) {
        try {
            GameActionResult result = gameEngine.executeAction(gameId, action);
            if (result != null && result.getNewState() != null) {
                refreshUI(result.getNewState());
            } else {
                refreshUI();
            }
        } catch (Exception e) {
            System.err.println("Action failed: " + e.getMessage());
            showError(e.getMessage());
        }
    }

    //UI Refresh

    public void refreshUI() {
        GameState gameState = gameEngine.getGameState(gameId);
        if (gameState != null) {
            refreshUI(gameState);
        }
    }

    private void refreshUI(GameState gameState) {
        Platform.runLater(() -> {
            if (gameState == null) return;

            updatePhase(gameState.getPhase());
            updateDeckInfo(gameState.getDeckRemaining());
            updatePlayersDisplay(gameState);
            highlightCurrentPlayer(gameState);

            if (gameState.getWinnerId() != null && !gameState.getWinnerId().isEmpty()) {
                showWinner(gameState.getWinnerId());
            }

            updateCurrentPlayerDetails(gameState);
        });
    }

    private void updatePhase(GamePhase phase) {
        if (phaseLabel != null) {
            String text;
            switch (phase) {
                case WAITING:
                    text = "Waiting for players...";
                    break;
                case PLAYING:
                    text = "Playing";
                    break;
                case PASSIVE_RESPONSE:
                    text = "Waiting for confirmation...";
                    break;
                case GAME_OVER:
                case ENDED:
                    text = "Game Over";
                    break;
                default:
                    text = phase.name();
            }
            phaseLabel.setText(text);
        }
    }

    private void updateDeckInfo(int remaining) {
        System.out.println("Deck remaining: " + remaining);
    }

    private void updatePlayersDisplay(GameState gameState) {
        Map<String, PlayerState> players = gameState.getPlayers();
        if (players == null || playersArea == null) return;

        for (Map.Entry<String, PlayerState> entry : players.entrySet()) {
            PlayerState ps = entry.getValue();
            System.out.println("Player: " + ps.getPlayerName() +
                    ", Money: " + ps.getMoney() +
                    ", Hand: " + ps.getHandCardCount() +
                    ", Sets: " + ps.getCompletedSets());
        }
    }

    private void highlightCurrentPlayer(GameState gameState) {
        PlayerState currentPlayer = gameState.getCurrentPlayer();
        if (currentPlayer != null && currentPlayerLabel != null) {
            currentPlayerLabel.setText("Current turn: " + currentPlayer.getPlayerName());
        }
    }

    private void showWinner(String winnerId) {
        if (winnerLabel != null) {
            winnerLabel.setText("Winner: " + winnerId);
        }
    }

    private void updateCurrentPlayerDetails(GameState gameState) {
        PlayerState myState = gameState.getPlayers().get(playerId);
        if (myState == null) return;

        updateMoneyDisplay(myState.getMoney());
        updatePropertyArea(myState.getPropertyIds());
        updateHandCount(myState.getHandCardCount());
    }

    private void updateMoneyDisplay(int money) {
        System.out.println("Current money: " + money);
    }

    private void updatePropertyArea(List<String> propertyIds) {
        System.out.println("Property cards: " + (propertyIds != null ? propertyIds.size() : 0));
    }

    private void updateHandCount(int count) {
        System.out.println("Hand cards: " + count);
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            System.err.println("Error: " + message);
        });
    }
}