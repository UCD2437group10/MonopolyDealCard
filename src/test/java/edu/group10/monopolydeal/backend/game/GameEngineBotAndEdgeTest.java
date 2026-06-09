package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.*;

import edu.group10.monopolydeal.TestHelpers;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.DeckService;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for bot turn AI and various edge cases.
 */
class GameEngineBotAndEdgeTest {

    // ========== Bot turn tests ==========

    @Test
    void botIsNotRequiredToBeReady() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Host", false));
        engine.addPlayer(new Player("p2", "Bot1", true));
        engine.setReady("p1", true);
        assertDoesNotThrow(() -> engine.startGame("p1"));
    }

    @Test
    void botPlaysTurnAutomatically() {
        GameEngine engine = new GameEngine(new TestHelpers.FixedDeckService());
        engine.addPlayer(new Player("p1", "Human", false));
        engine.addPlayer(new Player("p2", "Bot", true));
        engine.setReady("p1", true);
        engine.startGame("p1");
        // Human plays 3 money cards and ends turn
        PlayerState p1 = engine.playerState("p1");
        for (int i = 0; i < Math.min(3, p1.hand().size()); i++) {
            if (p1.hand().get(0).bankValue() > 0) {
                engine.playMoneyCard("p1", 0);
            }
        }
        engine.endTurn("p1");
        assertEquals("p2", engine.snapshot().currentPlayerId());
        assertDoesNotThrow(() -> engine.playBotTurn("p2"));
    }

    @Test
    void cannotPlayBotTurnForHumanPlayer() {
        GameEngine engine = TestHelpers.createStartedEngine();
        assertThrows(IllegalArgumentException.class, () -> engine.playBotTurn("p1"));
    }

    @Test
    void playBotTurnForNonexistentPlayerThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        // ensureTurnAlive ensures started, not game over, and current player
        // p99 is not current, so IllegalStateException
        assertThrows(IllegalStateException.class, () -> engine.playBotTurn("p99"));
    }

    @Test
    void botRespondsJustSayNoAutomatically() {
        GameEngine engine = new GameEngine(new TestHelpers.FixedDeckService());
        engine.addPlayer(new Player("p1", "Human", false));
        engine.addPlayer(new Player("p2", "Bot", true));
        engine.setReady("p1", true);
        engine.startGame("p1");
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3));
        TestHelpers.replaceHand(p2, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        p2.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        // Bot auto-uses JSN when the debt collector targets it
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        // After bot auto-JSN + actor doesn't have JSN → effect cancelled
        assertTrue(engine.snapshot().jsnResponderPlayerId().isEmpty());
    }

    @Test
    void botEndsTurnAutomatically() {
        GameEngine engine = new GameEngine(new TestHelpers.FixedDeckService());
        engine.addPlayer(new Player("p1", "Human", false));
        engine.addPlayer(new Player("p2", "Bot", true));
        engine.setReady("p1", true);
        engine.startGame("p1");
        engine.endTurn("p1");
        engine.playBotTurn("p2");
        assertEquals("p1", engine.snapshot().currentPlayerId());
    }

    // ========== Edge case tests ==========

    @Test
    void gameNotStartedChecks() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Test", false));
        engine.addPlayer(new Player("p2", "Test2", false));
        assertThrows(IllegalStateException.class, () -> engine.playMoneyCard("p1", 0));
        assertThrows(IllegalStateException.class, () -> engine.playPropertyCard("p1", 0, ""));
        assertThrows(IllegalStateException.class, () -> engine.endTurn("p1"));
    }

    @Test
    void unknownPlayerThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        assertThrows(IllegalArgumentException.class, () -> engine.playerState("p99"));
    }

    @Test
    void playMoneyWhenJsnPendingThrows() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3),
                TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        TestHelpers.replaceHand(p2, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        p2.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        assertFalse(engine.snapshot().jsnResponderPlayerId().isEmpty());
        assertThrows(IllegalStateException.class, () -> engine.playMoneyCard("p1", 0));
    }

    @Test
    void validActionCardPayloadKeysAreChecked() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Pass Go", CardType.ACTION, "-", 1));
        int beforeSize = p1.hand().size();
        engine.playActionCard("p1", 0, Map.of());
        assertEquals(beforeSize - 1 + 2, p1.hand().size());
    }

    @Test
    void playPropertyWithEmptyColorForSinglePropertyIsFine() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        assertDoesNotThrow(() -> engine.playPropertyCard("p1", 0, ""));
        assertTrue(p1.hasProperty("Brown"));
    }

    // ========== Payment edge cases ==========

    @Test
    void multipleDebtCollectorsAccumulateBank() {
        GameEngine engine = TestHelpers.createStartedEngine3P();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        PlayerState p3 = engine.playerState("p3");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3),
                TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3));
        // Use smaller bank values to avoid overpay
        p2.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        p3.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p3"));
        // Each collects 5M, total 10M
        assertEquals(10, p1.bankTotal());
    }

    @Test
    void actionCardReturnedOnInvalidPlay() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("House", CardType.ACTION, "-", 3));
        int handSizeBefore = p1.hand().size(); // Should be 1 (just the House card)
        try {
            engine.playActionCard("p1", 0, Map.of("color", "Railroad"));
            fail("Expected exception");
        } catch (Exception e) {
            assertEquals(handSizeBefore, p1.hand().size());
        }
    }

    // ========== Sly Deal edge cases ==========

    @Test
    void slyDealCannotStealFromSelf() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("Sly Deal", CardType.ACTION, "-", 3));
        assertThrows(IllegalArgumentException.class, () ->
                engine.playActionCard("p1", 0, Map.of(
                        "targetPlayerId", "p1", "color", "Brown", "propertyIndex", "0")));
    }

    @Test
    void forcedDealCannotSwapWithSelf() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        engine.playerState("p2").addProperty("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addProperty("Light Blue", TestHelpers.card("Oriental Avenue", CardType.PROPERTY, "Light Blue", 0));
        TestHelpers.replaceHand(p1, TestHelpers.card("Forced Deal", CardType.ACTION, "-", 3));
        assertThrows(IllegalArgumentException.class, () ->
                engine.playActionCard("p1", 0, Map.of(
                        "targetPlayerId", "p1",
                        "myColor", "Light Blue", "myIndex", "0",
                        "targetColor", "Brown", "targetIndex", "0")));
    }

    @Test
    void dealBreakerStealsCompleteSetWithHouse() {
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
}
