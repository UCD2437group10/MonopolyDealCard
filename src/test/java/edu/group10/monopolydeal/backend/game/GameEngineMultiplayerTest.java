package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.*;

import edu.group10.monopolydeal.TestHelpers;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.DeckService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Multiplayer-specific tests: 3P, 4P, mixed human/bot, complex interaction chains.
 */
class GameEngineMultiplayerTest {

    static final class LargeDeckService extends DeckService {
        @Override
        public List<edu.group10.monopolydeal.backend.model.card.Card> createDeck() {
            List<edu.group10.monopolydeal.backend.model.card.Card> deck = new ArrayList<>();
            for (int i = 0; i < 60; i++) {
                deck.add(new SimpleCard("1M Money", CardType.MONEY, "Yellow", 1));
            }
            for (int i = 0; i < 30; i++) {
                deck.add(new SimpleCard("Pass Go", CardType.ACTION, "-", 1));
            }
            // Add properties for testing
            for (int i = 0; i < 8; i++) {
                deck.add(new SimpleCard("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
            }
            for (int i = 0; i < 8; i++) {
                deck.add(new SimpleCard("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
            }
            for (int i = 0; i < 8; i++) {
                deck.add(new SimpleCard("Oriental Avenue", CardType.PROPERTY, "Light Blue", 0));
            }
            for (int i = 0; i < 8; i++) {
                deck.add(new SimpleCard("Vermont Avenue", CardType.PROPERTY, "Light Blue", 0));
            }
            for (int i = 0; i < 8; i++) {
                deck.add(new SimpleCard("Connecticut Avenue", CardType.PROPERTY, "Light Blue", 0));
            }
            // Add action cards
            for (int i = 0; i < 8; i++) {
                deck.add(new SimpleCard("Debt Collector", CardType.ACTION, "-", 3));
            }
            for (int i = 0; i < 8; i++) {
                deck.add(new SimpleCard("Just Say No", CardType.ACTION, "-", 4));
            }
            for (int i = 0; i < 5; i++) {
                deck.add(new SimpleCard("Deal Breaker", CardType.ACTION, "-", 5));
            }
            for (int i = 0; i < 5; i++) {
                deck.add(new SimpleCard("Sly Deal", CardType.ACTION, "-", 3));
            }
            for (int i = 0; i < 5; i++) {
                deck.add(new SimpleCard("Forced Deal", CardType.ACTION, "-", 3));
            }
            for (int i = 0; i < 5; i++) {
                deck.add(new SimpleCard("House", CardType.ACTION, "-", 3));
            }
            for (int i = 0; i < 3; i++) {
                deck.add(new SimpleCard("Hotel", CardType.ACTION, "-", 4));
            }
            for (int i = 0; i < 5; i++) {
                deck.add(new SimpleCard("It's My Birthday", CardType.ACTION, "-", 2));
            }
            return deck;
        }
    }

    static GameEngine create4PEngine() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new Player("p1", "Alice", false));
        engine.addPlayer(new Player("p2", "Bob", false));
        engine.addPlayer(new Player("p3", "Charlie", false));
        engine.addPlayer(new Player("p4", "Diana", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.setReady("p3", true);
        engine.setReady("p4", true);
        engine.startGame("p1");
        return engine;
    }

    static GameEngine create3PEngine() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new Player("p1", "Alice", false));
        engine.addPlayer(new Player("p2", "Bob", false));
        engine.addPlayer(new Player("p3", "Charlie", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.setReady("p3", true);
        engine.startGame("p1");
        return engine;
    }

    static GameEngine createMixedBotEngine() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new Player("p1", "Human1", false));
        engine.addPlayer(new Player("p2", "Human2", false));
        engine.addPlayer(new Player("p3", "Bot1", true));
        engine.addPlayer(new Player("p4", "Bot2", true));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        // Bots don't need ready
        engine.startGame("p1");
        return engine;
    }

    // ========== 4-player game structure ==========

    @Test
    void fourPlayerGameHasCorrectTurnOrder() {
        GameEngine engine = create4PEngine();
        assertEquals("p1", engine.snapshot().currentPlayerId());
        engine.endTurn("p1");
        assertEquals("p2", engine.snapshot().currentPlayerId());
        engine.endTurn("p2");
        assertEquals("p3", engine.snapshot().currentPlayerId());
        engine.endTurn("p3");
        assertEquals("p4", engine.snapshot().currentPlayerId());
        engine.endTurn("p4");
        assertEquals("p1", engine.snapshot().currentPlayerId());
    }

    @Test
    void fourPlayerGameDealsCorrectly() {
        GameEngine engine = create4PEngine();
        // p1: 5 start + 2 turn-start = 7; p2-p4: 5 each
        assertEquals(7, engine.playerState("p1").hand().size());
        assertEquals(5, engine.playerState("p2").hand().size());
        assertEquals(5, engine.playerState("p3").hand().size());
        assertEquals(5, engine.playerState("p4").hand().size());
    }

    @Test
    void fourPlayerAllHumanMustBeReady() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new Player("p1", "Alice", false));
        engine.addPlayer(new Player("p2", "Bob", false));
        engine.addPlayer(new Player("p3", "Charlie", false));
        engine.addPlayer(new Player("p4", "Diana", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        // p3, p4 not ready → cannot start
        assertThrows(IllegalStateException.class, () -> engine.startGame("p1"));
    }

    // ========== Multi-player mixed human/bot ==========

    @Test
    void mixedBotGameBotsDontNeedToBeReady() {
        GameEngine engine = createMixedBotEngine();
        assertTrue(engine.snapshot().started());
        assertEquals(4, engine.snapshot().players().size());
    }

    @Test
    void botTurnAdvancesAutomaticallyInMixedGame() {
        GameEngine engine = createMixedBotEngine();
        // p1 plays and ends
        engine.endTurn("p1");
        // p2 plays and ends
        engine.endTurn("p2");
        // p3 is bot → playBotTurn
        assertEquals("p3", engine.snapshot().currentPlayerId());
        assertTrue(engine.playerState("p3").player().bot());
        engine.playBotTurn("p3");
        // After bot turn, it's p4 (also bot)
        assertEquals("p4", engine.snapshot().currentPlayerId());
    }

    // ========== Multi-player rent tests ==========

    @Test
    void twoColorRentHitsAllOpponentsIn4P() {
        GameEngine engine = create4PEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        PlayerState p3 = engine.playerState("p3");
        PlayerState p4 = engine.playerState("p4");

        // p1: 2 Brown (complete set) + two-color rent card
        TestHelpers.replaceHand(p1,
                TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0),
                TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playPropertyCard("p1", 0, "");
        engine.playPropertyCard("p1", 0, "");

        // Give each opponent 2x1M = 2M exact payment
        for (PlayerState ps : List.of(p2, p3, p4)) {
            ps.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
            ps.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        }

        // Now p1 needs a two-color rent. Use rent from hand
        // Actually p1 needs to have a rent card. Let's inject one into hand.
        engine.playerState("p1").addToHand(TestHelpers.card("Rent Light Blue-Brown", CardType.RENT, "Light Blue/Brown", 1));
        int initialBankP1 = p1.bankTotal();
        engine.playRentCard("p1", p1.hand().size() - 1, "", "Brown", 0);

        // All 3 opponents pay 2M each = 6M
        assertEquals(initialBankP1 + 6, p1.bankTotal());
    }

    @Test
    void itsMyBirthdayHitsAllOpponentsIn4P() {
        GameEngine engine = create4PEngine();
        PlayerState p1 = engine.playerState("p1");
        TestHelpers.replaceHand(p1, TestHelpers.card("It's My Birthday", CardType.ACTION, "-", 2));
        // Give each of 3 opponents 2x1M = 2M
        for (String id : List.of("p2", "p3", "p4")) {
            engine.playerState(id).addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
            engine.playerState(id).addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        }
        int initialBankP1 = p1.bankTotal();
        engine.playActionCard("p1", 0, Map.of());
        // 3 opponents × 2M = 6M
        assertEquals(initialBankP1 + 6, p1.bankTotal());
    }

    // ========== Multi-player JSN chain ==========

    @Test
    void jsnChainAcrossThreePlayers() {
        GameEngine engine = create3PEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        PlayerState p3 = engine.playerState("p3");

        // p1 plays It's My Birthday
        TestHelpers.replaceHand(p1, TestHelpers.card("It's My Birthday", CardType.ACTION, "-", 2));
        // p2 has JSN, p3 also has JSN
        TestHelpers.replaceHand(p2, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        TestHelpers.replaceHand(p3, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        // Give opponents money
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p3.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p3.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));

        int initialBankP1 = p1.bankTotal();
        engine.playActionCard("p1", 0, Map.of());

        // p2 has JSN → gets prompt
        assertEquals("p2", engine.snapshot().jsnResponderPlayerId());
        engine.respondJustSayNo("p2", true); // p2 uses JSN → effect cancelled for p2

        // p3 has JSN → gets prompt next
        assertEquals("p3", engine.snapshot().jsnResponderPlayerId());
        engine.respondJustSayNo("p3", true); // p3 uses JSN → effect cancelled for p3

        // Both cancelled, no money collected
        assertEquals(initialBankP1, p1.bankTotal());
    }

    @Test
    void jsnChainOneCancelsOnePaysIn3P() {
        GameEngine engine = create3PEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");
        PlayerState p3 = engine.playerState("p3");

        TestHelpers.replaceHand(p1, TestHelpers.card("It's My Birthday", CardType.ACTION, "-", 2));
        TestHelpers.replaceHand(p2, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        // p3 has no JSN (Pass Go instead)
        TestHelpers.replaceHand(p3, TestHelpers.card("Pass Go", CardType.ACTION, "-", 1));
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p2.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p3.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        p3.addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));

        int initialBankP1 = p1.bankTotal();
        engine.playActionCard("p1", 0, Map.of());

        // p2 has JSN → gets prompt, uses it → cancelled for p2
        assertEquals("p2", engine.snapshot().jsnResponderPlayerId());
        engine.respondJustSayNo("p2", true);
        // p3 has no JSN → auto-pass → p3 pays 2M
        // Only 2M from p3
        assertEquals(initialBankP1 + 2, p1.bankTotal());
    }

