package edu.group10.core.model;

import edu.group10.core.card.action.Suit;

/**
 * Context of turns
 * Record the temporary state
 */

public class TurnContext {
    private int actionsLeftInTurn; //The lasting number of actions that can be executed in the current turn (at most 3)
    private long turnStartTime;
    private boolean isWaitingForResponse;
    private String waitingPlayerId;
    private String pendingTargetPlayerId; //The target players that is temporarily saved in multiple operations
    private String pendingCardId; //The ID of cards that is temporarily saved in multiple operations
    private String pendingPropertyId; //The ID of properties that is temporarily saved in multiple operations
    private boolean canDrawExtraCard; //Whether the player can draw extra cards (used by Pass Go)
    private Suit lastRentSuit;

    private static final int MAX_ACTIONS_PER_TURN = 3;
    private static final int TURN_TIMEOUT_MS = 60000; //60 secs

    public TurnContext() {
        this.actionsLeftInTurn = MAX_ACTIONS_PER_TURN;
        this.turnStartTime = System.currentTimeMillis();
        this.isWaitingForResponse = false;
        this.canDrawExtraCard = false;
    }

    public void startNewTurn() {
        this.actionsLeftInTurn = MAX_ACTIONS_PER_TURN;
        this.turnStartTime = System.currentTimeMillis();
        this.isWaitingForResponse = false;
        this.canDrawExtraCard = false;
        this.lastRentSuit = null;
        clearPending();
    }

    public void decrementActionsLeft() {
        if (actionsLeftInTurn > 0) {
            actionsLeftInTurn--;
        }
    }

    public boolean hasActionsLeft() {
        return actionsLeftInTurn > 0;
    }

    public boolean isTimedOut() {
        return System.currentTimeMillis() - turnStartTime > TURN_TIMEOUT_MS;
    }

    public boolean hasPendingAction() {
        return pendingTargetPlayerId != null || pendingCardId != null;
    }

    public void clearPending() {
        this.pendingTargetPlayerId = null;
        this.pendingCardId = null;
        this.pendingPropertyId = null;
    }

    //Getters and Setters
    public int getActionsLeftInTurn() { return actionsLeftInTurn; }
    public void setActionsLeftInTurn(int actionsLeftInTurn) { this.actionsLeftInTurn = actionsLeftInTurn; }

    public long getTurnStartTime() { return turnStartTime; }
    public void setTurnStartTime(long turnStartTime) { this.turnStartTime = turnStartTime; }

    public boolean isWaitingForResponse() { return isWaitingForResponse; }
    public void setWaitingForResponse(boolean waitingForResponse) { this.isWaitingForResponse = waitingForResponse; }

    public String getWaitingPlayerId() { return waitingPlayerId; }
    public void setWaitingPlayerId(String waitingPlayerId) { this.waitingPlayerId = waitingPlayerId; }

    public String getPendingTargetPlayerId() { return pendingTargetPlayerId; }
    public void setPendingTargetPlayerId(String pendingTargetPlayerId) { this.pendingTargetPlayerId = pendingTargetPlayerId; }

    public String getPendingCardId() { return pendingCardId; }
    public void setPendingCardId(String pendingCardId) { this.pendingCardId = pendingCardId; }

    public String getPendingPropertyId() { return pendingPropertyId; }
    public void setPendingPropertyId(String pendingPropertyId) { this.pendingPropertyId = pendingPropertyId; }

    public boolean isCanDrawExtraCard() { return canDrawExtraCard; }
    public void setCanDrawExtraCard(boolean canDrawExtraCard) { this.canDrawExtraCard = canDrawExtraCard; }

    public Suit getLastRentSuit() { return lastRentSuit; }
    public void setLastRentSuit(Suit lastRentSuit) { this.lastRentSuit = lastRentSuit; }
}
