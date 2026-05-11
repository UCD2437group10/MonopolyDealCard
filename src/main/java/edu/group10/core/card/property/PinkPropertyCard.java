package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class PinkPropertyCard extends PropertyCard {
    public PinkPropertyCard() {
        super("PROP_PINK", "Pink Property", 2);
    }
    
    @Override
    public int getSetProgressValue(int cardCountInSet) {
        return switch (cardCountInSet) {
            case 1, 2 -> 2;
            case 3 -> 4;
            default -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{PropertyColor.PINK};
    }
}
