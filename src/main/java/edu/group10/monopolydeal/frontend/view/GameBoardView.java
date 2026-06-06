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
 * A view utility class responsible for rendering reusable components of the main game board UI.
 * It handles the visual representation of a player's bank, property sets, and opponent summaries
 * using JavaFX layout containers and dynamically styled controls.
 */
public class GameBoardView {

    /**
     * Renders the current player's bank cards within the specified container.
     * Displays each monetary asset as a disabled button indicating the card's name and its deposited value.
     *
     * @param myBankPane the {@link FlowPane} container where the bank cards will be rendered.
     * @param me         the {@link PlayerState} representing the current player; if {@code null}, the pane is cleared.
     */
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

    /**
     * Renders the current player's grouped properties within the specified container.
     * Displays each property group (organized by color) as an interactive button summarizing the group's progress,
     * including the number of collected cards versus the required set size, and any attached houses or hotels.
     *
     * @param myPropertyBox   the {@link VBox} container where the property groups will be vertically stacked.
     * @param me              the {@link PlayerState} representing the current player.
     * @param requiredSetSize a function mapping a property color string to its required full set size.
     * @param clickHandler    the callback invoked when a specific property group button is clicked.
     */
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

    /**
     * Renders summary cards for all opponent players within the specified container.
     * Each summary card provides a brief overview of an opponent's current state (hand size, total bank value,
     * and number of property groups) and includes an interactive button to view further details.
     *
     * @param opponentsPane the {@link FlowPane} container where the opponent summaries will be rendered.
     * @param players       the complete list of {@link PlayerState} objects representing all players in the game.
     * @param meId          the unique identifier of the local player, used to filter them out of the rendered list.
     * @param onSetTarget   a callback intended to handle targeting an opponent for specific card actions.
     * @param onShowDetail  a callback invoked when the "View Assets" button is clicked for a specific opponent.
     */
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

    /**
     * Generates the base CSS style string used for rendering hand card elements.
     *
     * @param selected {@code true} if the card is currently selected (fully opaque), {@code false} otherwise (slightly transparent).
     * @return the formatted CSS style string.
     */
    public String handImageStyle(boolean selected) {
        return "-fx-background-color: transparent;"
                + "-fx-border-color: transparent;"
                + "-fx-padding: 0;"
                + (selected ? "-fx-opacity: 1.0;" : "-fx-opacity: 0.9;");
    }

    /**
     * Helper method to construct a standard styled button representing a card or a property group.
     *
     * @param text     the label text to display on the button.
     * @param color    the logical color category of the card (used to determine background color).
     * @param selected {@code true} to render with a highlighted selection border.
     * @param width    the preferred width of the button.
     * @param height   the preferred height of the button.
     * @return a fully configured JavaFX {@link Button}.
     */
    private Button cardButton(String text, String color, boolean selected, double width, double height) {
        Button button = new Button(text);
        button.setPrefSize(width, height);
        button.setStyle(cardStyle(color, selected));
        return button;
    }

    /**
     * Resolves the CSS styling block for a specific card based on its color category and selection state.
     *
     * @param color    the logical color string of the property/card.
     * @param selected {@code true} to apply a highlighted golden border, {@code false} for standard borders.
     * @return the CSS style string.
     */
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

    /**
     * Normalizes a raw color string by converting it to lowercase and stripping all spaces and common delimiters.
     *
     * @param color the raw color string (e.g., "Light Blue").
     * @return the normalized identifier (e.g., "lightblue").
     */
    private String normalizeColor(String color) {
        if (color == null) {
            return "";
        }
        return color.toLowerCase().replace(" ", "").replace("-", "").replace("_", "");
    }

    /**
     * A functional interface used as a callback when a player interacts with a rendered property group.
     */
    @FunctionalInterface
    public interface PropertyClickHandler {
        /**
         * Invoked when a property group stack is clicked.
         *
         * @param color the color category of the clicked property group.
         * @param cards the list of cards currently residing in that property group.
         * @param house the number of house improvements currently attached to the group.
         * @param hotel the number of hotel improvements currently attached to the group.
         */
        void onClick(String color, List<Card> cards, int house, int hotel);
    }
}