package edu.group10.monopolydeal.backend.service;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralizes allowed property colors for play and recolor actions.
 */
public final class CardPropertyRules {

    /** All base property colors supported by the ruleset. */
    private static final List<String> ALL_PROPERTY_COLORS = List.of(
            "Brown", "Light Blue", "Pink", "Orange", "Red",
            "Yellow", "Green", "Deep Blue", "Railroad", "Utility");

    /** Explicit color mappings for multi-color and wild property cards. */
    private static final Map<String, List<String>> MULTI_PROPERTY_COLOR_BY_NAME = new LinkedHashMap<>();

    static {
        MULTI_PROPERTY_COLOR_BY_NAME.put("Light Blue/Brown Multi", List.of("Light Blue", "Brown"));
        MULTI_PROPERTY_COLOR_BY_NAME.put("Wild Light Blue-Brown", List.of("Light Blue", "Brown"));

        MULTI_PROPERTY_COLOR_BY_NAME.put("Light Blue/Railroad Multi", List.of("Light Blue", "Railroad"));
        MULTI_PROPERTY_COLOR_BY_NAME.put("Wild Railroad-Light Blue", List.of("Light Blue", "Railroad"));

        MULTI_PROPERTY_COLOR_BY_NAME.put("Pink/Orange Multi", List.of("Pink", "Orange"));
        MULTI_PROPERTY_COLOR_BY_NAME.put("Wild Orange-Pink", List.of("Orange", "Pink"));

        MULTI_PROPERTY_COLOR_BY_NAME.put("Red/Yellow Multi", List.of("Red", "Yellow"));
        MULTI_PROPERTY_COLOR_BY_NAME.put("Wild Yellow-Red", List.of("Yellow", "Red"));

        MULTI_PROPERTY_COLOR_BY_NAME.put("Deep Blue/Green Multi", List.of("Deep Blue", "Green"));
        MULTI_PROPERTY_COLOR_BY_NAME.put("Wild Blue-Green", List.of("Deep Blue", "Green"));

        MULTI_PROPERTY_COLOR_BY_NAME.put("Green/Railroad Multi", List.of("Green", "Railroad"));
        MULTI_PROPERTY_COLOR_BY_NAME.put("Wild Railroad-Green", List.of("Green", "Railroad"));

        MULTI_PROPERTY_COLOR_BY_NAME.put("Railroad/Utility Multi", List.of("Railroad", "Utility"));
        MULTI_PROPERTY_COLOR_BY_NAME.put("Wild Utility-Railroad", List.of("Railroad", "Utility"));

        MULTI_PROPERTY_COLOR_BY_NAME.put("Wild Property", ALL_PROPERTY_COLORS);
        MULTI_PROPERTY_COLOR_BY_NAME.put("Property Wild Card", ALL_PROPERTY_COLORS);
    }

    private CardPropertyRules() {
    }

    /** Returns the list of all base property colors. */
    public static List<String> allPropertyColors() {
        return ALL_PROPERTY_COLORS;
    }

    /** Returns the playable color choices for the given property card. */
    public static List<String> allowedPropertyColors(Card card) {
        if (card == null) {
            return List.of();
        }
        if (card.type() == CardType.PROPERTY) {
            return List.of(card.color());
        }
        if (card.type() != CardType.MULTI_PROPERTY) {
            return List.of();
        }

        List<String> byName = MULTI_PROPERTY_COLOR_BY_NAME.get(card.name());
        if (byName != null) {
            return byName;
        }
        if ("Wild".equalsIgnoreCase(card.color())) {
            return ALL_PROPERTY_COLORS;
        }
        if (card.color() == null || card.color().isBlank()) {
            return List.of();
        }
        return Arrays.stream(card.color().split("/"))
                .map(String::trim)
                .filter(color -> !color.isBlank())
                .toList();
    }

    /** Resolves and validates the final color choice for a property placement. */
    public static String resolvePropertyColor(Card card, String colorChoice) {
        List<String> allowedColors = allowedPropertyColors(card);
        if (allowedColors.isEmpty()) {
            throw new IllegalArgumentException("card is not property type");
        }
        if (card != null && card.type() == CardType.PROPERTY) {
            return allowedColors.get(0);
        }
        if (colorChoice == null || colorChoice.isBlank()) {
            throw new IllegalArgumentException("multi property requires colorChoice");
        }
        for (String allowedColor : allowedColors) {
            if (allowedColor.equals(colorChoice)) {
                return allowedColor;
            }
        }
        throw new IllegalArgumentException("invalid colorChoice for card");
    }
}
