package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class BrownLightBlueDualPropertyCard extends PropertyCard {
    public BrownLightBlueDualPropertyCard() {
        super("PROP_DUAL_BROWN_LIGHT_BLUE", "Brown/Light Blue Dual Property", 1);
    }
    
    @Override
    public int getSetProgressValue(PropertyColor color, int cardCountInSet) {
        if (color == null) {
            return 0;
        }
        return switch (color) {
            case BROWN, LIGHT_BLUE -> switch (cardCountInSet) {
                case 1 -> 1;
                case 2, 3 -> 2;
                default -> 0;
            };
            default -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{PropertyColor.BROWN, PropertyColor.LIGHT_BLUE};
    }
}
