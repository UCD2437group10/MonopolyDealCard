package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class YellowPropertyCard extends PropertyCard {
    public YellowPropertyCard() {
        super("PROP_YELLOW", "Yellow Property", 3);
    }
    
    @Override
    public int getSetProgressValue(int cardCountInSet) {
        return switch (cardCountInSet) {
            case 1, 2 -> 3;
            case 3 -> 6;
            default -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{PropertyColor.YELLOW};
    }
}
