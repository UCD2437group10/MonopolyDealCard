package edu.group10.monopolydeal.frontend.view;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * A centralized UI service responsible for creating, configuring, and displaying modal dialogs.
 * It captures user input during gameplay operations, such as selecting target opponents,
 * picking property colors, or confirming card usage.
 */
public class GameDialogService {

    /**
     * Displays a dialog prompting the current player to select an opponent as a target for a game action.
     *
     * @param players           the list of all current {@link PlayerState} objects in the game.
     * @param myPlayerId        the ID of the local player, which will be excluded from the options.
     * @param preferredTargetId the ID of an opponent to pre-select by default, if present in the list.
     * @param ownerPane         the parent {@link VBox} used to determine the dialog's modality and owner window.
     * @return the ID of the selected target player, or {@code null} if the dialog is canceled or no valid opponents exist.
     */
    public String chooseTargetPlayerId(List<PlayerState> players, String myPlayerId, String preferredTargetId, VBox ownerPane) {
        List<String> options = players == null ? List.of() : players.stream()
                .map(ps -> ps.player().id())
                .filter(id -> !id.equals(myPlayerId))
                .toList();
        if (options.isEmpty()) {
            return null;
        }
        String initial = options.contains(preferredTargetId) ? preferredTargetId : options.get(0);
        ChoiceDialog<String> dialog = new ChoiceDialog<>(initial, options);
        styleDialog(dialog, ownerPane);
        dialog.setTitle("Select Target Player");
        dialog.setHeaderText("Please select a target player");
        return dialog.showAndWait().orElse(null);
    }

    /**
     * Displays a generic dialog allowing the user to select a color string from a provided list.
     *
     * @param title     the header text to display in the dialog.
     * @param colors    the list of valid color strings to choose from.
     * @param ownerPane the parent {@link VBox} used to determine the dialog's owner window.
     * @return the selected color string, or {@code null} if the dialog is canceled or the list is empty.
     */
    public String chooseColorFromList(String title, List<String> colors, VBox ownerPane) {
        if (colors == null || colors.isEmpty()) {
            return null;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(colors.get(0), colors);
        styleDialog(dialog, ownerPane);
        dialog.setTitle("Select Color");
        dialog.setHeaderText(title);
        return dialog.showAndWait().orElse(null);
    }

    /**
     * Displays a dialog prompting the user to select a specific property card from a given list.
     * Options are presented dynamically with their list index and corresponding card name.
     *
     * @param title     the header text to display in the dialog.
     * @param cards     the list of available property {@link Card} objects.
     * @param ownerPane the parent {@link VBox} used to determine the dialog's owner window.
     * @return the integer index of the selected property within the provided list.
     * @throws IllegalStateException if the provided list is empty, or if the user cancels the dialog.
     */
    public int choosePropertyIndex(String title, List<Card> cards, VBox ownerPane) {
        if (cards == null || cards.isEmpty()) {
            throw new IllegalStateException("No property available under selected color");
        }
        List<String> options = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            options.add(i + ": " + cards.get(i).name());
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
        styleDialog(dialog, ownerPane);
        dialog.setTitle("Select Property");
        dialog.setHeaderText(title);
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            throw new IllegalStateException("No property selected");
        }
        return Integer.parseInt(result.get().split(":")[0].trim());
    }

    /**
     * Displays a specialized dialog asking the user how many "Double The Rent" cards they wish to stack and play.
     *
     * @param ownerPane the parent {@link VBox} used to determine the dialog's owner window.
     * @return a string representing the chosen count ("0", "1", or "2"). Returns "0" if the dialog is canceled.
     */
    public String chooseDoubleRentCount(VBox ownerPane) {
        List<String> options = List.of("0", "1", "2");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("0", options);
        styleDialog(dialog, ownerPane);
        dialog.setTitle("Rent Multiplier");
        dialog.setHeaderText("Select Double The Rent usage count");
        return dialog.showAndWait().orElse("0");
    }

    /**
     * Prompts the user to select the active color for a multi-color property card being played.
     * Automatically handles both 10-color "Wild" cards and standard dual-color cards by parsing the color string.
     *
     * @param card      the multi-color property {@link Card} being played.
     * @param ownerPane the parent {@link VBox} used to determine the dialog's owner window.
     * @return the selected color string, or an empty string if the card is invalid or the dialog is canceled.
     */
    public String choosePropertyColor(Card card, VBox ownerPane) {
        if (card == null || card.type() != CardType.MULTI_PROPERTY) {
            return "";
        }
        if ("Wild".equalsIgnoreCase(card.color())) {
            return chooseColorFromList("Select color for wild property", List.of(
                    "Brown", "Light Blue", "Pink", "Orange", "Red", "Yellow", "Green", "Deep Blue", "Railroad", "Utility"), ownerPane);
        }
        List<String> colors = Arrays.stream(card.color().split("/")).map(String::trim).toList();
        return chooseColorFromList("Select color for dual-color property", colors, ownerPane);
    }

    /**
     * Determines the active color used when playing a rent card.
     * For wild rent cards ("Any") or dual rent cards ("ColorA/ColorB"), it prompts the user to select
     * the desired color. Standard mono-color rent cards are resolved automatically.
     *
     * @param card      the rent {@link Card} being played.
     * @param me        the current {@link PlayerState}, used to restrict "Wild Rent" options to owned property colors.
     * @param ownerPane the parent {@link VBox} used to determine the dialog's owner window.
     * @return the resolved or selected color string, or an empty string if invalid or canceled.
     */
    public String chooseRentColor(Card card, PlayerState me, VBox ownerPane) {
        if (card == null) {
            return "";
        }
        String color = card.color();
        if (color == null || color.isBlank() || "-".equals(color)) {
            return "";
        }
        if ("Any".equalsIgnoreCase(color)) {
            return chooseColorFromList("Select rent color", me == null ? List.of() : List.copyOf(me.properties().keySet()), ownerPane);
        }
        if (color.contains("/")) {
            return chooseColorFromList("Select rent color", Arrays.stream(color.split("/")).map(String::trim).toList(), ownerPane);
        }
        return color;
    }

    /**
     * Applies a standardized custom CSS theme and window ownership properties to a given dialog.
     * This ensures all popups match the application's overall visual aesthetic and modal behavior.
     *
     * @param dialog    the JavaFX {@link Dialog} instance to style.
     * @param ownerPane the parent {@link VBox} used to extract the parent {@link Window} hierarchy.
     */
    public void styleDialog(Dialog<?> dialog, VBox ownerPane) {
        if (dialog == null || dialog.getDialogPane() == null) {
            return;
        }
        String css = GameDialogService.class.getResource("/css/dialog-theme.css") == null
                ? null
                : GameDialogService.class.getResource("/css/dialog-theme.css").toExternalForm();
        if (css != null) {
            dialog.getDialogPane().getStylesheets().add(css);
        }
        dialog.getDialogPane().getStyleClass().add("md-dialog");
        Window owner = ownerPane == null || ownerPane.getScene() == null ? null : ownerPane.getScene().getWindow();
        if (owner != null) {
            dialog.initOwner(owner);
        }
    }
}