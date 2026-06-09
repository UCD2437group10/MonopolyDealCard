package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.*;

import edu.group10.monopolydeal.TestHelpers;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import org.junit.jupiter.api.Test;

/**
 * Tests for winner detection rules.
 */
class GameEngineWinnerRuleTest {

    @Test
    void sameBaseColorMultipleCompleteSetsDoNotCountAsThreeWins() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");

        while (!p1.hand().isEmpty()) {
            p1.removeHandCard(p1.hand().size() - 1);
        }

        // 3 Brown complete sets (same base color)
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown (2)", TestHelpers.card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        p1.addPropertyToExactGroup("Brown (2)", TestHelpers.card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        p1.addPropertyToExactGroup("Brown (3)", TestHelpers.card("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0));
        p1.addPropertyToExactGroup("Brown (3)", TestHelpers.card("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0));

        // Play a property card to trigger refreshWinner
        p1.addToHand(TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playPropertyCard("p1", 0, "");

        assertFalse(engine.snapshot().gameOver());
    }

    @Test
    void threeDistinctColorsWin() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");

        while (!p1.hand().isEmpty()) {
            p1.removeHandCard(p1.hand().size() - 1);
        }

        // 2 complete sets via direct adds
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Oriental Avenue", CardType.PROPERTY, "Light Blue", 0));
        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Vermont Avenue", CardType.PROPERTY, "Light Blue", 0));
        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Connecticut Avenue", CardType.PROPERTY, "Light Blue", 0));

        // Play 3rd set via playPropertyCard (triggers refreshWinner)
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
    void incompleteSetsDoNotTriggerWin() {
        GameEngine engine = TestHelpers.createStartedEngine();
        PlayerState p1 = engine.playerState("p1");

        while (!p1.hand().isEmpty()) {
            p1.removeHandCard(p1.hand().size() - 1);
        }

        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown", TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Oriental Avenue", CardType.PROPERTY, "Light Blue", 0));
        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Vermont Avenue", CardType.PROPERTY, "Light Blue", 0));
        p1.addPropertyToExactGroup("Light Blue", TestHelpers.card("Connecticut Avenue", CardType.PROPERTY, "Light Blue", 0));
        // Only 2 complete sets, Pink is incomplete
        p1.addPropertyToExactGroup("Pink", TestHelpers.card("St. Charles Place", CardType.PROPERTY, "Pink", 0));

        // Play a property card to trigger winner check
        p1.addToHand(TestHelpers.card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        engine.playPropertyCard("p1", 0, "");

        assertFalse(engine.snapshot().gameOver());
    }
}
