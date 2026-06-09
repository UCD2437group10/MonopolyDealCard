package edu.group10.monopolydeal.frontend.view;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * Presents a manual payment picker for human players.
 */
public final class PaymentSelectionDialog {

    private final GameDialogService dialogService;

    public PaymentSelectionDialog(GameDialogService dialogService) {
        this.dialogService = dialogService;
    }

    public Map<String, String> show(PlayerState payer, String collectorPlayerId, int amount, String sourceAction, VBox ownerPane) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        ButtonType confirmType = new ButtonType("Confirm Payment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);
        dialogService.styleDialog(dialog, ownerPane);
        dialog.setTitle("Select Payment");
        dialog.setHeaderText("Pay " + collectorPlayerId + " $" + amount + " for " + sourceAction);

        List<AssetOption> options = buildOptions(payer);
        Label totalLabel = new Label();
        totalLabel.getStyleClass().add("payment-total-label");
        VBox optionBox = new VBox(8);
        optionBox.setPadding(new Insets(8, 0, 0, 0));
        optionBox.getStyleClass().add("payment-option-box");
        for (AssetOption option : options) {
            optionBox.getChildren().add(option.checkBox());
        }
        ScrollPane scrollPane = new ScrollPane(optionBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(Math.min(320, Math.max(120, options.size() * 34)));
        scrollPane.getStyleClass().add("payment-scroll-pane");

        Label hintLabel = new Label("Select money and property cards until the total is enough. If you cannot reach the amount, select all assets.");
        hintLabel.setWrapText(true);
        VBox content = new VBox(10,
                hintLabel,
                totalLabel,
                scrollPane);
        content.getStyleClass().add("payment-dialog-content");
        dialog.getDialogPane().setContent(content);

        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmType);
        ChangeListener<Boolean> listener = (obs, oldValue, newValue) -> updateState(options, amount, totalLabel, confirmButton);
        for (AssetOption option : options) {
            option.checkBox().selectedProperty().addListener(listener);
        }
        updateState(options, amount, totalLabel, confirmButton);

        dialog.setResultConverter(buttonType -> buttonType == confirmType ? toPayload(options) : null);
        return dialog.showAndWait().orElse(null);
    }

    private void updateState(List<AssetOption> options, int amount, Label totalLabel, Button confirmButton) {
        int total = options.stream().filter(option -> option.checkBox().isSelected()).mapToInt(AssetOption::value).sum();
        long selectedCount = options.stream().filter(option -> option.checkBox().isSelected()).count();
        boolean allowConfirm = total >= amount || selectedCount == options.size();
        totalLabel.setText("Selected total: $" + total + " / $" + amount
                + (allowConfirm ? "" : "  (choose more assets)"));
        confirmButton.setDisable(!allowConfirm);
    }

    private Map<String, String> toPayload(List<AssetOption> options) {
        String bankIndexes = options.stream()
                .filter(option -> option.zone() == AssetZone.BANK && option.checkBox().isSelected())
                .map(option -> String.valueOf(option.index()))
                .collect(Collectors.joining(","));
        String propertyRefs = options.stream()
                .filter(option -> option.zone() == AssetZone.PROPERTY && option.checkBox().isSelected())
                .map(option -> option.color() + "@" + option.index())
                .collect(Collectors.joining(";"));
        return Map.of(
                "bankIndexes", bankIndexes,
                "propertyRefs", propertyRefs
        );
    }

    private List<AssetOption> buildOptions(PlayerState payer) {
        List<AssetOption> options = new ArrayList<>();
        for (int i = 0; i < payer.bank().size(); i++) {
            Card card = payer.bank().get(i);
            int value = card.bankValue();
            options.add(new AssetOption(
                    AssetZone.BANK,
                    "",
                    i,
                    value,
                    paymentCheckBox("Bank: " + card.name() + " ($" + value + ")")
            ));
        }
        payer.properties().forEach((color, cards) -> {
            for (int i = 0; i < cards.size(); i++) {
                Card card = cards.get(i);
                int value = card.bankValue();
                options.add(new AssetOption(
                    AssetZone.PROPERTY,
                    color,
                    i,
                    value,
                    paymentCheckBox("Property [" + color + "]: " + card.name() + " ($" + value + ")")
                ));
            }
        });
        return options;
    }

    private CheckBox paymentCheckBox(String text) {
        CheckBox checkBox = new CheckBox(text);
        checkBox.getStyleClass().add("payment-check-box");
        checkBox.setWrapText(true);
        return checkBox;
    }

    private enum AssetZone {
        BANK,
        PROPERTY
    }

    private record AssetOption(AssetZone zone, String color, int index, int value, CheckBox checkBox) {
    }
}
