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
import org.junit.jupiter.api.Test;

/**
 * Tests for game setup: join, ready, start, reset.
 */
class GameEngineSetupTest {

    // ========== Join tests ==========

    @Test
    void addPlayerSuccessfully() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Alice", false));
        engine.addPlayer(new Player("p2", "Bob", false));
        assertEquals(2, engine.snapshot().players().size());
    }

    @Test
    void addDuplicateIdThrows() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Alice", false));
        assertThrows(IllegalArgumentException.class, () ->
                engine.addPlayer(new Player("p1", "Alice2", false)));
    }

    @Test
    void addDuplicateNameThrows() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Alice", false));
        assertThrows(IllegalArgumentException.class, () ->
                engine.addPlayer(new Player("p2", "Alice", false)));
    }

    @Test
    void addBotPlayer() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Human", false));
        engine.addPlayer(new Player("p2", "Bot1", true));
        assertEquals(2, engine.snapshot().players().size());
        assertTrue(engine.playerState("p2").player().bot());
    }

    @Test
    void cannotAddPlayerAfterGameStarted() {
        GameEngine engine = TestHelpers.createStartedEngine();
        assertThrows(IllegalStateException.class, () ->
                engine.addPlayer(new Player("p3", "Late", false)));
    }

    @Test
    void firstPlayerBecomesHost() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "HostPlayer", false));
        engine.addPlayer(new Player("p2", "Guest", false));
        // Host is the first player who starts the game
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        assertDoesNotThrow(() -> engine.startGame("p1"));
    }

    // ========== Ready/Unready tests ==========

    @Test
    void setReadySuccessfully() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Alice", false));
        engine.setReady("p1", true);
        assertTrue(engine.snapshot().readyPlayerIds().contains("p1"));
    }

    @Test
    void setUnreadySuccessfully() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Alice", false));
        engine.setReady("p1", true);
        engine.setReady("p1", false);
        assertFalse(engine.snapshot().readyPlayerIds().contains("p1"));
    }

    @Test
    void readyForUnknownPlayerThrows() {
        GameEngine engine = new GameEngine(new DeckService());
        assertThrows(IllegalArgumentException.class, () ->
                engine.setReady("unknown", true));
    }

    // ========== Start game tests ==========

    @Test
    void onlyHostCanStartGame() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Host", false));
        engine.addPlayer(new Player("p2", "Guest", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        assertThrows(IllegalStateException.class, () -> engine.startGame("p2"));
    }

    @Test
    void needAtLeastTwoPlayers() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Solo", false));
        engine.setReady("p1", true);
        assertThrows(IllegalStateException.class, () -> engine.startGame("p1"));
    }

    @Test
    void allNonBotPlayersMustBeReady() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Host", false));
        engine.addPlayer(new Player("p2", "Guest", false));
        engine.setReady("p1", true);
        // p2 not ready
        assertThrows(IllegalStateException.class, () -> engine.startGame("p1"));
    }

    @Test
    void botPlayersNotRequiredToBeReady() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Host", false));
        engine.addPlayer(new Player("p2", "Bot1", true));
        engine.setReady("p1", true);
        // Bot doesn't need to be ready
        assertDoesNotThrow(() -> engine.startGame("p1"));
    }

    @Test
    void startGameDealsFiveCardsToEachPlayer() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Host", false));
        engine.addPlayer(new Player("p2", "Guest", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.startGame("p1");

        // First player gets 5 start cards + 2 turn-start draw = 7
        assertEquals(7, engine.playerState("p1").hand().size());
        // Second player gets 5 start cards
        assertEquals(5, engine.playerState("p2").hand().size());
    }

    @Test
    void cannotStartAlreadyStartedGame() {
        GameEngine engine = TestHelpers.createStartedEngine();
        assertThrows(IllegalStateException.class, () -> engine.startGame("p1"));
    }

    @Test
    void deckHasCorrectSize() {
        DeckService deckService = new DeckService();
        List<edu.group10.monopolydeal.backend.model.card.Card> deck = deckService.createDeck();
        assertEquals(109, deck.size(), "Deck must have exactly 109 cards");
    }

    // ========== Reset tests ==========

    @Test
    void resetClearsAllState() {
        GameEngine engine = TestHelpers.createStartedEngine();
        engine.resetGame();
        assertFalse(engine.snapshot().started());
        assertEquals(0, engine.snapshot().players().size());
    }

    @Test
    void afterResetCanRejoinAndStart() {
        GameEngine engine = TestHelpers.createStartedEngine();
        engine.resetGame();
        engine.addPlayer(new Player("p1", "Alice", false));
        engine.addPlayer(new Player("p2", "Bob", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        assertDoesNotThrow(() -> engine.startGame("p1"));
        assertTrue(engine.snapshot().started());
    }

    @Test
    void resetWhenNotStartedWorks() {
        GameEngine engine = new GameEngine(new DeckService());
        assertDoesNotThrow(() -> engine.resetGame());
    }

    // ========== State snapshot tests ==========

    @Test
    void snapshotBeforeStartHasCorrectState() {
        GameEngine engine = new GameEngine(new DeckService());
        engine.addPlayer(new Player("p1", "Alice", false));
        engine.addPlayer(new Player("p2", "Bob", false));
        engine.setReady("p1", true);
        var state = engine.snapshot();
        assertFalse(state.started());
        assertFalse(state.gameOver());
        assertTrue(state.readyPlayerIds().contains("p1"));
        assertFalse(state.readyPlayerIds().contains("p2"));
    }

    @Test
    void snapshotAfterStartHasCurrentPlayer() {
        GameEngine engine = TestHelpers.createStartedEngine();
        var state = engine.snapshot();
        assertTrue(state.started());
        assertEquals("p1", state.currentPlayerId());
    }
}
