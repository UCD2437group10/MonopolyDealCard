package edu.group10.core.model;

import edu.group10.common.model.Card;

import java.util.ArrayList;
import java.util.List;

public class DiscardPile {
    private List<Card> cards;

    public DiscardPile() {
        this.cards = new ArrayList<>();
    }

    public void add(Card card) {
        if (card != null) {
            cards.add(card);
        }
    }

    public void addAll(List<Card> newCards) {
        cards.addAll(newCards);
    }

    public List<Card> takeAll() {
        List<Card> taken = new ArrayList<>(cards);
        cards.clear();
        return taken;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public List<Card> getCards() { return cards; }
}
