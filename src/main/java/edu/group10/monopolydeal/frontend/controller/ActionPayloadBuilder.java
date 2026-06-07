package edu.group10.monopolydeal.frontend.controller;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Builds UI payload maps for action cards that need extra input.
 */
final class ActionPayloadBuilder {

    /** Collects extra parameters for the currently selected action card. */
    Map<String, String> build(
            int selectedHandIndex,
            Card selectedCard,
            Supplier<String> chooseTargetPlayerId,
            Function<String, PlayerState> findById,
            Supplier<PlayerState> findMe,
            DialogOps dialogOps,
            ToIntFunction<String> requiredSetSize,
            Consumer<String> statusSink) {

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("handIndex", String.valueOf(selectedHandIndex));

        String name = selectedCard == null ? "" : selectedCard.name();
        if ("Sly Deal".equals(name)) {
            String target = chooseTargetPlayerId.get();
            if (target == null) {
                return null;
            }
            PlayerState targetPs = findById.apply(target);
            String color = dialogOps.chooseColor("Select target property color", targetPs == null ? List.of() : List.copyOf(targetPs.properties().keySet()));
            if (color == null) {
                return null;
            }
            List<Card> cards = targetPs == null ? List.of() : targetPs.properties().getOrDefault(color, List.of());
            if (cards.isEmpty()) {
                statusSink.accept("No stealable property in selected color group");
                return null;
            }
            payload.put("targetPlayerId", target);
            payload.put("color", color);
            payload.put("propertyIndex", String.valueOf(cards.size() - 1));
            return payload;
        }

        if ("Forced Deal".equals(name)) {
            String target = chooseTargetPlayerId.get();
            if (target == null) {
                return null;
            }
            PlayerState me = findMe.get();
            PlayerState targetPs = findById.apply(target);
            String myColor = dialogOps.chooseColor("Select your property color", me == null ? List.of() : List.copyOf(me.properties().keySet()));
            if (myColor == null) {
                return null;
            }
            String targetColor = dialogOps.chooseColor("Select target property color", targetPs == null ? List.of() : List.copyOf(targetPs.properties().keySet()));
            if (targetColor == null) {
                return null;
            }
            List<Card> myCards = me == null ? List.of() : me.properties().getOrDefault(myColor, List.of());
            List<Card> targetCards = targetPs == null ? List.of() : targetPs.properties().getOrDefault(targetColor, List.of());
            if (myCards.isEmpty() || targetCards.isEmpty()) {
                statusSink.accept("No swappable property in selected color group");
                return null;
            }
            payload.put("targetPlayerId", target);
            payload.put("myColor", myColor);
            payload.put("targetColor", targetColor);
            payload.put("myIndex", String.valueOf(myCards.size() - 1));
            payload.put("targetIndex", String.valueOf(targetCards.size() - 1));
            return payload;
        }

        if ("Debt Collector".equals(name)) {
            String target = chooseTargetPlayerId.get();
            if (target == null) {
                return null;
            }
            payload.put("targetPlayerId", target);
            return payload;
        }

        if ("Deal Breaker".equals(name) || "House".equals(name) || "Hotel".equals(name)) {
            if ("Deal Breaker".equals(name)) {
                String target = chooseTargetPlayerId.get();
                if (target == null) {
                    return null;
                }
                PlayerState targetPs = findById.apply(target);
                List<String> completeColors = targetPs == null
                        ? List.of()
                        : targetPs.properties().entrySet().stream()
                        .filter(entry -> entry.getValue() != null && entry.getValue().size() >= requiredSetSize.applyAsInt(entry.getKey()))
                        .map(Map.Entry::getKey)
                        .toList();
                if (completeColors.isEmpty()) {
                    statusSink.accept("Target player has no complete set, cannot use Deal Breaker");
                    return null;
                }
                String color = dialogOps.chooseColor("Select complete set color to steal", completeColors);
                if (color == null) {
                    return null;
                }
                payload.put("targetPlayerId", target);
                payload.put("color", color);
                return payload;
            }

            PlayerState me = findMe.get();
            String color = dialogOps.chooseColor("Select color group for building", me == null ? List.of() : List.copyOf(me.properties().keySet()));
            if (color == null) {
                return null;
            }
            payload.put("color", color);
            return payload;
        }

        return payload;
    }

    /**
     * Callback contract used to open selection dialogs from the controller.
     */
    interface DialogOps {
        String chooseColor(String title, List<String> colors);

        int choosePropertyIndex(String title, List<Card> cards);
    }
}
