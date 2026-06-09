package edu.group10.monopolydeal;

import edu.group10.monopolydeal.backend.game.GameEngine;
import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.DeckService;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared test helpers for all game engine tests.
 */
public final class TestHelpers {

    private TestHelpers() {}

    public static Card card(String name, CardType type, String color, int value) {
        return new SimpleCard(name, type, color, value);
    }

    public static void replaceHand(PlayerState playerState, Card... cards) {
        while (!playerState.hand().isEmpty()) {
            playerState.removeHandCard(playerState.hand().size() - 1);
        }
        for (Card card : cards) {
            playerState.addToHand(card);
        }
    }

    public static GameEngine createStartedEngine(String p1Name, String p2Name) {
        GameEngine engine = new GameEngine(new FixedDeckService());
        engine.addPlayer(new Player("p1", p1Name, false));
        engine.addPlayer(new Player("p2", p2Name, false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.startGame("p1");
        return engine;
    }

    public static GameEngine createStartedEngine() {
        return createStartedEngine("Player1", "Player2");
    }

    /**
     * Fixed deck for deterministic testing: 20 Pass Go cards.
     */
    public static final class FixedDeckService extends DeckService {
        @Override
        public List<Card> createDeck() {
            List<Card> deck = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                deck.add(new SimpleCard("Pass Go", CardType.ACTION, "-", 1));
            }
            return deck;
        }
    }

    /**
     * Money-only deck for payment tests.
     */
    public static final class MoneyDeckService extends DeckService {
        @Override
        public List<Card> createDeck() {
            List<Card> deck = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                deck.add(new SimpleCard("1M Money", CardType.MONEY, "Yellow", 1));
            }
            return deck;
        }
    }

    /**
     * Starts an engine with 3 players.
     */
    public static GameEngine createStartedEngine3P() {
        GameEngine engine = new GameEngine(new FixedDeckService());
        engine.addPlayer(new Player("p1", "Player1", false));
        engine.addPlayer(new Player("p2", "Player2", false));
        engine.addPlayer(new Player("p3", "Player3", false));
        engine.setReady("p1", true);
        engine.setReady("p2", true);
        engine.setReady("p3", true);
        engine.startGame("p1");
        return engine;
    }
}
