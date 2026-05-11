package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class GreenPropertyCard extends PropertyCard {
    public GreenPropertyCard() {
        super("PROP_GREEN", "Green Property", 4);
    }
    
    @Override
    public int getSetProgressValue(int cardCountInSet) {
        return switch (cardCountInSet) {
            case 1, 2 -> 4;
            case 3 -> 8;
            default -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{PropertyColor.GREEN};
    }
}
