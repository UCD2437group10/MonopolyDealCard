package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.DeckService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameEngineStatePollingTest {

    @Test
    void pollingSnapshotDoesNotThrowWhenTimedOutForcedDealPayloadHasGoneStale() throws Exception {
        GameEngine engine = createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        replaceHand(p1, card("Forced Deal", CardType.ACTION, "-", 3));
        replaceHand(p2, card("Just Say No", CardType.ACTION, "-", 4));

        p1.addPropertyToExactGroup("Brown", card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p2.addPropertyToExactGroup("Railroad", card("Reading Railroad", CardType.PROPERTY, "Railroad", 0));

        engine.playActionCard("p1", 0, Map.of(
                "targetPlayerId", "p2",
                "myColor", "Brown",
                "myIndex", "0",
                "targetColor", "Railroad",
                "targetIndex", "0"
        ));

        p2.removeProperty("Railroad", 0);
        expirePendingJsn(engine);

        GameState state = assertDoesNotThrow(engine::pollStateSnapshot);
        assertNotNull(state);
        assertEquals("", state.jsnResponderPlayerId());
    }

    private void expirePendingJsn(GameEngine engine) throws Exception {
        Field pendingJsnField = GameEngine.class.getDeclaredField("pendingJsn");
        pendingJsnField.setAccessible(true);
        PendingJsnState pending = (PendingJsnState) pendingJsnField.get(engine);
        pending.setWaitingSinceMs(System.currentTimeMillis() - 20_000L);
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
