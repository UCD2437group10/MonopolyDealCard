package edu.group10.monopolydeal.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PropertySetRulesTest {

    @Test
    void baseColorStripsDuplicateGroupSuffix() {
        assertEquals("Brown", PropertySetRules.baseColor("Brown (2)"));
        assertEquals("Railroad", PropertySetRules.baseColor("Railroad (3)"));
    }

    @Test
    void requiredSetSizeMatchesMonopolyDealRules() {
        assertEquals(2, PropertySetRules.requiredSetSize("Brown"));
        assertEquals(2, PropertySetRules.requiredSetSize("Deep Blue (2)"));
        assertEquals(4, PropertySetRules.requiredSetSize("Railroad"));
        assertEquals(3, PropertySetRules.requiredSetSize("Pink"));
    }

    @Test
    void completeSetCheckUsesNormalizedColor() {
        assertTrue(PropertySetRules.isCompleteSet("Brown (2)", 2));
        assertTrue(PropertySetRules.isCompleteSet("Railroad", 4));
    }
}
