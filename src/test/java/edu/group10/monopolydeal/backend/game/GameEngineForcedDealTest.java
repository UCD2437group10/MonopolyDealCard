package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.backend.network.protocol.ProtocolMapper;
import edu.group10.monopolydeal.backend.service.DeckService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameEngineForcedDealTest {

    @Test
    void forcedDealSnapshotStillRoundTripsAfterSwappingRepeatedColorGroups() {
        GameEngine engine = createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        replaceHand(p1, card("Forced Deal", CardType.ACTION, "-", 3));
        replaceHand(p2);

        p1.addPropertyToExactGroup("Brown", card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p1.addPropertyToExactGroup("Brown (2)", card("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0));
        p2.addPropertyToExactGroup("Railroad", card("Reading Railroad", CardType.PROPERTY, "Railroad", 0));

        engine.playActionCard("p1", 0, Map.of(
                "targetPlayerId", "p2",
                "myColor", "Brown (2)",
                "myIndex", "0",
                "targetColor", "Railroad",
                "targetIndex", "0"
        ));

        GameState snapshot = engine.snapshot();
        assertFalse(snapshot.gameOver());
        assertEquals(1, engine.playerState("p1").properties().get("Railroad").size());
        assertEquals(1, engine.playerState("p2").properties().get("Brown (2)").size());

        GameResponse response = new GameResponse(true, "state", snapshot);
        GameResponse mapped = ProtocolMapper.toDomain(ProtocolMapper.toNet(response));
        assertNotNull(mapped.gameState());
        assertEquals(2, mapped.gameState().players().size());
        assertEquals(1, mapped.gameState().players().get(0).properties().get("Railroad").size());
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
