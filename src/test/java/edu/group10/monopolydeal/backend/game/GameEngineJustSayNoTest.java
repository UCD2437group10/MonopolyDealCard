package edu.group10.monopolydeal.backend.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.DeckService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameEngineJustSayNoTest {

    @Test
    void justSayNoCancelsDebtCollectorEffect() {
        GameEngine engine = createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        replaceHand(p1, card("Debt Collector", CardType.ACTION, "-", 3));
        replaceHand(p2, card("Just Say No", CardType.ACTION, "-", 4));
        p2.addToBank(card("5M Money", CardType.MONEY, "Purple", 5));

        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));
        assertEquals("p2", engine.snapshot().jsnResponderPlayerId());

        engine.respondJustSayNo("p2", true);

        assertEquals("", engine.snapshot().jsnResponderPlayerId());
        assertEquals(0, p1.bankTotal());
        assertEquals(5, p2.bankTotal());
        assertEquals(0, p2.hand().size());
    }

    @Test
    void debtCollectorAppliesWhenTargetDoesNotUseJustSayNo() {
        GameEngine engine = createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        replaceHand(p1, card("Debt Collector", CardType.ACTION, "-", 3));
        replaceHand(p2, card("Pass Go", CardType.ACTION, "-", 1));
        p2.addToBank(card("5M Money", CardType.MONEY, "Purple", 5));

        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));

        assertEquals("", engine.snapshot().jsnResponderPlayerId());
        assertEquals("p2", engine.snapshot().pendingPaymentPayerPlayerId());

        Map<String, List<Integer>> properties = new LinkedHashMap<>();
        engine.submitPendingPayment("p2", new PaymentSelection(List.of(0), properties));

        assertEquals("", engine.snapshot().pendingPaymentPayerPlayerId());
        assertEquals(5, p1.bankTotal());
        assertEquals(0, p2.bankTotal());
        assertTrue(p2.bank().isEmpty());
    }

    @Test
    void justSayNoCancelsSlyDealPropertyTransfer() {
        GameEngine engine = createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        replaceHand(p1, card("Sly Deal", CardType.ACTION, "-", 3));
        replaceHand(p2, card("Just Say No", CardType.ACTION, "-", 4));
        p2.addPropertyToExactGroup("Brown", card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));

        engine.playActionCard("p1", 0, Map.of(
                "targetPlayerId", "p2",
                "color", "Brown",
                "propertyIndex", "0"
        ));
        assertEquals("p2", engine.snapshot().jsnResponderPlayerId());

        engine.respondJustSayNo("p2", true);

        assertEquals("", engine.snapshot().jsnResponderPlayerId());
        assertEquals(0, p1.propertyCount("Brown"));
        assertEquals(1, p2.propertyCount("Brown"));
        assertEquals(0, p2.hand().size());
    }

    @Test
    void slyDealAppliesOnceAfterJustSayNoCounterChain() {
        GameEngine engine = createStartedEngine();
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        replaceHand(
                p1,
                card("Sly Deal", CardType.ACTION, "-", 3),
                card("Just Say No", CardType.ACTION, "-", 4)
        );
        replaceHand(p2, card("Just Say No", CardType.ACTION, "-", 4));
        p2.addPropertyToExactGroup("Brown", card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));

        engine.playActionCard("p1", 0, Map.of(
                "targetPlayerId", "p2",
                "color", "Brown",
                "propertyIndex", "0"
        ));
        assertEquals("p2", engine.snapshot().jsnResponderPlayerId());

        engine.respondJustSayNo("p2", true);
        assertEquals("p1", engine.snapshot().jsnResponderPlayerId());

        engine.respondJustSayNo("p1", true);

        assertEquals("", engine.snapshot().jsnResponderPlayerId());
        assertEquals(1, p1.propertyCount("Brown"));
        assertEquals(0, p2.propertyCount("Brown"));
        assertEquals(0, p1.hand().size());
        assertEquals(0, p2.hand().size());
    }

    private int propertyCount(PlayerState playerState, String color) {
        return playerState.properties().getOrDefault(color, List.of()).size();
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
