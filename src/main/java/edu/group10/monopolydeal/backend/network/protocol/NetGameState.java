package edu.group10.monopolydeal.backend.network.protocol;

import java.util.List;
import java.util.Set;

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
        List<NetPlayerState> players,
        Set<String> readyPlayerIds
) {
}
