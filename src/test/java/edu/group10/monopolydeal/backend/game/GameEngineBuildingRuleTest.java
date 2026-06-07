package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.DeckService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameEngineBuildingRuleTest {

    @Test
    void hotelCannotBeAddedTwiceToSamePropertySet() {
        GameEngine engine = createStartedEngine();
        PlayerState p1 = engine.playerState("p1");

        replaceHand(
                p1,
                card("House", CardType.ACTION, "-", 3),
                card("Hotel", CardType.ACTION, "-", 4),
                card("Hotel", CardType.ACTION, "-", 4)
        );
        p1.addPropertyToExactGroup("Brown", card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown", card("Baltic Avenue", CardType.PROPERTY, "Brown", 0));

        engine.playActionCard("p1", 0, Map.of("color", "Brown"));
        engine.playActionCard("p1", 0, Map.of("color", "Brown"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> engine.playActionCard("p1", 0, Map.of("color", "Brown"))
        );

        assertEquals("hotel already exists on this set", exception.getMessage());
        assertEquals(1, p1.hotelByColor().getOrDefault("Brown", 0));
    }

    private GameEngine createStartedEngine() {
        GameEngine engine = new GameEngine(new FixedDeckService());
        engine.addPlayer(new Player("p1", "Player1", false));
        engine.addPlayer(new Player("p2", "Player2", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.startGame("p1");
        return engine;
    }

    private void replaceHand(PlayerState playerState, Card... cards) {
        while (!playerState.hand().isEmpty()) {
            playerState.removeHandCard(playerState.hand().size() - 1);
        }
        for (Card card : cards) {
            playerState.addToHand(card);
        }
    }

    private Card card(String name, CardType type, String color, int value) {
        return new SimpleCard(name, type, color, value);
    }

    private static final class FixedDeckService extends DeckService {
        @Override
        public List<Card> createDeck() {
            List<Card> deck = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                deck.add(new SimpleCard("Pass Go", CardType.ACTION, "-", 1));
            }
            return deck;
        }
    }
}
