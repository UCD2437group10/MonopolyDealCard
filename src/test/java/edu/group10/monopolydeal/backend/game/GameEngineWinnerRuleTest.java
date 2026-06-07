package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.assertFalse;

import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.DeckService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameEngineWinnerRuleTest {

    @Test
    void sameBaseColorMultipleCompleteSetsDoNotCountAsThreeWins() {
        GameEngine engine = new GameEngine(new FixedDeckService());
        engine.addPlayer(new Player("p1", "Player1", false));
        engine.addPlayer(new Player("p2", "Player2", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.startGame("p1");

        PlayerState p1 = engine.playerState("p1");
        while (!p1.hand().isEmpty()) {
            p1.removeHandCard(p1.hand().size() - 1);
        }
        p1.addToHand(new SimpleCard("1M Money", CardType.MONEY, "Yellow", 1));

        p1.addPropertyToExactGroup("Brown", new SimpleCard("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown", new SimpleCard("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown (2)", new SimpleCard("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        p1.addPropertyToExactGroup("Brown (2)", new SimpleCard("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        p1.addPropertyToExactGroup("Brown (3)", new SimpleCard("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0));
        p1.addPropertyToExactGroup("Brown (3)", new SimpleCard("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0));

        engine.playMoneyCard("p1", 0);

        assertFalse(engine.snapshot().gameOver());
    }

    private static final class FixedDeckService extends DeckService {
        @Override
        public List<edu.group10.monopolydeal.backend.model.card.Card> createDeck() {
            List<edu.group10.monopolydeal.backend.model.card.Card> deck = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                deck.add(new SimpleCard("1M Money", CardType.MONEY, "Yellow", 1));
            }
            return deck;
        }
    }
}
