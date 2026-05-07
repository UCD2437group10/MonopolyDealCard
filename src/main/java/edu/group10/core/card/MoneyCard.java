package edu.group10.core.card;

import edu.group10.common.enums.CardType;
import edu.group10.common.model.Card;

public class MoneyCard extends Card {
    protected MoneyCard(String cardId, String cardName, int cardValue) {
        super(cardId, cardName, CardType.MONEY, cardValue);
    }
}
