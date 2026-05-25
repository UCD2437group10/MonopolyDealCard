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

public class GameDialogService {

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

    public String chooseDoubleRentCount(VBox ownerPane) {
        List<String> options = List.of("0", "1", "2");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("0", options);
        styleDialog(dialog, ownerPane);
        dialog.setTitle("Rent Multiplier");
        dialog.setHeaderText("Select Double The Rent usage count");
        return dialog.showAndWait().orElse("0");
    }

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
