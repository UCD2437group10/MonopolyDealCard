package edu.group10.monopolydeal.backend.service;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;

/**
 * 卡牌转化为金钱规则（基于 docs/cards.md）。
 */
public final class CardMoneyRules {

    private CardMoneyRules() {
    }

    public static boolean canBank(Card card) {
        return card.type() == CardType.MONEY || card.type() == CardType.ACTION || card.type() == CardType.RENT;
    }

    public static int bankValue(Card card) {
        if (!canBank(card)) {
            return 0;
        }
        return card.bankValue();
    }

    public static void validateBankable(Card card) {
        if (!canBank(card)) {
            throw new IllegalStateException("property and multi-property cannot be placed into bank");
        }
        if (card.bankValue() <= 0) {
            throw new IllegalStateException("bankable card must have positive bank value");
        }
    }
}
