package edu.group10.monopolydeal.backend.service;

/**
 * Shared rules for normalizing property group names and set completion sizes.
 */
public final class PropertySetRules {

    private PropertySetRules() {
    }

    /** Removes duplicate-group suffixes such as "Brown (2)" from a color label. */
    public static String baseColor(String color) {
        if (color == null) {
            return "";
        }
        return color.replaceFirst(" \\(\\d+\\)$", "");
    }

    /** Returns the number of properties needed to complete the given color set. */
    public static int requiredSetSize(String color) {
        return switch (baseColor(color)) {
            case "Brown", "Deep Blue", "Utility" -> 2;
            case "Railroad" -> 4;
            default -> 3;
        };
    }

    /** Returns whether the given property count completes the set for this color. */
    public static boolean isCompleteSet(String color, int propertyCount) {
        return propertyCount >= requiredSetSize(color);
    }
}
