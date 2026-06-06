package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class UtilityPropertyCard extends PropertyCard {
    public UtilityPropertyCard() {
        super("PROP_UTILITY", "Utility Property", 2);
    }
    
    @Override
    public int getSetProgressValue(int cardCountInSet) {
        return switch (cardCountInSet) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            default -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{PropertyColor.UTILITY};
    }
}
