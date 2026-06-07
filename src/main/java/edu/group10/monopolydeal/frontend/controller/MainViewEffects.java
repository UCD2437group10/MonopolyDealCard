package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.game.GameState;
import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.frontend.view.CardImageRegistry;
import edu.group10.monopolydeal.frontend.view.GameDialogService;
import edu.group10.monopolydeal.frontend.view.AudioFeedbackService;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Manages pile visuals and animation effects for the main game screen.
 */
final class MainViewEffects {
    private final GameDialogService gameDialogService;
    private final AudioFeedbackService audioFeedbackService;
    private final VBox gamePane;
    private final StackPane centerBoardPane;
    private final Button drawPileButton;
    private final Button discardPileButton;
    private final FlowPane opponentsPane;
    private final HBox myHandPane;

    MainViewEffects(GameDialogService gameDialogService, AudioFeedbackService audioFeedbackService,
                    VBox gamePane, StackPane centerBoardPane, Button drawPileButton,
                    Button discardPileButton, FlowPane opponentsPane, HBox myHandPane) {
        this.gameDialogService = gameDialogService;
        this.audioFeedbackService = audioFeedbackService;
        this.gamePane = gamePane;
        this.centerBoardPane = centerBoardPane;
        this.drawPileButton = drawPileButton;
        this.discardPileButton = discardPileButton;
        this.opponentsPane = opponentsPane;
        this.myHandPane = myHandPane;
    }

    void setupDeckPileUI() {
        configureDeckButton(drawPileButton, true);
        configureDeckButton(discardPileButton, false);
    }

    void updatePileButtons(GameState currentState) {
        if (drawPileButton == null || discardPileButton == null) {
            return;
        }
        int draw = currentState == null ? 0 : currentState.drawPileCount();
        int discard = currentState == null ? 0 : currentState.discardPileCount();
        drawPileButton.setText("Draw Pile\n" + draw);
        discardPileButton.setText("Discard Pile\n" + discard);
        drawPileButton.setGraphic(buildDrawPileGraphic());
        Card discardTop = currentState == null || currentState.discardPileCards() == null || currentState.discardPileCards().isEmpty()
                ? null
                : currentState.discardPileCards().get(0);
        discardPileButton.setGraphic(buildDiscardPileGraphic(discardTop));
    }

