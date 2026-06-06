package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.List;
import java.util.Set;

/**
 * Game snapshot for frontend
 */
public record GameState (

        boolean started,
        boolean gameOver,
        String winnerPlayerId,
        String currentPlayerId,
        String jsnResponderPlayerId,
        String jsnActorPlayerId,
        String jsnTargetPlayerId,
        String jsnSourceAction,
        int drawPileCount,
        int discardPileCount,
        List<PlayerState> players,
        Set<String> readyPlayerIds
) {
}
