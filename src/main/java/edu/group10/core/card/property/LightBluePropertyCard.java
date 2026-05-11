package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class LightBluePropertyCard extends PropertyCard {
    public LightBluePropertyCard() {
        super("PROP_LIGHT_BLUE", "Light Blue Property", 1);
    }
    
    @Override
    public int getSetProgressValue(int cardCountInSet) {
        return switch (cardCountInSet) {
            case 1 -> 1;
            case 2, 3 -> 2;
            default -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{PropertyColor.LIGHT_BLUE};
    }
}
