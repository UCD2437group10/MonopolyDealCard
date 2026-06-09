package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.game.GameState;
import java.util.function.Consumer;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Owns menu state, mode toggles, and menu-related UI helpers.
 */
final class MainViewMenuController {
    private final VBox menuPane;
    private final VBox gamePane;
    private final VBox multiConfigPane;
    private final Button singleModeButton;
    private final Button multiModeButton;
    private final Label menuStatusLabel;
    private final TextField menuHostField;
    private final TextField menuPortField;
    private final TextField menuPlayerIdField;
    private final TextField menuPlayerNameField;
    private final CheckBox menuBotCheckBox;
    private final CheckBox menuHostServerCheckBox;

    private String selectedMode = "single";

    MainViewMenuController(VBox menuPane, VBox gamePane, VBox multiConfigPane,
                           Button singleModeButton, Button multiModeButton, Label menuStatusLabel,
                           TextField menuHostField, TextField menuPortField, TextField menuPlayerIdField,
                           TextField menuPlayerNameField, CheckBox menuBotCheckBox,
                           CheckBox menuHostServerCheckBox) {
        this.menuPane = menuPane;
        this.gamePane = gamePane;
        this.multiConfigPane = multiConfigPane;
        this.singleModeButton = singleModeButton;
        this.multiModeButton = multiModeButton;
        this.menuStatusLabel = menuStatusLabel;
        this.menuHostField = menuHostField;
        this.menuPortField = menuPortField;
        this.menuPlayerIdField = menuPlayerIdField;
        this.menuPlayerNameField = menuPlayerNameField;
        this.menuBotCheckBox = menuBotCheckBox;
        this.menuHostServerCheckBox = menuHostServerCheckBox;
    }

    String selectedMode() {
        return selectedMode;
    }

    void selectSingleMode() {
        selectedMode = "single";
        setMenuStatus("Selected: Single Player (default local 127.0.0.1:18080)");
    }

    void selectMultiMode() {
        selectedMode = "multi";
        setMenuStatus("Selected: Multiplayer (host locally or join as client)");
    }

    // Update menu-only controls based on the selected mode and game phase.
    void applyModeUi(GameState currentState, Button readyButton, Button startButton) {
        boolean multi = "multi".equals(selectedMode);
        boolean showLobbyControls = multi && (currentState == null || !currentState.started());
        multiConfigPane.setVisible(multi);
        multiConfigPane.setManaged(multi);
        if (readyButton != null) {
            readyButton.setVisible(showLobbyControls);
            readyButton.setManaged(showLobbyControls);
        }
        if (startButton != null) {
            startButton.setVisible(showLobbyControls);
            startButton.setManaged(showLobbyControls);
        }
        singleModeButton.setStyle(multi ? "" : "-fx-font-weight: bold;");
        multiModeButton.setStyle(multi ? "-fx-font-weight: bold;" : "");
    }

    // Collect menu inputs into one immutable entry configuration.
    EntryConfig buildEntryConfig() {
        if ("multi".equals(selectedMode)) {
            return new EntryConfig(
                    nonEmpty(menuHostField.getText(), "127.0.0.1"),
                    parseInt(menuPortField.getText(), 18080),
                    nonEmpty(menuPlayerIdField.getText(), "p1"),
                    nonEmpty(menuPlayerNameField.getText(), nonEmpty(menuPlayerIdField.getText(), "p1")),
                    menuBotCheckBox != null && menuBotCheckBox.isSelected(),
                    menuHostServerCheckBox != null && menuHostServerCheckBox.isSelected());
        }
        return new EntryConfig("127.0.0.1", 18080, "p1", "Player1", false, false);
    }

    // Switch from the menu to the main game pane and clear reconnect warnings.
    void showGamePane(Label statusLabel, Consumer<String> actionLogSink, Label reconnectLabel) {
        menuPane.setVisible(false);
        menuPane.setManaged(false);
        gamePane.setVisible(true);
        gamePane.setManaged(true);
        statusLabel.setText("Success: entered game view");
        actionLogSink.accept("Entered game successfully");
        reconnectLabel.setText("");
    }

    // Return to the menu while keeping the current mode selection.
    void backToMenu() {
        menuPane.setVisible(true);
        menuPane.setManaged(true);
        gamePane.setVisible(false);
        gamePane.setManaged(false);
        setMenuStatus("Returned to lobby");
    }

    // Restore the default menu state after leaving a room or finishing a game.
    void restoreInitialMenuState() {
        selectedMode = "single";
        if (menuHostField != null) {
            menuHostField.setText("127.0.0.1");
        }
        if (menuPortField != null) {
            menuPortField.setText("18080");
        }
        if (menuPlayerIdField != null) {
            menuPlayerIdField.setText("p1");
        }
        if (menuPlayerNameField != null) {
            menuPlayerNameField.setText("Player1");
        }
        if (menuBotCheckBox != null) {
            menuBotCheckBox.setSelected(false);
        }
        if (menuHostServerCheckBox != null) {
            menuHostServerCheckBox.setSelected(false);
        }
        backToMenu();
    }

    void setMenuStatus(String message) {
        menuStatusLabel.setText(message);
    }

    Label menuStatusLabel() {
        return menuStatusLabel;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    record EntryConfig(String host, int port, String playerId, String playerName, boolean bot, boolean hostServer) {
    }
}
