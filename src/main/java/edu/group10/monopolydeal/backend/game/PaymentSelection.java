package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Value object describing which assets a player selected for a manual payment.
 */
public final class PaymentSelection {

    private final List<Integer> bankIndexes;
    private final Map<String, List<Integer>> propertyIndexesByColor;

    PaymentSelection(List<Integer> bankIndexes, Map<String, List<Integer>> propertyIndexesByColor) {
        this.bankIndexes = bankIndexes == null ? List.of() : List.copyOf(bankIndexes);
        Map<String, List<Integer>> copy = new LinkedHashMap<>();
        if (propertyIndexesByColor != null) {
            for (Map.Entry<String, List<Integer>> entry : propertyIndexesByColor.entrySet()) {
                copy.put(entry.getKey(), List.copyOf(entry.getValue()));
            }
        }
        this.propertyIndexesByColor = Map.copyOf(copy);
    }

    public static PaymentSelection fromPayload(Map<String, String> payload) {
        return new PaymentSelection(
                parseIndexes(payload.getOrDefault("bankIndexes", "")),
                parsePropertyRefs(payload.getOrDefault("propertyRefs", ""))
        );
    }

    List<Integer> bankIndexes() {
        return bankIndexes;
    }

    Map<String, List<Integer>> propertyIndexesByColor() {
        return propertyIndexesByColor;
    }

    int totalValue(PlayerState state) {
        validateAgainst(state);
        int total = 0;
        for (int index : bankIndexes) {
            total += state.bank().get(index).bankValue();
        }
        for (Map.Entry<String, List<Integer>> entry : propertyIndexesByColor.entrySet()) {
            List<Card> group = state.properties().get(entry.getKey());
            for (int index : entry.getValue()) {
                total += group.get(index).bankValue();
            }
        }
        return total;
    }

    boolean selectsAllAssets(PlayerState state) {
        validateAgainst(state);
        return selectedAssetCount() == totalAssetCount(state);
    }

    void validateAgainst(PlayerState state) {
        validateUniqueIndexes(bankIndexes, state.bank().size(), "bank");
        for (Map.Entry<String, List<Integer>> entry : propertyIndexesByColor.entrySet()) {
            List<Card> group = state.properties().get(entry.getKey());
            if (group == null) {
                throw new IllegalArgumentException("invalid property color: " + entry.getKey());
            }
            validateUniqueIndexes(entry.getValue(), group.size(), "property");
        }
    }

    private int selectedAssetCount() {
        int total = bankIndexes.size();
        for (List<Integer> indexes : propertyIndexesByColor.values()) {
            total += indexes.size();
        }
        return total;
    }

    private int totalAssetCount(PlayerState state) {
        int total = state.bank().size();
        for (List<Card> group : state.properties().values()) {
            total += group.size();
        }
        return total;
    }

    private static void validateUniqueIndexes(List<Integer> indexes, int size, String zone) {
        Set<Integer> unique = new LinkedHashSet<>();
        for (int index : indexes) {
            if (index < 0 || index >= size) {
                throw new IllegalArgumentException("invalid " + zone + " index");
            }
            if (!unique.add(index)) {
                throw new IllegalArgumentException("duplicate " + zone + " index");
            }
        }
    }

    private static List<Integer> parseIndexes(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<Integer> result = new ArrayList<>();
        for (String part : raw.split(",")) {
            if (!part.isBlank()) {
                result.add(Integer.parseInt(part.trim()));
            }
        }
        return result;
    }

    private static Map<String, List<Integer>> parsePropertyRefs(String raw) {
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }
        Map<String, List<Integer>> result = new LinkedHashMap<>();
        for (String ref : raw.split(";")) {
            if (ref.isBlank()) {
                continue;
            }
            int splitAt = ref.lastIndexOf('@');
            if (splitAt <= 0 || splitAt >= ref.length() - 1) {
                throw new IllegalArgumentException("invalid property selection");
            }
            String color = ref.substring(0, splitAt).trim();
            int index = Integer.parseInt(ref.substring(splitAt + 1).trim());
            result.computeIfAbsent(color, key -> new ArrayList<>()).add(index);
        }
        return result;
    }
}
