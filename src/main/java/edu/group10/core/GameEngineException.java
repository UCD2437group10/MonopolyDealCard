package edu.group10.core;

/**
 * Game engine exception
 *
 * It will be thrown when there is regularized errors while executing actions
 * Infra can catch this exception and change it to error response to the front end
 */

public class GameEngineException extends Exception {

    private String errorCode;

    public GameEngineException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public GameEngineException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