    @Test
    void dealBreakerJsnChainIn3PWithBot() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new Player("p1", "Human", false));
        engine.addPlayer(new Player("p2", "Bot1", true));
        engine.addPlayer(new Player("p3", "Human2", false));
        engine.setReady("p1", true);
        engine.setReady("p3", true);
        engine.startGame("p1");

        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        // Set up p2 with complete set
        p2.addProperty("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p2.addProperty("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        // p2 has JSN (bot auto-uses it)
        TestHelpers.replaceHand(p2, TestHelpers.card("Just Say No", CardType.ACTION, "-", 4));
        TestHelpers.replaceHand(p1, TestHelpers.card("Deal Breaker", CardType.ACTION, "-", 5));

        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2", "color", "Brown"));
        // Bot auto-uses JSN, then p1 doesn't have JSN, so deal breaker goes through
        // Actually: bot auto-JSN → actor(p1) might not have JSN → effect applies
    }

    // ========== Multiple actions across players ==========

    @Test
    void fullRoundAllPlayersActAndEndTurn4P() {
        GameEngine engine = create4PEngine();
        // Each player plays money cards and ends turn
        for (int round = 0; round < 2; round++) {
            for (String pid : List.of("p1", "p2", "p3", "p4")) {
                assertEquals(pid, engine.snapshot().currentPlayerId());
                PlayerState ps = engine.playerState(pid);
                int actions = 0;
                List<Integer> moneyIndices = findMoneyCardIndices(ps);
                for (int idx : moneyIndices) {
                    if (actions >= 3) break;
                    engine.playMoneyCard(pid, idx - actions); // index shifts
                    actions++;
                }
                engine.endTurn(pid);
            }
        }
        // Should be p1's turn again after 2 full rounds
        assertEquals("p1", engine.snapshot().currentPlayerId());
    }

