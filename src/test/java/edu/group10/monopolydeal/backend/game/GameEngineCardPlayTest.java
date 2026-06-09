package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.*;

import edu.group10.monopolydeal.TestHelpers;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for playing money, property, and rent cards.
 */
class GameEngineCardPlayTest {

    // ========== Money card tests ==========

    @Test
    void playMoneyCardAddsToBank() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        int initialBank = p1.bankTotal();
        engine.playMoneyCard("p1", 0);
        assertEquals(initialBank + 1, p1.bankTotal());
    }

    @Test
    void playMoneyConsumesAction() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1),
                TestHelpers.card("2M Money", CardType.MONEY, "Orange-Red", 2),
                TestHelpers.card("3M Money", CardType.MONEY, "Green", 3));
        engine.playMoneyCard("p1", 0);
        engine.playMoneyCard("p1", 0);
        engine.playMoneyCard("p1", 0);
        assertThrows(IllegalStateException.class, () -> engine.playMoneyCard("p1", 0));
    }

    @Test
    void playMoneyInvalidHandIndexThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        assertThrows(IllegalArgumentException.class, () -> engine.playMoneyCard("p1", -1));
        assertThrows(IllegalArgumentException.class, () -> engine.playMoneyCard("p1", 999));
    }

    @Test
    void propertyCardCannotBeBanked() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        assertThrows(IllegalStateException.class, () -> engine.playMoneyCard("p1", 0));
    }

    @Test
    void multiPropertyCardCannotBeBanked() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        assertThrows(IllegalStateException.class, () -> engine.playMoneyCard("p1", 0));
    }

    @Test
    void actionCardCanBeBankedAsMoney() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Deal Breaker", CardType.ACTION, "-", 5));
        engine.playMoneyCard("p1", 0);
        assertEquals(5, p1.bankTotal());
    }

    @Test
    void rentCardCanBeBankedAsMoney() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Rent Wild", CardType.RENT, "Any", 3));
        engine.playMoneyCard("p1", 0);
        assertEquals(3, p1.bankTotal());
    }

    @Test
    void notCurrentPlayerCannotPlayMoney() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p2, TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        assertThrows(IllegalStateException.class, () -> engine.playMoneyCard("p2", 0));
    }

    // ========== Property card tests ==========

    @Test
    void playSingleColorProperty() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playPropertyCard("p1", 0, "");
        assertTrue(p1.hasProperty("Brown"));
        assertEquals(1, p1.propertyCount("Brown"));
    }

    @Test
    void playMultiPropertyWithColorChoice() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        engine.playPropertyCard("p1", 0, "Deep Blue");
        assertTrue(p1.hasProperty("Deep Blue"));
    }

    @Test
    void playMultiPropertyWithoutColorChoiceThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        assertThrows(IllegalArgumentException.class, () -> engine.playPropertyCard("p1", 0, ""));
    }

    @Test
    void playMultiPropertyWithInvalidColorChoiceThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0));
        assertThrows(IllegalArgumentException.class, () ->
                engine.playPropertyCard("p1", 0, "Pink"));
    }

    @Test
    void playNonPropertyAsPropertyThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        assertThrows(IllegalArgumentException.class, () -> engine.playPropertyCard("p1", 0, ""));
    }

    @Test
    void propertyAutoGroupsByColor() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0),
                TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playPropertyCard("p1", 0, "");
        engine.playPropertyCard("p1", 0, "");
        assertEquals(2, p1.propertyCount("Brown"));
    }

    @Test
    void completeSetCreatesNewPropertyGroup() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0),
                TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0),
                TestHelpers.card("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0));
        engine.playPropertyCard("p1", 0, "");
        engine.playPropertyCard("p1", 0, "");
        engine.playPropertyCard("p1", 0, "Brown");
        assertEquals(2, p1.propertyCount("Brown"));
        assertTrue(p1.properties().containsKey("Brown (2)"));
        assertEquals(1, p1.propertyCount("Brown (2)"));
    }

    // ========== Change property color tests ==========

    @Test
    void changeMultiPropertyColor() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        engine.playPropertyCard("p1", 0, "Brown");
        assertEquals(1, p1.propertyCount("Brown"));
        engine.changePropertyColor("p1", "Brown", 0, "Deep Blue");
        assertEquals(0, p1.propertyCount("Brown"));
        assertEquals(1, p1.propertyCount("Deep Blue"));
    }

    @Test
    void changeSinglePropertyColorThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playPropertyCard("p1", 0, "");
        assertThrows(IllegalArgumentException.class, () ->
                engine.changePropertyColor("p1", "Brown", 0, "Light Blue"));
    }

    @Test
    void changeToSameColorThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        engine.playPropertyCard("p1", 0, "Brown");
        assertThrows(IllegalArgumentException.class, () ->
                engine.changePropertyColor("p1", "Brown", 0, "Brown"));
    }

    @Test
    void cannotMovePropertyFromCompleteSetWithHouse() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        // Brown: [Wild Property, Baltic Avenue] = complete set (2 cards) + house
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addHouse("Brown"); // House on the complete Brown set
        assertTrue(p1.hasHouse("Brown"));
        // Try to move the multi-property from a complete set with house → IllegalStateException
        assertThrows(IllegalStateException.class, () ->
                engine.changePropertyColor("p1", "Brown", 0, "Deep Blue"));
    }

    @Test
    void changePropertyColorAllowedDuringTurn() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0),
                TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        engine.playPropertyCard("p1", 0, "Brown");
        engine.changePropertyColor("p1", "Brown", 0, "Light Blue");
        assertEquals(1, p1.propertyCount("Light Blue"));
        assertEquals(0, p1.propertyCount("Brown"));
    }

    // ========== Rent card tests ==========

    @Test
    void twoColorRentAffectsAllOpponents() {
        GameEngine engine = new GameEngine(new TestHelpers.FixedDeckService());
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p1", "P1", false));
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p2", "P2", false));
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p3", "P3", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.setReady("p3", true);
        engine.startGame("p1");
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        PlayerState p3 = engine.playerState("p3");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0),
                TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0),
                TestHelpers.card("Rent Light Blue-Brown", CardType.RENT, "Light Blue/Brown", 1));
        // Give opponents exact rent amount (Brown=2 complete → base rent = 2)
        // Two 1M cards so payment is exactly 2
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p3.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p3.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        int initialP1Bank = p1.bankTotal();
        engine.playPropertyCard("p1", 0, "");
        engine.playPropertyCard("p1", 0, "");
        engine.playRentCard("p1", 0, "", "Brown", 0);
        // 2M from each opponent = 4M total
        assertEquals(initialP1Bank + 4, p1.bankTotal());
    }

    @Test
    void rentRequiresProperty() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Rent Wild", CardType.RENT, "Any", 3));
        // Wild rent requires colorChoice, and player must have that color property
        assertThrows(IllegalArgumentException.class, () ->
                engine.playRentCard("p1", 0, "p2", "Brown", 0));
    }

    @Test
    void rentCannotTargetSelf() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0),
                TestHelpers.card("Rent Wild", CardType.RENT, "Any", 3));
        engine.playPropertyCard("p1", 0, "");
        assertThrows(IllegalArgumentException.class, () ->
                engine.playRentCard("p1", 0, "p1", "Brown", 0));
    }

    @Test
    void doubleRentMultipliesCharge() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0),
                TestHelpers.card("Rent Wild", CardType.RENT, "Any", 3),
                TestHelpers.card("Double The Rent", CardType.ACTION, "-", 1));
        // Give p2 2M (exact amount for 1 Brown doubled)
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        int initialP1Bank = p1.bankTotal();
        engine.playPropertyCard("p1", 0, "");
        engine.playRentCard("p1", 0, "p2", "Brown", 1); // 1 double
        // Base rent for 1 Brown = 1, doubled = 2
        assertEquals(initialP1Bank + 2, p1.bankTotal());
    }

    @Test
    void rentWithExactPaymentWorks() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        // Give p1 2 Brown properties (complete set for Brown → base rent = 2)
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        TestHelpers.replaceHand(p1, TestHelpers.card("Rent Wild", CardType.RENT, "Any", 3));
        // Give p2 2x 1M (= 2M)
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        int initialP1Bank = p1.bankTotal();
        engine.playRentCard("p1", 0, "p2", "Brown", 0);
        assertEquals(initialP1Bank + 2, p1.bankTotal());
    }
}
