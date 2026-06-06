package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class PinkOrangeDualPropertyCard extends PropertyCard {
    public PinkOrangeDualPropertyCard() {
        super("PROP_DUAL_PINK_ORANGE", "Pink/Orange Dual Property", 2);
    }
    
    @Override
    public int getSetProgressValue(PropertyColor color, int cardCountInSet) {
        if (color == null) {
            return 0;
        }
        return switch (color) {
            case PINK, ORANGE -> switch (cardCountInSet) {
                case 1, 2 -> 2;
                case 3 -> 4;
                default -> 0;
            };
            default -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{PropertyColor.PINK, PropertyColor.ORANGE};
    }
}
