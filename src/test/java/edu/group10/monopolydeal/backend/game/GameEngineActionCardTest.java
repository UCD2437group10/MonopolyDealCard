package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.*;

import edu.group10.monopolydeal.TestHelpers;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for action cards, Just Say No chains, and related mechanics.
 */
class GameEngineActionCardTest {

    // ========== Pass Go ==========

    @Test
    void passGoDrawsTwoCards() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Pass Go", CardType.ACTION, "-", 1));
        int beforeSize = p1.hand().size(); // = 1
        engine.playActionCard("p1", 0, Map.of());
        assertEquals(beforeSize - 1 + 2, p1.hand().size()); // 1 - 1 + 2 = 2
    }

    // ========== Debt Collector ==========

    @Test
    void debtCollectorCharges5M() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3));
        p2.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        // p2 has no JSN, pays 5M
        assertEquals(5, p1.bankTotal());
        assertEquals(0, p2.bankTotal());
    }

    @Test
    void debtCollectorWithJustSayNoCancelled() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3));
        TestHelpers.replaceHand(p2, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        p2.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        assertEquals("p2", engine.snapshot().jsnResponderPlayerId());
        engine.respondJustSayNo("p2", true);
        assertEquals(0, p1.bankTotal()); // No money taken
        assertEquals(5, p2.bankTotal()); // p2 keeps money
    }

    @Test
    void debtCollectorWithoutJustSayNoAppliesImmediately() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3));
        TestHelpers.replaceHand(p2, TestHelpers.card("Pass Go", CardType.ACTION, "-", 1)); // No JSN
        p2.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        // No JSN prompt, effect applies immediately
        assertEquals("", engine.snapshot().jsnResponderPlayerId());
        assertEquals(5, p1.bankTotal());
        assertEquals(0, p2.bankTotal());
    }

    // ========== It's My Birthday ==========

    @Test
    void itsMyBirthdayChargesAllOpponents() {
        GameEngine engine = TestHelpers.createStartedEngine3P();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        PlayerState p3 = engine.playerState("p3");
        TestHelpers.replaceHand(p1, TestHelpers.card("It's My Birthday", CardType.ACTION, "-", 2));
        // Give 2x1M = 2M exact payment to each opponent
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p3.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p3.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        int initialP1Bank = p1.bankTotal();
        engine.playActionCard("p1", 0, Map.of());
        // Both opponents pay 2M each = 4M total
        assertEquals(initialP1Bank + 4, p1.bankTotal());
    }

    @Test
    void itsMyBirthdayWithJustSayNoFromOneOpponent() {
        GameEngine engine = TestHelpers.createStartedEngine3P();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        PlayerState p3 = engine.playerState("p3");
        TestHelpers.replaceHand(p1, TestHelpers.card("It's My Birthday", CardType.ACTION, "-", 2));
        TestHelpers.replaceHand(p2, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        // p2 has JSN, p3 pays 2M (exact)
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p3.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p3.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        int initialP1Bank = p1.bankTotal();
        engine.playActionCard("p1", 0, Map.of());
        // p2 has JSN → can cancel
        assertEquals("p2", engine.snapshot().jsnResponderPlayerId());
        engine.respondJustSayNo("p2", true);
        // Only p3 pays 2M
        assertEquals(initialP1Bank + 2, p1.bankTotal());
    }

    // ========== Sly Deal ==========

    @Test
    void slyDealStealsSingleProperty() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Sly Deal", CardType.ACTION, "-", 3));
        p2.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2", "color", "Brown", "propertyIndex", "0"));
        assertEquals(0, p2.propertyCount("Brown"));
        assertEquals(1, p1.propertyCount("Brown"));
    }

    @Test
    void slyDealCannotStealFromCompleteSet() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Sly Deal", CardType.ACTION, "-", 3));
        p2.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p2.addProperty("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0)); // complete for Brown (2)
        assertThrows(IllegalStateException.class, () ->
                engine.playActionCard("p1", 0, Map.of(
                        "targetPlayerId", "p2", "color", "Brown", "propertyIndex", "0")));
    }

    // ========== Forced Deal ==========

    @Test
    void forcedDealSwapsProperties() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Forced Deal", CardType.ACTION, "-", 3));
        p1.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p2.addProperty("Light Blue", TestHelpers.card("Oriental Avenue", CardType.PROPERTY, "Light Blue", 0));
        engine.playActionCard("p1", 0, Map.of(
                "targetPlayerId", "p2",
                "myColor", "Brown", "myIndex", "0",
                "targetColor", "Light Blue", "targetIndex", "0"));
        assertEquals(0, p1.propertyCount("Brown"));
        assertEquals(1, p1.propertyCount("Light Blue"));
        assertEquals(1, p2.propertyCount("Brown"));
        assertEquals(0, p2.propertyCount("Light Blue"));
    }

    @Test
    void forcedDealCannotSwapFromCompleteSet() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Forced Deal", CardType.ACTION, "-", 3));
        p1.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addProperty("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0)); // complete set
        engine.playerState("p2").addProperty("Light Blue", TestHelpers.card("Oriental Avenue", CardType.PROPERTY, "Light Blue", 0));
        assertThrows(IllegalStateException.class, () ->
                engine.playActionCard("p1", 0, Map.of(
                        "targetPlayerId", "p2",
                        "myColor", "Brown", "myIndex", "0",
                        "targetColor", "Light Blue", "targetIndex", "0")));
    }

    // ========== Deal Breaker ==========

    @Test
    void dealBreakerStealsCompleteSet() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Deal Breaker", CardType.ACTION, "-", 5));
        p2.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p2.addProperty("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0)); // complete set
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2", "color", "Brown"));
        assertEquals(0, p2.propertyCount("Brown"));
        assertEquals(2, p1.propertyCount("Brown"));
    }

    @Test
    void dealBreakerOnIncompleteSetThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Deal Breaker", CardType.ACTION, "-", 5));
        p2.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0)); // incomplete
        assertThrows(IllegalStateException.class, () ->
                engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2", "color", "Brown")));
    }

    @Test
    void dealBreakerStealsHouseAndHotel() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Deal Breaker", CardType.ACTION, "-", 5));
        p2.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p2.addProperty("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        p2.addHouse("Brown");
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2", "color", "Brown"));
        assertEquals(2, p1.propertyCount("Brown"));
        assertTrue(p1.hasHouse("Brown"));
        assertFalse(p2.hasHouse("Brown"));
    }

    // ========== House & Hotel ==========

    @Test
    void addHouseToCompleteSet() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("House", CardType.ACTION, "-", 3));
        engine.playerState("p1").addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playerState("p1").addProperty("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playActionCard("p1", 0, Map.of("color", "Brown"));
        assertTrue(p1.hasHouse("Brown"));
    }

    @Test
    void houseOnIncompleteSetThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("House", CardType.ACTION, "-", 3));
        p1.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0)); // only 1
        assertThrows(IllegalStateException.class, () ->
                engine.playActionCard("p1", 0, Map.of("color", "Brown")));
    }

    @Test
    void houseOnRailroadThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("House", CardType.ACTION, "-", 3));
        p1.addProperty("Railroad", TestHelpers.card("Reading Railroad", CardType.PROPERTY, "Railroad", 0));
        p1.addProperty("Railroad", TestHelpers.card("Pennsylvania Railroad", CardType.PROPERTY, "Railroad", 0));
        p1.addProperty("Railroad", TestHelpers.card("B&O Railroad", CardType.PROPERTY, "Railroad", 0));
        p1.addProperty("Railroad", TestHelpers.card("Short Line", CardType.PROPERTY, "Railroad", 0));
        assertThrows(IllegalArgumentException.class, () ->
                engine.playActionCard("p1", 0, Map.of("color", "Railroad")));
    }

    @Test
    void houseOnUtilityThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("House", CardType.ACTION, "-", 3));
        p1.addProperty("Utility", TestHelpers.card("Electric Company", CardType.PROPERTY, "Utility", 0));
        p1.addProperty("Utility", TestHelpers.card("Water Works", CardType.PROPERTY, "Utility", 0));
        assertThrows(IllegalArgumentException.class, () ->
                engine.playActionCard("p1", 0, Map.of("color", "Utility")));
    }

    @Test
    void addHotelToCompleteSetWithHouse() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Hotel", CardType.ACTION, "-", 4),
                TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p1.addProperty("Green", TestHelpers.card("Pacific Avenue", CardType.PROPERTY, "Green", 0));
        p1.addProperty("Green", TestHelpers.card("North Carolina Avenue", CardType.PROPERTY, "Green", 0));
        p1.addProperty("Green", TestHelpers.card("Pennsylvania Avenue", CardType.PROPERTY, "Green", 0));
        p1.addHouse("Green");
        engine.playActionCard("p1", 0, Map.of("color", "Green"));
        assertTrue(p1.hasHotel("Green"));
    }

    @Test
    void hotelWithoutHouseThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Hotel", CardType.ACTION, "-", 4));
        p1.addProperty("Green", TestHelpers.card("Pacific Avenue", CardType.PROPERTY, "Green", 0));
        p1.addProperty("Green", TestHelpers.card("North Carolina Avenue", CardType.PROPERTY, "Green", 0));
        p1.addProperty("Green", TestHelpers.card("Pennsylvania Avenue", CardType.PROPERTY, "Green", 0));
        assertThrows(IllegalStateException.class, () ->
                engine.playActionCard("p1", 0, Map.of("color", "Green")));
    }

    // ========== Just Say No reactive-only behavior ==========

    @Test
    void justSayNoCannotBePlayedActively() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        assertThrows(IllegalArgumentException.class, () ->
                engine.playActionCard("p1", 0, Map.of()));
    }

    @Test
    void doubleTheRentCannotBePlayedActively() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Double The Rent", CardType.ACTION, "-", 1));
        assertThrows(IllegalArgumentException.class, () ->
                engine.playActionCard("p1", 0, Map.of()));
    }

    @Test
    void respondJsnWithoutPendingJsnThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        assertThrows(IllegalStateException.class, () -> engine.respondJustSayNo("p2", true));
    }

    @Test
    void wrongPlayerCannotRespondJsn() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3));
        TestHelpers.replaceHand(p2, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        p2.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        assertThrows(IllegalStateException.class, () -> engine.respondJustSayNo("p1", true));
    }

    // ========== JSN Ping-Pong Chain ==========

    @Test
    void jsnChainActorCanUseJsn() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Sly Deal", CardType.ACTION, "-", 3),
                TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        TestHelpers.replaceHand(p2, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        p2.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2", "color", "Brown", "propertyIndex", "0"));
        // p2 has JSN -> prompt
        assertEquals("p2", engine.snapshot().jsnResponderPlayerId());
        engine.respondJustSayNo("p2", true); // p2 uses JSN -> bounces to p1
        // p1 also has JSN, should get prompt
        assertEquals("p1", engine.snapshot().jsnResponderPlayerId());
        engine.respondJustSayNo("p1", true); // p1 uses JSN -> bounces back to p2
        // p2 has no more JSN, effect applies
        assertEquals(0, engine.snapshot().jsnResponderPlayerId().length());
        assertEquals(0, p2.propertyCount("Brown"));
        assertEquals(1, p1.propertyCount("Brown"));
    }

    @Test
    void playerWithoutJsnSkipsPrompt() {
        GameEngine engine = TestHelpers.createStartedEngine3P();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        PlayerState p3 = engine.playerState("p3");
        TestHelpers.replaceHand(p1, TestHelpers.card("It's My Birthday", CardType.ACTION, "-", 2));
        TestHelpers.replaceHand(p2, TestHelpers.card("Pass Go", CardType.ACTION, "-", 1)); // No JSN
        TestHelpers.replaceHand(p3, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        p2.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        p3.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        engine.playActionCard("p1", 0, Map.of());
        // p2 has no JSN → auto-pass → p3 has JSN → prompted
        assertEquals("p3", engine.snapshot().jsnResponderPlayerId());
    }

    // ========== Action consuming and pending JSN ==========

    @Test
    void cannotPlayActionDuringPendingJsn() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3),
                TestHelpers.card("Pass Go", CardType.ACTION, "-", 1));
        TestHelpers.replaceHand(p2, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        p2.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        assertTrue(engine.snapshot().jsnResponderPlayerId().length() > 0);
        // p1 can't play another action while JSN is pending
        assertThrows(IllegalStateException.class, () -> engine.playActionCard("p1", 0, Map.of()));
    }

    // ========== Unsupported action ==========

    @Test
    void unsupportedActionCardThrows() {
        // "House" is supported, let's try an unknown card type as action
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        assertThrows(IllegalArgumentException.class, () ->
                engine.playActionCard("p1", 0, Map.of()));
    }
}
