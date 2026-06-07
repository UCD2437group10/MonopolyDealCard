package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.game.GameState;
import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.frontend.view.GameBoardView;
import edu.group10.monopolydeal.frontend.view.GameDialogService;
import edu.group10.monopolydeal.frontend.viewmodel.GameViewModel;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Renders non-hand game sections and manages action availability.
 */
final class MainViewBoardCoordinator {
    private final GameBoardView gameBoardView;
    private final GameViewModel gameViewModel;
    private final GameDialogService gameDialogService;
    private final FlowPane myBankPane;
    private final FlowPane opponentsPane;
    private final VBox myPropertyBox;
    private final VBox gamePane;
    private final TextArea playersTextArea;
    private final Label turnLabel;
    private final Button endTurnButton;
    private final Button playMoneyButton;
    private final Button playPropertyButton;
    private final Button changePropertyColorButton;
    private final Button playRentButton;
    private final Button playActionButton;

    private String preferredTargetId = "";

    MainViewBoardCoordinator(GameBoardView gameBoardView, GameViewModel gameViewModel,
                             GameDialogService gameDialogService, FlowPane myBankPane,
                             FlowPane opponentsPane, VBox myPropertyBox, VBox gamePane,
                             TextArea playersTextArea, Label turnLabel, Button endTurnButton,
                             Button playMoneyButton, Button playPropertyButton,
                             Button changePropertyColorButton, Button playRentButton,
                             Button playActionButton) {
        this.gameBoardView = gameBoardView;
        this.gameViewModel = gameViewModel;
        this.gameDialogService = gameDialogService;
        this.myBankPane = myBankPane;
        this.opponentsPane = opponentsPane;
        this.myPropertyBox = myPropertyBox;
        this.gamePane = gamePane;
        this.playersTextArea = playersTextArea;
        this.turnLabel = turnLabel;
        this.endTurnButton = endTurnButton;
        this.playMoneyButton = playMoneyButton;
        this.playPropertyButton = playPropertyButton;
        this.changePropertyColorButton = changePropertyColorButton;
        this.playRentButton = playRentButton;
        this.playActionButton = playActionButton;
    }

    void clear() {
        preferredTargetId = "";
        playersTextArea.setText("No state yet");
        myBankPane.getChildren().clear();
        myPropertyBox.getChildren().clear();
        opponentsPane.getChildren().clear();
        updateActionDisabled(true);
    }

    void renderBoard(GameState currentState, String playerId) {
        renderMyBank();
        renderMyProperties();
        renderOpponents(currentState, playerId);
        playersTextArea.setText(gameViewModel.playersSummaryText());
    }

    void updateTurnAndDisable() {
        boolean isMyTurn = gameViewModel.isMyTurn();
        turnLabel.setText(gameViewModel.turnText());
        updateActionDisabled(!isMyTurn);
    }

    void updateActionFormBySelectedCard(Card selectedCard) {
        String name = selectedCard == null ? "" : selectedCard.name();
        boolean isRent = selectedCard != null && selectedCard.type() == CardType.RENT;
        boolean isAction = selectedCard != null && selectedCard.type() == CardType.ACTION;
        boolean canPlayAsMoney = selectedCard != null
                && (selectedCard.type() == CardType.MONEY
                || selectedCard.type() == CardType.ACTION
                || selectedCard.type() == CardType.RENT)
                && selectedCard.bankValue() > 0;
        boolean actionForbiddenActivePlay = "Just Say No".equals(name) || "Double The Rent".equals(name);

        playRentButton.setDisable(!gameViewModel.isMyTurn() || !isRent);
        playActionButton.setDisable(!gameViewModel.isMyTurn() || !isAction || actionForbiddenActivePlay);
        playPropertyButton.setDisable(!gameViewModel.isMyTurn() || selectedCard == null
                || (selectedCard.type() != CardType.PROPERTY && selectedCard.type() != CardType.MULTI_PROPERTY));
        changePropertyColorButton.setDisable(!gameViewModel.isMyTurn() || !hasRecolorableProperty());
        playMoneyButton.setDisable(!gameViewModel.isMyTurn() || !canPlayAsMoney);
    }

    String chooseTargetPlayerId(GameState currentState, String playerId, Label statusLabel) {
        String target = gameDialogService.chooseTargetPlayerId(
                currentState == null ? List.of() : currentState.players(),
                playerId,
                preferredTargetId,
                gamePane);
        if (target == null) {
            statusLabel.setText("No target player available");
            return null;
        }
        preferredTargetId = target;
        return target;
    }

    private void renderMyBank() {
        gameBoardView.renderMyBank(myBankPane, gameViewModel.findMe());
    }

    private void renderMyProperties() {
        gameBoardView.renderMyProperties(
                myPropertyBox,
                gameViewModel.findMe(),
                gameViewModel::requiredSetSize,
                this::showMyPropertyGroupDialog);
    }

    private void renderOpponents(GameState currentState, String playerId) {
        gameBoardView.renderOpponents(
                opponentsPane,
                currentState == null ? List.of() : currentState.players(),
                playerId,
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

    private void showPlayerDetailsDialog(PlayerState playerState) {
        String detail = gameViewModel.playerDetail(playerState);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        gameDialogService.styleDialog(alert, gamePane);
        alert.setTitle("Player Asset Details");
        alert.setHeaderText("View " + playerState.player().displayName());
        alert.setContentText(detail);
        alert.showAndWait();
    }

    private void updateActionDisabled(boolean disabled) {
        endTurnButton.setDisable(disabled);
        playMoneyButton.setDisable(disabled);
        playPropertyButton.setDisable(disabled);
        changePropertyColorButton.setDisable(disabled);
        playRentButton.setDisable(disabled);
        playActionButton.setDisable(disabled);
    }

    private boolean hasRecolorableProperty() {
        PlayerState me = gameViewModel.findMe();
        if (me == null) {
            return false;
        }
        for (List<Card> cards : me.properties().values()) {
            for (Card card : cards) {
                if (card.type() == CardType.MULTI_PROPERTY) {
                    return true;
                }
            }
        }
        return false;
    }
}