    private List<Integer> findMoneyCardIndices(PlayerState ps) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < ps.hand().size(); i++) {
            var c = ps.hand().get(i);
            if (c.type() == CardType.MONEY && c.bankValue() > 0) {
                indices.add(i);
            }
        }
        return indices;
    }

    // ========== Host management ==========

    @Test
    void onlyHostCanStartGameInMultiplayer() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new Player("p1", "Host", false));
        engine.addPlayer(new Player("p2", "Guest1", false));
        engine.addPlayer(new Player("p3", "Guest2", false));
        engine.addPlayer(new Player("p4", "Guest3", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.setReady("p3", true);
        engine.setReady("p4", true);
        // p2 is not host
        assertThrows(IllegalStateException.class, () -> engine.startGame("p2"));
        assertThrows(IllegalStateException.class, () -> engine.startGame("p3"));
        // p1 (host) works
        assertDoesNotThrow(() -> engine.startGame("p1"));
    }

    @Test
    void hostIsFirstJoinedPlayer() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new Player("p3", "Guest2", false));
        engine.addPlayer(new Player("p1", "Host", false));
        engine.addPlayer(new Player("p2", "Guest1", false));
        engine.setReady("p3", true);
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        // p3 is first joined, should be host
        assertDoesNotThrow(() -> engine.startGame("p3"));
        // p1 is NOT host, can't start
        engine.resetGame();
        engine.addPlayer(new Player("p3", "Guest2", false));
        engine.addPlayer(new Player("p1", "Host", false));
        engine.addPlayer(new Player("p2", "Guest1", false));
        engine.setReady("p3", true);
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        assertThrows(IllegalStateException.class, () -> engine.startGame("p1"));
    }

    // ========== Reset in multiplayer ==========

    @Test
    void resetPreservesPlayerOrderForRejoin() {
        GameEngine engine = create4PEngine();
        engine.resetGame();
        // Re-add players, verify order
        engine.addPlayer(new Player("p1", "Alice", false));
        engine.addPlayer(new Player("p2", "Bob", false));
        engine.addPlayer(new Player("p3", "Charlie", false));
        engine.addPlayer(new Player("p4", "Diana", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.setReady("p3", true);
        engine.setReady("p4", true);
        engine.startGame("p1");
        assertEquals("p1", engine.snapshot().currentPlayerId());
    }

    // ========== Multi-player ready/unready management ==========

    @Test
    void unreadyThenRereadyResetsReadiness() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new Player("p1", "Host", false));
        engine.addPlayer(new Player("p2", "Guest1", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        assertTrue(engine.snapshot().readyPlayerIds().contains("p2"));
        engine.setReady("p2", false);
        // Now p2 is not ready, can't start
        assertThrows(IllegalStateException.class, () -> engine.startGame("p1"));
        engine.setReady("p2", true);
        assertDoesNotThrow(() -> engine.startGame("p1"));
    }

    // ========== Multi-player transfer edge cases ==========

    @Test
    void twoColorRentWithBotTargets() {
        GameEngine engine = createMixedBotEngine();
        PlayerState p1 = engine.playerState("p1");
        // Give p1 property + rent card
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        // Give all opponents 1M each
        for (String id : List.of("p2", "p3", "p4")) {
            engine.playerState(id).addToBank(TestHelpers.card("1M Money", CardType.MONEY, "Yellow", 1));
        }
        TestHelpers.replaceHand(p1, TestHelpers.card("Rent Light Blue-Brown", CardType.RENT, "Light Blue/Brown", 1));
        int initialBankP1 = p1.bankTotal();
        engine.playRentCard("p1", 0, "", "Brown", 0);
        // All 3 opponents pay 2M (Brown complete set base = 2)
        // But each only has 1M → bank drained + properties taken for remainder
        // We check p1 received some payment
        assertTrue(p1.bankTotal() > initialBankP1, "p1 should have received payment");
    }

    @Test
    void multiplePlayersTargetSameOpponent() {
        GameEngine engine = create3PEngine();
        PlayerState p2 = engine.playerState("p2");
        // Give p2 lots of money
        p2.addToBank(TestHelpers.card("10M Money", CardType.MONEY, "Gold-Orange", 10));

        // p1 targets p2 with Debt Collector
        TestHelpers.replaceHand(engine.playerState("p1"),
                TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3));
        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        engine.endTurn("p1");

        // p2's turn - p2 ends immediately
        engine.endTurn("p2");

        // p3 also targets p2 with Debt Collector
        TestHelpers.replaceHand(engine.playerState("p3"),
                TestHelpers.card("Debt Collector", CardType.ACTION, "-", 3));
        engine.playActionCard("p3", 0, Map.of("targetPlayerId", "p2"));
        // p2 loses money to both (total 10M lost — actually 5M to p1, 5M to p3)
        // After losing 10M, p2 may have fewer assets
        assertTrue(p2.bankTotal() < 10, "p2 should have lost some money");
    }

    // ========== Winner detection across multiple turns ==========

    @Test
    void playerTwoCollectsThreeSetsBeforePlayerOne() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new Player("p1", "P1", false));
        engine.addPlayer(new Player("p2", "P2", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.startGame("p1");

        // p1 passes turn quickly
        engine.endTurn("p1");

        // p2 sets up 3 complete sets and plays a property card to trigger winner check
        PlayerState p2 = engine.playerState("p2");
        p2.addPropertyToExactGroup("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p2.addPropertyToExactGroup("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        p2.addPropertyToExactGroup("Light Blue", TestHelpers.card("Oriental Avenue", CardType.PROPERTY, "Light Blue", 0));
        p2.addPropertyToExactGroup("Light Blue", TestHelpers.card("Vermont Avenue", CardType.PROPERTY, "Light Blue", 0));
        p2.addPropertyToExactGroup("Light Blue", TestHelpers.card("Connecticut Avenue", CardType.PROPERTY, "Light Blue", 0));
        p2.addPropertyToExactGroup("Pink", TestHelpers.card("St. Charles Place", CardType.PROPERTY, "Pink", 0));
        p2.addPropertyToExactGroup("Pink", TestHelpers.card("States Avenue", CardType.PROPERTY, "Pink", 0));

        // p2 plays the 3rd Pink property via playPropertyCard to trigger winner check
        TestHelpers.replaceHand(p2, TestHelpers.card("Virginia Avenue", CardType.PROPERTY, "Pink", 0));
        engine.playPropertyCard("p2", 0, "");

        assertTrue(engine.snapshot().gameOver());
        assertEquals("p2", engine.snapshot().winnerPlayerId());
    }

    // ========== Edge cases with many players ==========

    @Test
    void playerCannotJoinWithExistingNameIn4P() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new Player("p1", "Alice", false));
        engine.addPlayer(new Player("p2", "Bob", false));
        engine.addPlayer(new Player("p3", "Charlie", false));
        assertThrows(IllegalArgumentException.class, () ->
                engine.addPlayer(new Player("p4", "Alice", false))); // Duplicate name
    }

    @Test
    void emptyNamePlayerCanJoin() {
        GameEngine engine = new GameEngine(new LargeDeckService());
        engine.addPlayer(new Player("p1", "Alice", false));
        assertDoesNotThrow(() -> engine.addPlayer(new Player("p2", "", false)));
        assertDoesNotThrow(() -> engine.addPlayer(new Player("p3", null, false)));
    }
}
