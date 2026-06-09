package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import java.util.List;
import java.util.Map;

/**
 * Dispatches action cards to their rule-specific resolvers.
 */
final class ActionResolutionService {

    @FunctionalInterface
    interface BuildingAction {
        void apply(String playerId, String color);
    }

    private final TurnManager turnManager;
    private final JustSayNoResolver justSayNoResolver;
    private final BuildingAction houseAction;
    private final BuildingAction hotelAction;

    ActionResolutionService(
            TurnManager turnManager,
            JustSayNoResolver justSayNoResolver,
            BuildingAction houseAction,
            BuildingAction hotelAction
    ) {
        this.turnManager = turnManager;
        this.justSayNoResolver = justSayNoResolver;
        this.houseAction = houseAction;
        this.hotelAction = hotelAction;
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
            case "House" -> houseAction.apply(playerId, payload.getOrDefault("color", ""));
            case "Hotel" -> hotelAction.apply(playerId, payload.getOrDefault("color", ""));
            case "Just Say No", "Double The Rent" -> throw new IllegalArgumentException(actionCard.name() + " can only be used reactively");
            default -> throw new IllegalArgumentException("unsupported action card: " + actionCard.name());
        }
    }
}
