package edu.group10.monopolydeal.frontend.controller;

import javafx.scene.control.Label;

final class NetworkErrorHandler {
    private boolean connected;

    boolean isConnected() {
        return connected;
    }

    void markConnected(Label reconnectLabel) {
        connected = true;
        if (reconnectLabel != null) {
            reconnectLabel.setText("");
        }
    }

    void markDisconnected() {
        connected = false;
    }

    void handleConnectFailure(Exception exception, Label menuStatusLabel) {
        connected = false;
        if (menuStatusLabel != null) {
            menuStatusLabel.setText("Connection failed: " + exception.getMessage());
        }
    }

    void handlePollingFailure(Exception exception, Label reconnectLabel) {
        connected = false;
        if (reconnectLabel != null) {
            reconnectLabel.setText("Connection error, retrying: " + exception.getMessage());
        }
    }
}
