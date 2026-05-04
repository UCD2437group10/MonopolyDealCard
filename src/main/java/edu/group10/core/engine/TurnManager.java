package edu.group10.core.engine;

import edu.group10.common.enums.GamePhase;
import edu.group10.common.model.GameEvent;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;
import edu.group10.core.model.TurnContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Turn manager
 * Manage switching turns, handle with overtime
 */
public class TurnManager {
    /**
     * Switch the turns manually
     */
    public List<GameEvent> nextTurn(InternalGameState state) {
        List<GameEvent> events = new ArrayList<>();

        if (state.getPhase() != GamePhase.PLAYING) {
            return events;
        }

        Player oldPlayer = state.getCurrentPlayer();
        state.nextTurn();
        Player newPlayer = state.getCurrentPlayer();

        events.add(GameEvent.turnEnded(
                oldPlayer != null ? oldPlayer.getPlayerId() : "unknown",
                newPlayer != null ? newPlayer.getPlayerId() : "unknown"
        ));

        return events;
    }

    /**
     * Check and handle with overtime
     */
    public List<GameEvent> handleTimeout(InternalGameState state) {
        List<GameEvent> events = new ArrayList<>();

        if (state.getPhase() != GamePhase.PLAYING) {
            return events;
        }

        TurnContext ctx = state.getTurnContext();

        if (ctx.isTimedOut()) {
            events.add(GameEvent.warning("The turn is overtime, the turn will be automatically switched."));
            events.addAll(nextTurn(state));
        }

        return events;
    }

    /**
     * Get the remaining time (millisecond)
     */
    public long getRemainingTimeMs(InternalGameState state) {
        TurnContext ctx = state.getTurnContext();
        long elapsed = System.currentTimeMillis() - ctx.getTurnStartTime();
        return Math.max(0, 60000 - elapsed);
    }

    /**
     * Get the number of remaining actions
     */
    public int getRemainingActions(InternalGameState state) {
        return state.getTurnContext().getActionsLeftInTurn();
    }

    /**
     * Compulsively end the current turn (used for special cases)
     */
    public List<GameEvent> forceEndTurn(InternalGameState state) {
        return nextTurn(state);
    }
}
