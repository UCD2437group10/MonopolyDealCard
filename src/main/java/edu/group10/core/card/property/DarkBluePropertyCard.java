package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class DarkBluePropertyCard extends PropertyCard {
    public DarkBluePropertyCard() {
        super("PROP_DARK_BLUE", "Dark Blue Property", 4);
    }
    
    @Override
    public int getSetProgressValue(int cardCountInSet) {
        return switch (cardCountInSet) {
            case 1 -> 4;
            case 2, 3 -> 8;
            default -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{PropertyColor.DARK_BLUE};
    }
}
