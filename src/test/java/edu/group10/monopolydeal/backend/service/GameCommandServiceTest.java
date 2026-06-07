package edu.group10.monopolydeal.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.group10.monopolydeal.backend.game.GameEngine;
import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.network.protocol.GameRequest;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameCommandServiceTest {

    @Test
    void joinReadyAndStartCommandsDriveLobbyFlow() {
        // This covers the main lobby flow used before a real match begins.
        GameCommandService service = new GameCommandService(new GameEngine(new FixedDeckService()));

        GameResponse join1 = service.handle(new GameRequest("JOIN", "p1", Map.of("name", "Alice")));
        GameResponse join2 = service.handle(new GameRequest("JOIN", "p2", Map.of("name", "Bob")));
        GameResponse ready1 = service.handle(new GameRequest("READY", "p1", Map.of()));
        GameResponse ready2 = service.handle(new GameRequest("READY", "p2", Map.of()));
        GameResponse start = service.handle(new GameRequest("START", "p1", Map.of()));

        assertTrue(join1.success());
        assertTrue(join2.success());
        assertTrue(ready1.gameState().readyPlayerIds().contains("p1"));
        assertTrue(ready2.gameState().readyPlayerIds().contains("p2"));
        assertTrue(start.success());
        assertTrue(start.gameState().started());
        assertEquals("p1", start.gameState().currentPlayerId());
        assertEquals(2, start.gameState().players().size());
    }

    @Test
    void drawCommandReturnsFriendlyFailureMessage() {
        // Manual draw is disabled because drawing is handled by the game flow.
        GameCommandService service = new GameCommandService(new GameEngine(new FixedDeckService()));

        GameResponse response = service.handle(new GameRequest("DRAW", "p1", Map.of()));

        assertFalse(response.success());
        assertEquals("manual draw is disabled; cards are auto-drawn at turn start or by action effects", response.message());
    }

    @Test
    void unknownActionReturnsFailureInsteadOfThrowing() {
        // Unsupported commands should fail safely at the service layer.
        GameCommandService service = new GameCommandService(new GameEngine(new FixedDeckService()));

        GameResponse response = service.handle(new GameRequest("FLY", "p1", Map.of()));

        assertFalse(response.success());
        assertEquals("unknown action: FLY", response.message());
    }

    private static final class FixedDeckService extends DeckService {
        @Override
        public List<Card> createDeck() {
            // Use a stable deck so these command tests stay deterministic.
            List<Card> deck = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                deck.add(new SimpleCard("1M Money", CardType.MONEY, "Yellow", 1));
            }
            return deck;
        }
    }
}
