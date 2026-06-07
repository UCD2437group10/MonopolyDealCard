package edu.group10.monopolydeal.frontend.view;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.CardPropertyRules;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Creates and styles the small dialogs used by gameplay actions.
 */
public class GameDialogService {

    /** Lets the user choose a target player from the current snapshot. */
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

    /** Opens a generic color-selection dialog. */
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

    /** Lets the user choose one property card from a color group. */
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

    /** Opens a generic option picker and returns the chosen index. */
    public int chooseOptionIndex(String title, String dialogTitle, List<String> options, VBox ownerPane) {
        if (options == null || options.isEmpty()) {
            throw new IllegalStateException("No option available");
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
        styleDialog(dialog, ownerPane);
        dialog.setTitle(dialogTitle);
        dialog.setHeaderText(title);
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            throw new IllegalStateException("No option selected");
        }
        return options.indexOf(result.get());
    }

    /** Asks how many Double The Rent cards to consume. */
    public String chooseDoubleRentCount(VBox ownerPane) {
        List<String> options = List.of("0", "1", "2");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("0", options);
        styleDialog(dialog, ownerPane);
        dialog.setTitle("Rent Multiplier");
        dialog.setHeaderText("Select Double The Rent usage count");
        return dialog.showAndWait().orElse(null);
    }

    /** Asks for the color used when playing a multi-property card. */
    public String choosePropertyColor(Card card, VBox ownerPane) {
        if (card == null || card.type() != CardType.MULTI_PROPERTY) {
            return "";
        }
        return chooseColorFromList("Select color for property", CardPropertyRules.allowedPropertyColors(card), ownerPane);
    }

    /** Asks for the destination color when moving a multi-property card. */
    public String choosePropertyColorForMove(Card card, String currentColor, VBox ownerPane) {
        if (card == null || card.type() != CardType.MULTI_PROPERTY) {
            return null;
        }
        List<String> colors = CardPropertyRules.allowedPropertyColors(card).stream()
                .filter(color -> !color.equals(currentColor))
                .toList();
        return chooseColorFromList("Select new color for property", colors, ownerPane);
    }

    /** Resolves or asks for the color used by a rent card. */
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
            return chooseColorFromList("Select rent color", java.util.Arrays.stream(color.split("/")).map(String::trim).toList(), ownerPane);
        }
        return color;
    }

    /** Applies the shared dialog theme and owner window. */
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
