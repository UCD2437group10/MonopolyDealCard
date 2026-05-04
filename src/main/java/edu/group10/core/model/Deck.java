package edu.group10.core.model;

import edu.group10.common.model.Card;

import java.util.*;

public class Deck {
    private Stack<Card> cards;

    public Deck() {
        this.cards = new Stack<>();
    }

    public void initialize(List<Card> cardList) {
        this.cards.clear();
        this.cards.addAll(cardList);
        shuffle();
    }

    public void shuffle() {
        List<Card> cardList = new ArrayList<>(cards);
        Collections.shuffle(cardList);
        this.cards.clear();
        this.cards.addAll(cardList);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.pop();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public void addAll(List<Card> newCards) {
        cards.addAll(newCards);
    }

    public void addCard(Card card) {
        cards.push(card);
    }

    //Being used to reshuffle from the discard pile
    public void reshuffleFromDiscard(List<Card> discardCards) {
        cards.clear();
        cards.addAll(discardCards);
        shuffle();
    }

    // Getters
    public Stack<Card> getCards() { return cards; }
}
