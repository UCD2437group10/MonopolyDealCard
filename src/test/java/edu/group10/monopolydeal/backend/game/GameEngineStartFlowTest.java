package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.service.DeckService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameEngineStartFlowTest {

    @Test
    void startGameRequiresAllNonBotPlayersReady() {
        // A multiplayer game should not start until every human player is ready.
        GameEngine engine = new GameEngine(new FixedDeckService());
        engine.addPlayer(new Player("p1", "Player1", false));
        engine.addPlayer(new Player("p2", "Player2", false));
        engine.setReady("p1", true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> engine.startGame("p1")
        );

        assertEquals("all non-bot players must be ready", exception.getMessage());
    }

    @Test
    void gameStartDealsOpeningHandsAndTurnDraw() {
        // The first player gets the normal start-of-turn draw after opening hands.
        GameEngine engine = new GameEngine(new FixedDeckService());
        engine.addPlayer(new Player("p1", "Player1", false));
        engine.addPlayer(new Player("p2", "Player2", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);

        engine.startGame("p1");

        GameState snapshot = engine.snapshot();
        assertTrue(snapshot.started());
        assertEquals("p1", snapshot.currentPlayerId());
        assertEquals(7, engine.playerState("p1").hand().size());
        assertEquals(5, engine.playerState("p2").hand().size());
        assertEquals(18, snapshot.drawPileCount());
    }

    @Test
    void onlyHostCanStartGame() {
        // The host controls the transition from lobby to match.
        GameEngine engine = new GameEngine(new FixedDeckService());
        engine.addPlayer(new Player("p1", "Player1", false));
        engine.addPlayer(new Player("p2", "Player2", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> engine.startGame("p2")
        );

        assertEquals("only host can start game", exception.getMessage());
    }

    private static final class FixedDeckService extends DeckService {
        @Override
        public List<Card> createDeck() {
            // Keep the deck simple so hand-size assertions are easy to verify.
            List<Card> deck = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                deck.add(new SimpleCard("1M Money", CardType.MONEY, "Yellow", 1));
            }
            return deck;
        }
    }
}
