package edu.group10.FrontEnd.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML private Label phaseLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label winnerLabel;
    @FXML private VBox playersArea;
    @FXML private VBox moneyArea;
    @FXML private VBox propertyArea;
    @FXML private VBox handArea;
    @FXML private Button drawCardButton;
    @FXML private Button endTurnButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (drawCardButton != null) {
            drawCardButton.setOnAction(e -> onDrawCard());
        }
        if (endTurnButton != null) {
            endTurnButton.setOnAction(e -> onEndTurn());
        }

        setupBoardAreas();

        edu.group10.common.model.GameState mockState = new edu.group10.common.model.GameState("GAME_001");
        mockState.setPhase(edu.group10.common.enums.GamePhase.WAITING);

        edu.group10.common.model.PlayerState me = new edu.group10.common.model.PlayerState();
        me.setPlayerId("PLAYER_1");
        me.setMoney(6);
        me.setBankCardIds(Arrays.asList("Money 5M", "Money 1M"));
        me.setPropertyIds(Arrays.asList("Blue Property", "Pass Go"));

        edu.group10.common.model.PlayerState opponent = new edu.group10.common.model.PlayerState();
        opponent.setPlayerId("PLAYER_2_BOSS");
        opponent.setHandCardCount(5);
        opponent.setMoney(10);

        mockState.getPlayers().put("PLAYER_1", me);
        mockState.getPlayers().put("PLAYER_2_BOSS", opponent);

        List<String> mySecretHand = Arrays.asList("Deal Breaker", "Money 2M", "Just Say No", "Green Property");

        refreshGameUI(mockState, "PLAYER_1", mySecretHand);
    }

    private void onDrawCard() {
        System.out.println("Draw Card Clicked");
    }

    private void onEndTurn() {
        System.out.println("End Turn Clicked");
    }

    private void setupBoardAreas() {
        if (moneyArea != null && getFlowPaneFrom(moneyArea) == null) {
            FlowPane bankBox = new FlowPane(10, 10);
            bankBox.setAlignment(Pos.TOP_LEFT);
            moneyArea.getChildren().add(bankBox);
        }

        if (propertyArea != null && getFlowPaneFrom(propertyArea) == null) {
            FlowPane propBox = new FlowPane(10, 10);
            propBox.setAlignment(Pos.TOP_LEFT);
            propertyArea.getChildren().add(propBox);
        }
    }

    private FlowPane getFlowPaneFrom(VBox area) {
        for (Node node : area.getChildren()) {
            if (node instanceof FlowPane) return (FlowPane) node;
        }
        return null;
    }

    private HBox getHBoxFrom(VBox area) {
        for (Node node : area.getChildren()) {
            if (node instanceof HBox) return (HBox) node;
        }
        return null;
    }

    private String getCardColor(String cardName) {
        String name = cardName.toLowerCase();
        if (name.contains("rent") || name.contains("house") || name.contains("hotel")) return "#457b9d";
        else if (name.contains("debt") || name.contains("birthday")) return "#ffb703";
        else if (name.contains("deal breaker") || name.contains("forced deal") || name.contains("sly deal")) return "#e63946";
        else if (name.contains("pass go") || name.contains("just say no")) return "#2a9d8f";
        else if (name.contains("money")) return "#e9c46a";
        else if (name.contains("property")) return "#118ab2";
        else return "#8d99ae";
    }

    private String getCardIcon(String cardName) {
        String name = cardName.toLowerCase();
        if (name.contains("rent")) return "🧾";
        else if (name.contains("house")) return "🏠";
        else if (name.contains("hotel")) return "🏨";
        else if (name.contains("debt")) return "💸";
        else if (name.contains("birthday")) return "🎁";
        else if (name.contains("deal breaker")) return "💔";
        else if (name.contains("forced deal")) return "🔀";
        else if (name.contains("sly deal")) return "🕵️";
        else if (name.contains("pass go")) return "🚀";
        else if (name.contains("just say no")) return "🚫";
        else if (name.contains("money")) return "💰";
        else if (name.contains("property")) return "🏢";
        else return "🃏";
    }

    private StackPane createCardView(String cardName, boolean isHandCard) {
        StackPane card = new StackPane();
        card.setPrefSize(80, 120);
        card.setMaxSize(80, 120);
        card.setMinSize(80, 120);

        String bgColor = getCardColor(cardName);
        String iconSymbol = getCardIcon(cardName);

        card.setStyle("-fx-background-color: " + bgColor + ";" +
                "-fx-border-color: #333333; -fx-border-width: 2px;" +
                "-fx-border-radius: 8px; -fx-background-radius: 8px;" +
                "-fx-cursor: hand;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        card.setEffect(shadow);

        VBox contentBox = new VBox(8);
        contentBox.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(iconSymbol);
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label nameLabel = new Label(cardName);
        nameLabel.setStyle("-fx-text-fill: #111111; -fx-font-weight: bold; -fx-font-size: 11px; -fx-alignment: center; -fx-wrap-text: true;");
        nameLabel.setMaxWidth(70);

        contentBox.getChildren().addAll(iconLabel, nameLabel);
        card.getChildren().add(contentBox);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: " + bgColor + ";" +
                "-fx-border-color: #ffd166; -fx-border-width: 3px;" +
                "-fx-border-radius: 8px; -fx-background-radius: 8px;" +
                "-fx-cursor: hand; -fx-translate-y: -10;"));

        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: " + bgColor + ";" +
                "-fx-border-color: #333333; -fx-border-width: 2px;" +
                "-fx-border-radius: 8px; -fx-background-radius: 8px;" +
                "-fx-cursor: hand; -fx-translate-y: 0;"));

        card.setOnMouseClicked(e -> {
            System.out.println("我点击了卡牌: " + cardName);
            if (isHandCard) {
                card.setTranslateY(0);
                card.setOnMouseEntered(null);
                card.setOnMouseExited(null);
                playCardAnimation(card, cardName);
            } else {
                System.out.println("桌面上的牌不能直接打出");
            }
        });

        return card;
    }

    private void playCardAnimation(StackPane card, String cardName) {
        VBox targetArea;
        if (cardName.toLowerCase().contains("money")) {
            targetArea = moneyArea;
        } else {
            targetArea = propertyArea;
        }

        HBox handBox = getHBoxFrom(handArea);
        if (handBox != null) {
            handBox.getChildren().remove(card);
        }

        FlowPane targetBox = getFlowPaneFrom(targetArea);

        if (targetBox != null) {
            targetBox.getChildren().add(card);

            card.setStyle("-fx-background-color: " + getCardColor(cardName) + ";" +
                    "-fx-border-color: #333333; -fx-border-width: 2px;" +
                    "-fx-border-radius: 8px; -fx-background-radius: 8px;");

            card.setTranslateY(200);
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), card);
            tt.setToY(0);
            tt.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            tt.play();
        }
    }

    public void refreshGameUI(edu.group10.common.model.GameState state, String myPlayerId, List<String> myActualHandCards) {
        if (phaseLabel != null) phaseLabel.setText("Phase: " + state.getPhase().toString());

        String currentTurnName = "Unknown";
        for (edu.group10.common.model.PlayerState ps : state.getPlayers().values()) {
            if (state.getCurrentPlayerIndex() >= 0) {
                currentTurnName = "Player index: " + state.getCurrentPlayerIndex();
            }
        }
        if (currentPlayerLabel != null) currentPlayerLabel.setText("Current turn: " + currentTurnName);

        edu.group10.common.model.PlayerState myState = state.getPlayers().get(myPlayerId);

        if (myState != null) {
            FlowPane bankBox = getFlowPaneFrom(moneyArea);
            if (bankBox != null) {
                bankBox.getChildren().clear();
                if (myState.getBankCardIds() != null) {
                    for (String cardName : myState.getBankCardIds()) {
                        bankBox.getChildren().add(createCardView(cardName, false));
                    }
                }
            }

            FlowPane propBox = getFlowPaneFrom(propertyArea);
            if (propBox != null) {
                propBox.getChildren().clear();
                if (myState.getPropertyIds() != null) {
                    for (String cardName : myState.getPropertyIds()) {
                        propBox.getChildren().add(createCardView(cardName, false));
                    }
                }
            }
        }

        if (handArea != null) {
            handArea.getChildren().clear();
            HBox handBox = new HBox(10);
            handBox.setAlignment(Pos.CENTER_LEFT);

            if (myActualHandCards != null) {
                for (String cardName : myActualHandCards) {
                    handBox.getChildren().add(createCardView(cardName, true));
                }
            }
            handArea.getChildren().add(handBox);
        }

        if (playersArea != null) {
            playersArea.getChildren().clear();

            Label titleLabel = new Label("Other Players");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");
            playersArea.getChildren().add(titleLabel);

            for (edu.group10.common.model.PlayerState ps : state.getPlayers().values()) {
                if (!ps.getPlayerId().equals(myPlayerId)) {
                    VBox otherPlayerCard = new VBox(5);
                    otherPlayerCard.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-padding: 10px; -fx-background-radius: 5px;");

                    Label nameLbl = new Label("👤 " + ps.getPlayerId());
                    nameLbl.setStyle("-fx-text-fill: #ffd166; -fx-font-weight: bold;");

                    Label handLbl = new Label("🃏 Hand Cards: " + ps.getHandCardCount());
                    handLbl.setStyle("-fx-text-fill: white;");

                    Label moneyLbl = new Label("💰 Bank Money: $" + ps.getMoney());
                    moneyLbl.setStyle("-fx-text-fill: white;");

                    otherPlayerCard.getChildren().addAll(nameLbl, handLbl, moneyLbl);
                    playersArea.getChildren().add(otherPlayerCard);
                }
            }
        }
    }
}