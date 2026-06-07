package edu.group10.monopolydeal.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import java.util.List;
import org.junit.jupiter.api.Test;

class CardPropertyRulesTest {

    @Test
    void wildPropertyCanChooseAnyBaseColor() {
        // Wild property cards can be placed into any supported base color.
        SimpleCard wild = new SimpleCard("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0);

        assertIterableEquals(
                List.of("Brown", "Light Blue", "Pink", "Orange", "Red",
                        "Yellow", "Green", "Deep Blue", "Railroad", "Utility"),
                CardPropertyRules.allowedPropertyColors(wild)
        );
        assertEquals("Railroad", CardPropertyRules.resolvePropertyColor(wild, "Railroad"));
    }

    @Test
    void namedDualColorCardUsesConfiguredChoices() {
        // Named dual-color cards should follow the predefined color mapping.
        SimpleCard dual = new SimpleCard("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0);

        assertIterableEquals(List.of("Light Blue", "Brown"), CardPropertyRules.allowedPropertyColors(dual));
        assertEquals("Brown", CardPropertyRules.resolvePropertyColor(dual, "Brown"));
    }

    @Test
    void multiPropertyRequiresValidColorChoice() {
        // Invalid target colors should be rejected during placement or recolor.
        SimpleCard dual = new SimpleCard("Railroad/Utility Multi", CardType.MULTI_PROPERTY, "Railroad/Utility", 0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CardPropertyRules.resolvePropertyColor(dual, "Pink")
        );

        assertEquals("invalid colorChoice for card", exception.getMessage());
    }

    @Test
    void normalPropertyAlwaysKeepsItsOwnColor() {
        // A normal property card does not need an extra color choice.
        SimpleCard property = new SimpleCard("Boardwalk", CardType.PROPERTY, "Deep Blue", 0);

        assertEquals("Deep Blue", CardPropertyRules.resolvePropertyColor(property, ""));
    }
}
