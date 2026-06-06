package edu.group10.common.model;

import java.util.ArrayList;
import java.util.List;

public class GameActionResult {
    private boolean success;
    private String errorCode;
    private String errorMessage;
    private GameState newState; //State after action
    private List<GameEvent> events; //Event during the process (for animation)
    private int actionSequence;

    public GameActionResult() {
        this.events = new ArrayList<>();
    }

    //Factory method of success
    public static GameActionResult success(GameState newState, List<GameEvent> events) {
        GameActionResult result = new GameActionResult();
        result.setSuccess(true);
        result.setNewState(newState);
        result.setEvents(events);
        return result;
    }

    //Factory method of failure
    public static GameActionResult failure(String errorCode, String errorMessage) {
        GameActionResult result = new GameActionResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }

    //Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public GameState getNewState() { return newState; }
    public void setNewState(GameState newState) { this.newState = newState; }

    public List<GameEvent> getEvents() { return events; }
    public void setEvents(List<GameEvent> events) { this.events = events; }

    public int getActionSequence() { return actionSequence; }
    public void setActionSequence(int actionSequence) { this.actionSequence = actionSequence; }

    @Override
    public String toString() {
        return "GameActionResult{" +
                "success=" + success +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", newState=" + newState +
                ", events=" + events +
                '}';
    }
}
