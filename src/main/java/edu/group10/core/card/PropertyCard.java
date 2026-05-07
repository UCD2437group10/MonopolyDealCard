package edu.group10.core.card;

import edu.group10.common.enums.CardType;
import edu.group10.common.model.Card;

public class PropertyCard extends Card {
    protected PropertyCard(String cardId, String cardName, int cardValue) {
        super(cardId, cardName, CardType.PROPERTY, cardValue);
    }
}
