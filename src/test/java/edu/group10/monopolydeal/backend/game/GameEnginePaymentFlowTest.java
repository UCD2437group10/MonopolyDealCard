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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameEnginePaymentFlowTest {

    @Test
    void debtCollectorWaitsForHumanManualPayment() {
        GameEngine engine = createStartedEngine(false);
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        replaceHand(p1, card("Debt Collector", CardType.ACTION, "-", 3));
        p2.addToBank(card("4M Money", CardType.MONEY, "Blue", 4));
        p2.addPropertyToExactGroup("Brown", card("Baltic Avenue", CardType.PROPERTY, "Brown", 1));

        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));

        assertEquals("p2", engine.snapshot().pendingPaymentPayerPlayerId());
        assertEquals(0, p1.bankTotal());

        Map<String, List<Integer>> properties = new LinkedHashMap<>();
        properties.put("Brown", List.of(0));
        engine.submitPendingPayment("p2", new PaymentSelection(List.of(0), properties));

        assertEquals("", engine.snapshot().pendingPaymentPayerPlayerId());
        assertEquals(4, p1.bankTotal());
        assertEquals(1, p1.propertyCount("Brown"));
        assertEquals(0, p2.bankTotal());
        assertEquals(0, p2.propertyCount("Brown"));
    }

    @Test
    void manualPaymentRejectsInsufficientSubsetWhenMoreAssetsRemain() {
        GameEngine engine = createStartedEngine(false);
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        replaceHand(p1, card("Debt Collector", CardType.ACTION, "-", 3));
        p2.addToBank(card("3M Money", CardType.MONEY, "Green", 3));
        p2.addPropertyToExactGroup("Brown", card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        p2.addPropertyToExactGroup("Brown", card("Baltic Avenue", CardType.PROPERTY, "Brown", 1));

        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> engine.submitPendingPayment("p2", new PaymentSelection(List.of(0), Map.of()))
        );

        assertEquals("selected assets do not cover the required amount", exception.getMessage());
        assertEquals("p2", engine.snapshot().pendingPaymentPayerPlayerId());
    }

    @Test
    void selectingAllAssetsAllowsUnderpaymentWhenPlayerCannotAffordFullAmount() {
        GameEngine engine = createStartedEngine(false);
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        replaceHand(p1, card("Debt Collector", CardType.ACTION, "-", 3));
        p2.addToBank(card("2M Money", CardType.MONEY, "Orange", 2));
        p2.addPropertyToExactGroup("Brown", card("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));

        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));

        Map<String, List<Integer>> properties = new LinkedHashMap<>();
        properties.put("Brown", List.of(0));
        engine.submitPendingPayment("p2", new PaymentSelection(List.of(0), properties));

        assertEquals("", engine.snapshot().pendingPaymentPayerPlayerId());
        assertEquals(2, p1.bankTotal());
        assertEquals(1, p1.propertyCount("Brown"));
        assertEquals(0, p2.bankTotal());
        assertEquals(0, p2.propertyCount("Brown"));
    }

    @Test
    void botPaymentRemainsAutomatic() {
        GameEngine engine = createStartedEngine(true);
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        replaceHand(p1, card("Debt Collector", CardType.ACTION, "-", 3));
        p2.addToBank(card("5M Money", CardType.MONEY, "Purple", 5));

        engine.playActionCard("p1", 0, Map.of("targetPlayerId", "p2"));

        assertEquals("", engine.snapshot().pendingPaymentPayerPlayerId());
        assertEquals(5, p1.bankTotal());
        assertEquals(0, p2.bankTotal());
    }

    @Test
    void wildPropertyCountsAsZeroDuringManualPayment() {
        GameEngine engine = createStartedEngine(false);
        PlayerState p1 = engine.playerState("p1");
        PlayerState p2 = engine.playerState("p2");

        replaceHand(p1, card("It's My Birthday", CardType.ACTION, "-", 2));
        p2.addPropertyToExactGroup("Brown", card("Wild Property", CardType.MULTI_PROPERTY, "Wild", 0));
        p2.addPropertyToExactGroup("Brown", card("Baltic Avenue", CardType.PROPERTY, "Brown", 1));

        engine.playActionCard("p1", 0, Map.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> engine.submitPendingPayment("p2", new PaymentSelection(List.of(), Map.of("Brown", List.of(0))))
        );

        assertEquals("selected assets do not cover the required amount", exception.getMessage());

        engine.submitPendingPayment("p2", new PaymentSelection(List.of(), Map.of("Brown", List.of(0, 1))));
        assertEquals("", engine.snapshot().pendingPaymentPayerPlayerId());
        assertEquals(2, p1.propertyCount("Brown"));
        assertEquals(0, p2.propertyCount("Brown"));
    }

    private GameEngine createStartedEngine(boolean secondPlayerBot) {
        GameEngine engine = new GameEngine(new FixedDeckService());
        engine.addPlayer(new Player("p1", "Player1", false));
        engine.addPlayer(new Player("p2", "Player2", secondPlayerBot));
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
