package edu.group10.monopolydeal.frontend.controller;

import javafx.scene.control.Label;

/**
 * Tracks connection state and formats network error messages for the UI.
 */
final class NetworkErrorHandler {
    private boolean connected;

    /** Returns whether the UI currently considers the connection healthy. */
    boolean isConnected() {
        return connected;
    }

    /** Marks the connection as healthy and clears reconnect text. */
    void markConnected(Label reconnectLabel) {
        connected = true;
        if (reconnectLabel != null) {
            reconnectLabel.setText("");
        }
    }

    /** Marks the connection as unavailable. */
    void markDisconnected() {
        connected = false;
    }

    /** Handles a connect-time failure and updates the menu status label. */
    void handleConnectFailure(Exception exception, Label menuStatusLabel) {
        connected = false;
        if (menuStatusLabel != null) {
            menuStatusLabel.setText("Connection failed: " + exception.getMessage());
        }
    }

    /** Handles a polling failure and updates the reconnect label. */
    void handlePollingFailure(Exception exception, Label reconnectLabel) {
        connected = false;
        if (reconnectLabel != null) {
            reconnectLabel.setText("Connection error, retrying: " + exception.getMessage());
        }
    }
}
