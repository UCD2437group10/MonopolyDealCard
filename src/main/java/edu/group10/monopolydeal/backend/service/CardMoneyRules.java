package edu.group10.monopolydeal.backend.service;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;

/**
 * Centralizes the rules for treating cards as money.
 */
public final class CardMoneyRules {

    private CardMoneyRules() {
    }

    /** Returns whether the card may be placed in a bank. */
    public static boolean canBank(Card card) {
        return card.type() == CardType.MONEY || card.type() == CardType.ACTION || card.type() == CardType.RENT;
    }

    /** Returns the money value contributed by a bankable card. */
    public static int bankValue(Card card) {
        if (!canBank(card)) {
            return 0;
        }
        return card.bankValue();
    }

    /** Throws when the card cannot legally be banked. */
    public static void validateBankable(Card card) {
        if (!canBank(card)) {
            throw new IllegalStateException("property and multi-property cannot be placed into bank");
        }
        if (card.bankValue() <= 0) {
            throw new IllegalStateException("bankable card must have positive bank value");
        }
    }
}
