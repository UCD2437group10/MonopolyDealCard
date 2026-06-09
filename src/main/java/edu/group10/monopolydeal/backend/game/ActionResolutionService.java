package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.List;
import java.util.Map;

/**
 * Dispatches action cards to their rule-specific resolvers.
 */
final class ActionResolutionService {
    private final Map<String, PlayerState> players;
    private final TurnManager turnManager;
    private final JustSayNoResolver justSayNoResolver;
    private final VictoryManager victoryManager;

    ActionResolutionService(
            Map<String, PlayerState> players,
            TurnManager turnManager,
            JustSayNoResolver justSayNoResolver,
            VictoryManager victoryManager
    ) {
        this.players = players;
        this.turnManager = turnManager;
        this.justSayNoResolver = justSayNoResolver;
        this.victoryManager = victoryManager;
    }

    // Route each action card to the matching rule handler.
    void resolve(String playerId, Card actionCard, Map<String, String> payload, List<String> turnOrder) {
        String targetId = payload.getOrDefault("targetPlayerId", "");
        switch (actionCard.name()) {
            case "Pass Go" -> turnManager.drawCards(playerId, 2);
            case "Debt Collector" -> justSayNoResolver.startPendingJsn(playerId, actionCard.name(), PendingEffectType.DEBT_COLLECTOR, payload, List.of(targetId));
            case "It's My Birthday" -> {
                List<String> targets = turnOrder.stream().filter(id -> !id.equals(playerId)).toList();
                justSayNoResolver.startPendingJsn(playerId, actionCard.name(), PendingEffectType.ITS_MY_BIRTHDAY, payload, targets);
            }
            case "Sly Deal" -> justSayNoResolver.startPendingJsn(playerId, actionCard.name(), PendingEffectType.SLY_DEAL, payload, List.of(targetId));
            case "Forced Deal" -> justSayNoResolver.startPendingJsn(playerId, actionCard.name(), PendingEffectType.FORCED_DEAL, payload, List.of(targetId));
            case "Deal Breaker" -> justSayNoResolver.startPendingJsn(playerId, actionCard.name(), PendingEffectType.DEAL_BREAKER, payload, List.of(targetId));
            case "House" -> addHouse(playerId, payload.getOrDefault("color", ""));
            case "Hotel" -> addHotel(playerId, payload.getOrDefault("color", ""));
            case "Just Say No", "Double The Rent" -> throw new IllegalArgumentException(actionCard.name() + " can only be used reactively");
            default -> throw new IllegalArgumentException("unsupported action card: " + actionCard.name());
        }
    }

    private void addHouse(String playerId, String color) {
        PlayerState playerState = playerState(playerId);
        if ("Railroad".equals(color) || "Utility".equals(color)) {
            throw new IllegalArgumentException("house cannot be used on Railroad/Utility");
        }
        if (!victoryManager.isCompleteSet(playerState, color)) {
            throw new IllegalStateException("house requires complete set");
        }
        playerState.addHouse(color);
    }

    private void addHotel(String playerId, String color) {
        PlayerState playerState = playerState(playerId);
        if ("Railroad".equals(color) || "Utility".equals(color)) {
            throw new IllegalArgumentException("hotel cannot be used on Railroad/Utility");
        }
        if (!playerState.hasHouse(color)) {
            throw new IllegalStateException("hotel requires house first");
        }
        if (!victoryManager.isCompleteSet(playerState, color)) {
            throw new IllegalStateException("hotel requires complete set");
        }
        if (playerState.hasHotel(color)) {
            throw new IllegalStateException("hotel already exists on this set");
        }
        playerState.addHotel(color);
    }

    private PlayerState playerState(String playerId) {
        PlayerState state = players.get(playerId);
        if (state == null) {
            throw new IllegalArgumentException("unknown player: " + playerId);
        }
        return state;
    }
}
