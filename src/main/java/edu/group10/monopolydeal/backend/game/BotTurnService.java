package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.CardMoneyRules;
import edu.group10.monopolydeal.backend.service.CardPropertyRules;
import java.util.List;
import java.util.Map;

/**
 * Contains simple automated decision making for bot turns.
 */
final class BotTurnService {

    /** Plays a full bot turn using simple greedy heuristics. */
    void playTurn(GameEngine engine, String playerId) {
        int tries = 0;
        int actionsPlayed = 0;
        while (tries < 12) {
            if (actionsPlayed >= 3) {
                break;
            }
            if (tryPlayMoney(engine, playerId)) {
                if (engine.hasPendingJsn()) {
                    return;
                }
                tries++;
                actionsPlayed++;
                continue;
            }
            if (tryPlayProperty(engine, playerId)) {
                if (engine.hasPendingJsn()) {
                    return;
                }
                tries++;
                actionsPlayed++;
                continue;
            }
            if (engine.playerState(playerId).bankTotal() == 0 && tryPlayCheapestActionAsMoney(engine, playerId)) {
                if (engine.hasPendingJsn()) {
                    return;
                }
                tries++;
                actionsPlayed++;
                continue;
            }
            if (tryPlayAction(engine, playerId)) {
                if (engine.hasPendingJsn()) {
                    return;
                }
                tries++;
                actionsPlayed++;
                continue;
            }
            break;
        }
        if (engine.hasPendingJsn()) {
            return;
        }
        engine.endTurn(playerId);
    }

    /** Picks and banks the first money card found in hand. */
    private boolean tryPlayMoney(GameEngine engine, String playerId) {
        List<Card> hand = engine.playerState(playerId).hand();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (CardMoneyRules.canBank(card) && card.type() == CardType.MONEY) {
                engine.playMoneyCard(playerId, i);
                return true;
            }
        }
        return false;
    }

    /** Plays the first available property card. */
    private boolean tryPlayProperty(GameEngine engine, String playerId) {
        List<Card> hand = engine.playerState(playerId).hand();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.type() == CardType.PROPERTY) {
                engine.playPropertyCard(playerId, i, "");
                return true;
            }
            if (card.type() == CardType.MULTI_PROPERTY) {
                engine.playPropertyCard(playerId, i, chooseColorForMultiProperty(engine, playerId, card));
                return true;
            }
        }
        return false;
    }

    /** Banks the cheapest action card when the bot has no money. */
    private boolean tryPlayCheapestActionAsMoney(GameEngine engine, String playerId) {
        List<Card> hand = engine.playerState(playerId).hand();
        int bestIndex = -1;
        int bestValue = Integer.MAX_VALUE;
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.type() == CardType.ACTION && CardMoneyRules.canBank(card) && card.bankValue() > 0 && card.bankValue() < bestValue) {
                bestValue = card.bankValue();
                bestIndex = i;
            }
        }
        if (bestIndex >= 0) {
            engine.playMoneyCard(playerId, bestIndex);
            return true;
        }
        return false;
    }

    /** Plays the first supported action card with an auto-built payload. */
    private boolean tryPlayAction(GameEngine engine, String playerId) {
        List<Card> hand = engine.playerState(playerId).hand();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.type() != CardType.ACTION) {
                continue;
            }
            Map<String, String> payload = buildBotActionPayload(engine, playerId, card.name());
            if (payload == null) {
                continue;
            }
            engine.playActionCard(playerId, i, payload);
            return true;
        }
        return false;
    }

    private String chooseColorForMultiProperty(GameEngine engine, String playerId, Card card) {
        List<String> options = CardPropertyRules.allowedPropertyColors(card);
        if (options.isEmpty()) {
            return card.color();
        }
        if (options.contains("Deep Blue")) {
            return "Deep Blue";
        }
        return options.get(0);
    }

    private Map<String, String> buildBotActionPayload(GameEngine engine, String playerId, String actionName) {
        if ("Just Say No".equals(actionName) || "Double The Rent".equals(actionName)) {
            return null;
        }
        if ("Pass Go".equals(actionName) || "It's My Birthday".equals(actionName)) {
            return Map.of();
        }
        String targetId = firstOpponent(engine, playerId);
        if ("Debt Collector".equals(actionName)) {
            return targetId == null ? null : Map.of("targetPlayerId", targetId);
        }
        if ("House".equals(actionName)) {
            String color = findHouseColor(engine.playerState(playerId));
            return color == null ? null : Map.of("color", color);
        }
        if ("Hotel".equals(actionName)) {
            String color = findHotelColor(engine.playerState(playerId));
            return color == null ? null : Map.of("color", color);
        }
        return null;
    }

    private String findHouseColor(PlayerState playerState) {
        for (String color : playerState.properties().keySet()) {
            if ("Railroad".equals(color) || "Utility".equals(color)) {
                continue;
            }
            if (isCompleteSet(playerState, color) && !playerState.hasHouse(color)) {
                return color;
            }
        }
        return null;
    }

    private String findHotelColor(PlayerState playerState) {
        for (String color : playerState.properties().keySet()) {
            if ("Railroad".equals(color) || "Utility".equals(color)) {
                continue;
            }
            if (isCompleteSet(playerState, color) && playerState.hasHouse(color) && !playerState.hasHotel(color)) {
                return color;
            }
        }
        return null;
    }

    private String firstOpponent(GameEngine engine, String playerId) {
        for (String id : engine.turnOrder()) {
            if (!id.equals(playerId)) {
                return id;
            }
        }
        return null;
    }

    private boolean isCompleteSet(PlayerState playerState, String color) {
        return playerState.propertyCount(color) >= requiredSetSize(color);
    }

    private int requiredSetSize(String color) {
        return switch (baseColor(color)) {
            case "Brown", "Deep Blue", "Utility" -> 2;
            case "Railroad" -> 4;
            default -> 3;
        };
    }

    private String baseColor(String color) {
        if (color == null) {
            return "";
        }
        return color.replaceFirst(" \\(\\d+\\)$", "");
    }
}
