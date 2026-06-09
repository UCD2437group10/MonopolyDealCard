package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.*;

import edu.group10.monopolydeal.TestHelpers;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.service.DeckService;
import org.junit.jupiter.api.Test;

/**
 * Tests for winner detection, turn flow, payment, and hand limits.
 */
class GameEngineTurnAndWinnerTest {

    // A deck with many cards so tests don't run out
    static final class LargeDeckService extends DeckService {
        @Override
        public List<Card> createDeck() {
            List<Card> deck = new ArrayList<>();
            for (int i = 0; i < 60; i++) {
                deck.add(new SimpleCard("1M Money", CardType.MONEY, "Yellow", 1));
            }
            return deck;
        }
    }

    // ========== Turn management ==========

    @Test
    void endTurnPassesToNextPlayer() {
        GameEngine engine = TestHelpers.createStartedEngine();
        assertEquals("p1", engine.snapshot().currentPlayerId());
        engine.endTurn("p1");
        assertEquals("p2", engine.snapshot().currentPlayerId());
    }

    @Test
    void endTurnWrapsAround() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p1", "P1", false));
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p2", "P2", false));
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p3", "P3", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.setReady("p3", true);
        engine.startGame("p1");
        assertEquals("p1", engine.snapshot().currentPlayerId());
        engine.endTurn("p1");
        assertEquals("p2", engine.snapshot().currentPlayerId());
        engine.endTurn("p2");
        assertEquals("p3", engine.snapshot().currentPlayerId());
        engine.endTurn("p3");
        assertEquals("p1", engine.snapshot().currentPlayerId());
    }

    @Test
    void endTurnWithMoreThan7CardsThrows() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p1", "P1", false));
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p2", "P2", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.startGame("p1");
        PlayerState p1 = engine.playerState("p1");
        // p1 has 7 cards (5+2), add more to exceed 7
        for (int i = 0; i < 3; i++) {
            p1.addToHand(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        }
        assertEquals(10, p1.hand().size());
        assertThrows(IllegalStateException.class, () -> engine.endTurn("p1"));
    }

    @Test
    void endTurnDraws2CardsForNextPlayer() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p2 = engine.playerState("p2");
        int initialSize = p2.hand().size();
        engine.endTurn("p1");
        assertEquals(initialSize + 2, p2.hand().size());
    }

    @Test
    void endTurnWithEmptyHandDraws5Cards() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p2 = engine.playerState("p2");
        while (!p2.hand().isEmpty()) {
            p2.removeHandCard(0);
        }
        assertEquals(0, p2.hand().size());
        engine.endTurn("p1");
        assertEquals(5, p2.hand().size());
    }

    @Test
    void cannotEndTurnIfNotCurrentPlayer() {
        GameEngine engine = TestHelpers.createStartedEngine();
        assertThrows(IllegalStateException.class, () -> engine.endTurn("p2"));
    }

    @Test
    void maxThreeActionsPerTurn() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1,
                TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1),
                TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1),
                TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1),
                TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        engine.playMoneyCard("p1", 0);
        engine.playMoneyCard("p1", 0);
        engine.playMoneyCard("p1", 0);
        assertThrows(IllegalStateException.class, () -> engine.playMoneyCard("p1", 0));
    }

    @Test
    void actionsResetAfterEndTurn() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p1", "P1", false));
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p2", "P2", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.startGame("p1");
        PlayerState p1 = engine.playerState("p1");
        // Play 3 money cards
        int actions = 0;
        for (int i = 0; i < 3 && i < p1.hand().size(); i++) {
            if (p1.hand().get(0).bankValue() > 0) {
                engine.playMoneyCard("p1", 0);
                actions++;
            }
        }
        engine.endTurn("p1");
        engine.endTurn("p2");
        // Back to p1, should be able to play
        assertEquals("p1", engine.snapshot().currentPlayerId());
        if (!p1.hand().isEmpty() && p1.hand().get(0).bankValue() > 0) {
            assertDoesNotThrow(() -> engine.playMoneyCard("p1", 0));
        }
    }

    // ========== Winner detection ==========

    @Test
    void threeCompleteSetsWin() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p1", "P1", false));
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p2", "P2", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.startGame("p1");
        PlayerState p1 = engine.playerState("p1");

        // Set up 2 complete sets via addPropertyToExactGroup, then play the 3rd via playPropertyCard
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));

        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Oriental Avenue", CardType.PROPERTY, "Light Blue", 0));
        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Vermont Avenue", CardType.PROPERTY, "Light Blue", 0));
        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Connecticut Avenue", CardType.PROPERTY, "Light Blue", 0));

        // Play the 3rd complete set using playPropertyCard (triggers refreshWinner)
        TestHelpers.replaceHand(p1,
                TestHelpers.card("St. Charles Place", CardType.PROPERTY, "Pink", 0),
                TestHelpers.card("States Avenue", CardType.PROPERTY, "Pink", 0),
                TestHelpers.card("Virginia Avenue", CardType.PROPERTY, "Pink", 0));
        engine.playPropertyCard("p1", 0, "");
        engine.playPropertyCard("p1", 0, "");
        engine.playPropertyCard("p1", 0, "");

        assertTrue(engine.snapshot().gameOver());
        assertEquals("p1", engine.snapshot().winnerPlayerId());
    }

    @Test
    void sameColorMultipleSetsDontCountSeparately() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p1", "P1", false));
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p2", "P2", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.startGame("p1");
        PlayerState p1 = engine.playerState("p1");

        // 3 Brown complete sets (same base color — should NOT trigger win)
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown (2)", TestHelpers.card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        p1.addPropertyToExactGroup("Brown (2)", TestHelpers.card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        p1.addPropertyToExactGroup("Brown (3)", TestHelpers.card("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0));
        p1.addPropertyToExactGroup("Brown (3)", TestHelpers.card("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0));

        // Play a property card to trigger winner check
        TestHelpers.replaceHand(p1, TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playPropertyCard("p1", 0, "");
        assertFalse(engine.snapshot().gameOver());
    }

    @Test
    void incompleteSetsDontTriggerWin() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p1", "P1", false));
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p2", "P2", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.startGame("p1");
        PlayerState p1 = engine.playerState("p1");

        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Oriental Avenue", CardType.PROPERTY, "Light Blue", 0));
        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Vermont Avenue", CardType.PROPERTY, "Light Blue", 0));
        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Connecticut Avenue", CardType.PROPERTY, "Light Blue", 0));
        // Pink = incomplete (1 card)
        p1.addPropertyToExactGroup("Pink", TestHelpers.card("St. Charles Place", CardType.PROPERTY, "Pink", 0));

        TestHelpers.replaceHand(p1, TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playPropertyCard("p1", 0, "");
        assertFalse(engine.snapshot().gameOver());
    }

    @Test
    void cannotPlayOnGameOver() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p1", "P1", false));
        engine.addPlayer(new edu.group10.monopolydeal.backend.model.player.Player("p2", "P2", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.startGame("p1");
        PlayerState p1 = engine.playerState("p1");

        // Set up 2 complete sets
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Deep Blue", TestHelpers.card("Boardwalk", CardType.PROPERTY, "Deep Blue", 0));
        p1.addPropertyToExactGroup("Deep Blue", TestHelpers.card("Park Place", CardType.PROPERTY, "Deep Blue", 0));

        // Play 3rd set to win
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Oriental Avenue", CardType.PROPERTY, "Light Blue", 0),
                TestHelpers.card("Vermont Avenue", CardType.PROPERTY, "Light Blue", 0),
                TestHelpers.card("Connecticut Avenue", CardType.PROPERTY, "Light Blue", 0));
        engine.playPropertyCard("p1", 0, "");
        engine.playPropertyCard("p1", 0, "");
        engine.playPropertyCard("p1", 0, "");

        assertTrue(engine.snapshot().gameOver());
        assertThrows(IllegalStateException.class, () -> engine.playMoneyCard("p1", 0));
    }

    // ========== Payment system ==========

    @Test
    void paymentUsesBankBeforeProperties() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3));
        // Exact 5M in bank - drainBankForPayment takes last element
        p2.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        p2.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        int p2PropertyCountBefore = p2.propertyCount("Brown");
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        // Paid exactly 5M from bank, property untouched
        assertTrue(p2.bank().isEmpty()); // 5M card taken
        assertEquals(p2PropertyCountBefore, p2.propertyCount("Brown"));
        assertEquals(5, p1.bankTotal());
    }

    @Test
    void paymentUsesPropertiesWhenBankInsufficient() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3));
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1)); // Only 1M
        p2.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p2.addProperty("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        // Bank 1M covers 1, remaining 4 taken from properties (each = 1M value)
        assertTrue(p2.bank().isEmpty());
        assertTrue(p2.propertyCount("Brown") < 2); // Some properties taken
    }

    @Test
    void passivePlayerDoesntLoseWhenActionIsCancelled() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        TestHelpers.replaceHand(p1, TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3));
        TestHelpers.replaceHand(p2, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        p2.addToBank(TestHelpers.card("5M Money", CardType.MONEY, "Purple", 5));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        engine.respondJustSayNo("p2", true);
        assertEquals(5, p2.bankTotal()); // Kept money
    }
}
