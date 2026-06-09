package edu.group10.monopolydeal.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.group10.monopolydeal.backend.model.card.Card;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeckServiceCardValueTest {

    @Test
    void propertyCardsUsePrintedValuesFromLocalCardAssets() {
        List<Card> deck = new DeckService().createDeck().stream()
                .sorted(Comparator.comparing(Card::name).thenComparing(Card::color))
                .toList();

        assertEquals(1, valueOf(deck, "Mediterranean Avenue"));
        assertEquals(4, valueOf(deck, "Boardwalk"));
        assertEquals(2, valueOf(deck, "Reading Railroad"));
        assertEquals(2, valueOf(deck, "Electric Company"));
        assertEquals(1, valueOf(deck, "Light Blue/Brown Multi"));
        assertEquals(4, valueOf(deck, "Light Blue/Railroad Multi"));
        assertEquals(2, valueOf(deck, "Pink/Orange Multi"));
        assertEquals(3, valueOf(deck, "Red/Yellow Multi"));
        assertEquals(4, valueOf(deck, "Deep Blue/Green Multi"));
        assertEquals(4, valueOf(deck, "Green/Railroad Multi"));
        assertEquals(2, valueOf(deck, "Railroad/Utility Multi"));
        assertEquals(0, valueOf(deck, "Wild Property"));
    }

    private int valueOf(List<Card> deck, String cardName) {
        return deck.stream()
                .filter(card -> card.name().equals(cardName))
                .findFirst()
                .orElseThrow()
                .bankValue();
    }
}
