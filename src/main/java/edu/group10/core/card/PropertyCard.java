package edu.group10.core.card;

import edu.group10.common.enums.CardType;
import edu.group10.common.enums.PropertyColor;
import edu.group10.common.model.Card;

public class PropertyCard extends Card {
    protected PropertyCard(String cardId, String cardName, int cardValue) {
        super(cardId, cardName, CardType.PROPERTY, cardValue);
    }

    public int getSetProgressValue(int cardCountInSet) {
        return 0;
    }

    public int getSetProgressValue(PropertyColor color, int cardCountInSet) {
        return getSetProgressValue(cardCountInSet);
    }

    public PropertyColor[] getColours() {
        return new PropertyColor[0];
    }
}