    void showDrawPile(GameState currentState) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        gameDialogService.styleDialog(alert, gamePane);
        int draw = currentState == null ? 0 : currentState.drawPileCount();
        alert.setTitle("Draw Pile");
        alert.setHeaderText("Draw pile info");
        alert.setContentText("Remaining cards: " + draw + "\n\nThis pile is hidden by rules; card details are not shown.");
        alert.showAndWait();
    }

    void showDiscardPile(GameState currentState) {
        int discard = currentState == null ? 0 : currentState.discardPileCount();
        List<Card> cards = currentState == null || currentState.discardPileCards() == null
                ? List.of()
                : currentState.discardPileCards();
        Dialog<Void> dialog = new Dialog<>();
        gameDialogService.styleDialog(dialog, gamePane);
        dialog.setTitle("Discard Pile");
        dialog.setHeaderText("Discard pile cards: " + discard + " (latest first)");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        if (cards.isEmpty()) {
            Label empty = new Label("Discard pile is empty");
            empty.setStyle("-fx-text-fill: #e7d39a;");
            dialog.getDialogPane().setContent(empty);
            dialog.showAndWait();
            return;
        }

        FlowPane cardPane = new FlowPane();
        cardPane.setHgap(12);
        cardPane.setVgap(12);
        cardPane.setPrefWrapLength(720);
        for (Card card : cards) {
            VBox cardBox = new VBox(6);
            ImageView imageView = CardImageRegistry.buildCardImageView(card);
            if (imageView != null) {
                imageView.setFitWidth(132);
                imageView.setFitHeight(188);
                cardBox.getChildren().add(imageView);
            }
            Label label = new Label(card.name());
            label.setStyle("-fx-text-fill: #e7d39a; -fx-font-size: 11px;");
            label.setWrapText(true);
            label.setPrefWidth(132);
            cardBox.getChildren().add(label);
            cardPane.getChildren().add(cardBox);
        }

        ScrollPane scrollPane = new ScrollPane(cardPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportWidth(760);
        scrollPane.setPrefViewportHeight(420);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    void playCardWithCenterAnimation(Button source, Card selectedCard, Supplier<GameResponse> action,
                                     Consumer<GameResponse> responseConsumer, boolean playSoundOnSuccess) {
        if (source == null || centerBoardPane == null || centerBoardPane.getScene() == null) {
            completeCardAction(action, responseConsumer, playSoundOnSuccess);
            return;
        }
        Bounds sourceScene = source.localToScene(source.getBoundsInLocal());
        Bounds centerScene = centerBoardPane.localToScene(centerBoardPane.getBoundsInLocal());
        if (sourceScene == null || centerScene == null) {
            completeCardAction(action, responseConsumer, playSoundOnSuccess);
            return;
        }

        ImageView flying = selectedCard == null ? null : CardImageRegistry.buildCardImageView(selectedCard);
        if (flying == null) {
            completeCardAction(action, responseConsumer, playSoundOnSuccess);
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
            completeCardAction(action, responseConsumer, playSoundOnSuccess);
        });
        animation.play();
    }

    void maybeAnimateDraws(GameState previousState, GameState nextState, String localPlayerId) {
        if (!shouldAnimateDraw(previousState, nextState) || drawPileButton == null || gamePane == null || gamePane.getScene() == null) {
            return;
        }

        int remainingCards = Math.max(0, previousState.drawPileCount() - nextState.drawPileCount());
        SequentialTransition sequence = new SequentialTransition();
        for (PlayerState nextPlayer : nextState.players()) {
            PlayerState previousPlayer = findPlayerById(previousState, nextPlayer.player().id());
            int previousHand = previousPlayer == null ? 0 : previousPlayer.hand().size();
            int gained = Math.max(0, nextPlayer.hand().size() - previousHand);
            if (gained <= 0) {
                continue;
            }
            Node targetNode = resolveDrawAnimationTarget(nextPlayer.player(), localPlayerId);
            if (targetNode == null) {
                continue;
            }
            int count = Math.min(gained, remainingCards);
            for (int i = 0; i < count; i++) {
                ParallelTransition animation = buildDrawAnimation(targetNode, i);
                if (animation != null) {
                    sequence.getChildren().add(animation);
                    remainingCards--;
                }
            }
            if (remainingCards <= 0) {
                break;
            }
        }
        if (!sequence.getChildren().isEmpty()) {
            sequence.play();
        }
    }

    private void configureDeckButton(Button button, boolean drawPile) {
        if (button == null) {
            return;
        }
        button.setStyle("-fx-background-color: rgba(22,19,14,0.95);"
                + "-fx-border-color: #8b6e26; -fx-border-width: 1.2;"
                + "-fx-text-fill: #f0d57a; -fx-font-weight: bold; -fx-padding: 8;");
        button.setContentDisplay(javafx.scene.control.ContentDisplay.TOP);
        button.setGraphicTextGap(6);
        button.setGraphic(drawPile ? buildDrawPileGraphic() : buildDiscardPileGraphic(null));
    }

    private void completeCardAction(Supplier<GameResponse> action, Consumer<GameResponse> responseConsumer,
                                    boolean playSoundOnSuccess) {
        GameResponse response = action.get();
        responseConsumer.accept(response);
        if (playSoundOnSuccess && response.success()) {
            audioFeedbackService.playCardPlay();
        }
    }

    private boolean shouldAnimateDraw(GameState previousState, GameState nextState) {
        return previousState != null
                && nextState != null
                && previousState.started()
                && nextState.started()
                && !nextState.gameOver()
                && previousState.drawPileCount() > nextState.drawPileCount()
                && previousState.players() != null
                && nextState.players() != null
                && previousState.players().size() == nextState.players().size();
    }

    private ParallelTransition buildDrawAnimation(Node targetNode, int cardOffsetIndex) {
        Bounds sourceScene = drawPileButton.localToScene(drawPileButton.getBoundsInLocal());
        Bounds boardScene = centerBoardPane == null ? null : centerBoardPane.localToScene(centerBoardPane.getBoundsInLocal());
        Bounds targetScene = targetNode.localToScene(targetNode.getBoundsInLocal());
        if (sourceScene == null || boardScene == null || targetScene == null) {
            return null;
        }

        Image image = CardImageRegistry.loadImage(CardImageRegistry.cardBackResource());
        if (image == null) {
            return null;
        }

        ImageView flying = new ImageView(image);
        flying.setManaged(false);
        flying.setMouseTransparent(true);
        flying.setPreserveRatio(false);
        flying.setFitWidth(92);
        flying.setFitHeight(132);

        double startX = sourceScene.getMinX() - boardScene.getMinX() + 16 + (cardOffsetIndex * 4.0);
        double startY = sourceScene.getMinY() - boardScene.getMinY() + 16 - (cardOffsetIndex * 3.0);
        flying.setTranslateX(startX);
        flying.setTranslateY(startY);
        centerBoardPane.getChildren().add(flying);

        double targetX = computeTargetX(targetNode, targetScene, boardScene);
        double targetY = computeTargetY(targetNode, targetScene, boardScene);
        TranslateTransition move = new TranslateTransition(Duration.millis(320), flying);
        move.setToX(targetX);
        move.setToY(targetY);
        FadeTransition fade = new FadeTransition(Duration.millis(320), flying);
        fade.setFromValue(1.0);
        fade.setToValue(0.2);
        ScaleTransition scale = new ScaleTransition(Duration.millis(320), flying);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.88);
        scale.setToY(0.88);
        ParallelTransition animation = new ParallelTransition(move, fade, scale);
        animation.setDelay(Duration.millis(cardOffsetIndex * 80.0));
        animation.setOnFinished(event -> centerBoardPane.getChildren().remove(flying));
        return animation;
    }

    private double computeTargetX(Node targetNode, Bounds targetScene, Bounds boardScene) {
        double targetWidth = targetNode instanceof Region region ? region.getWidth() : targetScene.getWidth();
        if (targetNode == myHandPane) {
            return targetScene.getMinX() - boardScene.getMinX() + Math.min(80, Math.max(24, targetWidth * 0.12));
        }
        return targetScene.getMinX() - boardScene.getMinX() + Math.max(24, targetWidth * 0.5 - 46);
    }

    private double computeTargetY(Node targetNode, Bounds targetScene, Bounds boardScene) {
        double targetHeight = targetNode instanceof Region region ? region.getHeight() : targetScene.getHeight();
        if (targetNode == myHandPane) {
            return targetScene.getMinY() - boardScene.getMinY() + Math.max(10, targetHeight * 0.15);
        }
        return targetScene.getMinY() - boardScene.getMinY() + Math.max(12, targetHeight * 0.2);
    }

    private Node resolveDrawAnimationTarget(Player player, String localPlayerId) {
        if (player == null) {
            return null;
        }
        if (localPlayerId.equals(player.id())) {
            return myHandPane;
        }
        for (Node node : opponentsPane.getChildren()) {
            if (player.id().equals(String.valueOf(node.getUserData()))) {
                return node;
            }
        }
        return null;
    }

    private StackPane buildDrawPileGraphic() {
        StackPane pile = new StackPane();
        pile.setPrefSize(132, 178);
        for (int i = 2; i >= 0; i--) {
            ImageView layer = buildPileImageView(null);
            if (layer == null) {
                continue;
            }
            layer.setTranslateX(i * 4.0);
            layer.setTranslateY(-i * 3.0);
            layer.setOpacity(i == 0 ? 1.0 : 0.82);
            pile.getChildren().add(layer);
        }
        return pile;
    }

    private StackPane buildDiscardPileGraphic(Card topCard) {
        StackPane pile = new StackPane();
        pile.setPrefSize(132, 178);
        ImageView base = buildPileImageView(null);
        if (base != null) {
            base.setTranslateX(4);
            base.setTranslateY(-3);
            base.setOpacity(0.78);
            pile.getChildren().add(base);
        }
        ImageView top = buildPileImageView(topCard);
        if (top != null) {
            pile.getChildren().add(top);
        }
        return pile;
    }

    private ImageView buildPileImageView(Card card) {
        Image image = card == null
                ? CardImageRegistry.loadImage(CardImageRegistry.cardBackResource())
                : CardImageRegistry.loadCardImage(card);
        if (image == null) {
            return null;
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(124);
        imageView.setFitHeight(162);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        return imageView;
    }

    private PlayerState findPlayerById(GameState state, String id) {
        if (state == null || state.players() == null || id == null) {
            return null;
        }
        for (PlayerState playerState : state.players()) {
            if (id.equals(playerState.player().id())) {
                return playerState;
            }
        }
        return null;
    }
}
