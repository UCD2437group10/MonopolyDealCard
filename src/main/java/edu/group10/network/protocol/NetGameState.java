package edu.group10.network.protocol;

import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import java.util.List;
import java.util.Set;

/**
 * Network-safe snapshot of the full game state.
 */
public record NetGameState(
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
        List<SimpleCard> discardPileCards,
        List<NetPlayerState> players,
        Set<String> readyPlayerIds
) {
}
