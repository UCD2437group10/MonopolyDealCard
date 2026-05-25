package edu.group10.monopolydeal.frontend.view;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * 游戏主界面视图渲染器。
 */
public class GameBoardView {

    public void renderMyBank(FlowPane myBankPane, PlayerState me) {
        myBankPane.getChildren().clear();
        if (me == null) {
            return;
        }
        for (Card card : me.bank()) {
            Button button = cardButton(card.name() + "\n$" + card.bankValue(), "money", false, 110, 50);
            button.setDisable(true);
            myBankPane.getChildren().add(button);
        }
    }

    public void renderMyProperties(VBox myPropertyBox, PlayerState me, ToIntFunction<String> requiredSetSize,
                                   PropertyClickHandler clickHandler) {
        myPropertyBox.getChildren().clear();
        if (me == null) {
            return;
        }
        if (me.properties().isEmpty()) {
            myPropertyBox.getChildren().add(new Label("No properties"));
            return;
        }
        for (Map.Entry<String, List<Card>> entry : me.properties().entrySet()) {
            String color = entry.getKey();
            List<Card> cards = entry.getValue();
            int house = me.houseByColor().getOrDefault(color, 0);
            int hotel = me.hotelByColor().getOrDefault(color, 0);
            int need = requiredSetSize.applyAsInt(color);
            int progress = Math.min(cards.size(), need);
            Button stackButton = cardButton(color + "  " + progress + "/" + need + "  H:" + house + " T:" + hotel,
                    color, false, 320, 34);
            stackButton.setOnAction(event -> clickHandler.onClick(color, cards, house, hotel));
            myPropertyBox.getChildren().add(stackButton);
        }
    }

    public void renderOpponents(FlowPane opponentsPane, List<PlayerState> players, String meId,
                                Consumer<String> onSetTarget, Consumer<PlayerState> onShowDetail) {
        opponentsPane.getChildren().clear();
        if (players == null) {
            return;
        }
        for (PlayerState p : players) {
            if (p.player().id().equals(meId)) {
                continue;
            }
            VBox card = new VBox(4);
            card.setStyle("-fx-background-color: rgba(212,175,55,0.08); -fx-border-color: #7a6320; -fx-border-width: 1; -fx-padding: 6;");
            card.setPrefWidth(250);
            Label name = new Label(p.player().displayName() + " (" + p.player().id() + ")");
            name.setStyle("-fx-text-fill: #d4af37; -fx-font-weight: bold;");
            Label hand = new Label("Hand: " + p.hand().size() + " | Bank: $" + p.bankTotal() + " | Property groups: " + p.properties().size());
            hand.setStyle("-fx-text-fill: #e3cf8a;");
            Button detailBtn = new Button("View Assets");
            detailBtn.setOnAction(event -> onShowDetail.accept(p));
            card.getChildren().addAll(name, hand, detailBtn);
            opponentsPane.getChildren().add(card);
        }
    }

    public String handImageStyle(boolean selected) {
        return "-fx-background-color: transparent;"
                + "-fx-border-color: transparent;"
                + "-fx-padding: 0;"
                + (selected ? "-fx-opacity: 1.0;" : "-fx-opacity: 0.9;");
    }

    private Button cardButton(String text, String color, boolean selected, double width, double height) {
        Button button = new Button(text);
        button.setPrefSize(width, height);
        button.setStyle(cardStyle(color, selected));
        return button;
    }

    private String cardStyle(String color, boolean selected) {
        String bg = switch (normalizeColor(color)) {
            case "blue", "deepblue" -> "#1f2a44";
            case "green" -> "#1f3a2f";
            case "yellow" -> "#5d4a1f";
            case "red" -> "#4a1f1f";
            case "orange" -> "#4f3218";
            case "pink" -> "#4a2740";
            case "brown" -> "#3a2a1b";
            case "lightblue" -> "#22404f";
            case "railroad" -> "#3a3a3a";
            case "utility" -> "#2f3238";
            case "money" -> "#2a3b24";
            default -> "#2a2520";
        };
        String border = selected ? "#f0d57a" : "#7a6320";
        String width = selected ? "3" : "1";
        return "-fx-background-color: " + bg + ";"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: " + width + ";"
                + "-fx-text-fill: #e7d39a;"
                + "-fx-font-size: 11px;"
                + "-fx-wrap-text: true;";
    }

    private String normalizeColor(String color) {
        if (color == null) {
            return "";
        }
        return color.toLowerCase().replace(" ", "").replace("-", "").replace("_", "");
    }

    @FunctionalInterface
    public interface PropertyClickHandler {
        void onClick(String color, List<Card> cards, int house, int hotel);
    }
}
