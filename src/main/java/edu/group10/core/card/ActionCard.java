package edu.group10.core.card;

import edu.group10.common.enums.CardType;
import edu.group10.common.model.Card;
import edu.group10.common.model.Command;

public class ActionCard extends Card {
    protected ActionCard(String cardId, String cardName, int cardValue) {
        super(cardId, cardName, CardType.ACTION, cardValue);
    }
}
