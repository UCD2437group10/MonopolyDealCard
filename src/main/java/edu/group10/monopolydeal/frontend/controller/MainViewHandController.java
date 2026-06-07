package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.frontend.view.CardImageRegistry;
import edu.group10.monopolydeal.frontend.view.GameBoardView;
import edu.group10.monopolydeal.frontend.view.AudioFeedbackService;
import edu.group10.monopolydeal.frontend.viewmodel.GameViewModel;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Owns hand rendering and the local selected-card state for the main view.
 */
final class MainViewHandController {
    private final GameBoardView gameBoardView;
    private final AudioFeedbackService audioFeedbackService;
    private final HBox myHandPane;
    private final Label selectedCardLabel;

    private final List<Button> handCardButtons = new ArrayList<>();
    private List<String> renderedHandKeys = List.of();
    private int selectedHandIndex = -1;
    private Card selectedCard;

    MainViewHandController(GameBoardView gameBoardView, AudioFeedbackService audioFeedbackService,
                           HBox myHandPane, Label selectedCardLabel) {
        this.gameBoardView = gameBoardView;
        this.audioFeedbackService = audioFeedbackService;
        this.myHandPane = myHandPane;
        this.selectedCardLabel = selectedCardLabel;
    }

    int selectedHandIndex() {
        return selectedHandIndex;
    }

    Card selectedCard() {
        return selectedCard;
    }

    Button selectedHandButton() {
        if (selectedHandIndex < 0 || selectedHandIndex >= myHandPane.getChildren().size()) {
            return null;
        }
        var node = myHandPane.getChildren().get(selectedHandIndex);
        return node instanceof Button button ? button : null;
    }

    boolean ensureCardSelected(Label statusLabel) {
        if (selectedHandIndex >= 0) {
            return true;
        }
        statusLabel.setText("Please select a hand card first");
        return false;
    }

    void clear() {
        selectedHandIndex = -1;
        selectedCard = null;
        selectedCardLabel.setText("Selected card: none");
        if (!myHandPane.getChildren().isEmpty()) {
            myHandPane.getChildren().clear();
        }
        handCardButtons.clear();
        renderedHandKeys = List.of();
    }

    void render(GameViewModel gameViewModel, Runnable onSelectionChanged) {
        PlayerState me = gameViewModel.findMe();
        if (me == null) {
            clear();
            return;
        }
        if (selectedHandIndex >= me.hand().size()) {
            clear();
        }
        List<Card> hand = me.hand();
        List<String> handKeys = buildHandKeys(hand);
        if (handKeys.equals(renderedHandKeys) && myHandPane.getChildren().size() == hand.size()) {
            if (selectedHandIndex >= 0 && selectedHandIndex < hand.size()) {
                selectedCard = hand.get(selectedHandIndex);
            }
            updateSelectedCardLabel();
            refreshHandSelectionVisuals();
            return;
        }

        myHandPane.getChildren().clear();
        handCardButtons.clear();
        renderedHandKeys = handKeys;
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            Button button = buildHandCardButton(card, i, gameViewModel, onSelectionChanged);
            handCardButtons.add(button);
            myHandPane.getChildren().add(button);
        }
        refreshHandSelectionVisuals();
        updateSelectedCardLabel();
    }

    private Button buildHandCardButton(Card card, int idx, GameViewModel gameViewModel, Runnable onSelectionChanged) {
        Button button = new Button();
        button.setPrefSize(170, 240);
        button.setMinSize(170, 240);
        button.setMaxSize(170, 240);
        ImageView imageView = CardImageRegistry.buildCardImageView(card);
        if (imageView != null) {
            button.setGraphic(imageView);
        }
        button.setOnAction(event -> selectHandCard(idx, gameViewModel, onSelectionChanged));
        return button;
    }

    private void selectHandCard(int idx, GameViewModel gameViewModel, Runnable onSelectionChanged) {
        PlayerState me = gameViewModel.findMe();
        if (me == null || idx < 0 || idx >= me.hand().size()) {
            return;
        }
        if (selectedHandIndex != idx) {
            audioFeedbackService.playCardSelect();
        }
        selectedHandIndex = idx;
        selectedCard = me.hand().get(idx);
        updateSelectedCardLabel();
        refreshHandSelectionVisuals();
        onSelectionChanged.run();
    }

    private void refreshHandSelectionVisuals() {
        for (int i = 0; i < handCardButtons.size(); i++) {
            Button button = handCardButtons.get(i);
            boolean selected = i == selectedHandIndex;
            button.setStyle(gameBoardView.handImageStyle(selected));
            applyHandCardSelectionVisual(button, selected);
        }
    }

    private void updateSelectedCardLabel() {
        if (selectedCard == null || selectedHandIndex < 0) {
            selectedCardLabel.setText("Selected card: none");
            return;
        }
        selectedCardLabel.setText("Selected card: #" + selectedHandIndex + " " + selectedCard.name());
    }

    private List<String> buildHandKeys(List<Card> hand) {
        List<String> keys = new ArrayList<>(hand.size());
        for (Card card : hand) {
            keys.add(card.name() + "|" + card.type() + "|" + card.color() + "|" + card.bankValue());
        }
        return keys;
    }

    private void applyHandCardSelectionVisual(Button button, boolean selected) {
        double targetScale = selected ? 1.06 : 1.0;
        button.setScaleX(targetScale);
        button.setScaleY(targetScale);
        button.setViewOrder(selected ? -1 : 0);
    }
}
