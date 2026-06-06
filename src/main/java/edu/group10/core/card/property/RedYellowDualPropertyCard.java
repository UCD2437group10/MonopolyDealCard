package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class RedYellowDualPropertyCard extends PropertyCard {
    public RedYellowDualPropertyCard() {
        super("PROP_DUAL_RED_YELLOW", "Red/Yellow Dual Property", 3);
    }
    
    @Override
    public int getSetProgressValue(PropertyColor color, int cardCountInSet) {
        if (color == null) {
            return 0;
        }
        return switch (color) {
            case RED, YELLOW -> switch (cardCountInSet) {
                case 1, 2 -> 3;
                case 3 -> 6;
                default -> 0;
            };
            default -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{PropertyColor.RED, PropertyColor.YELLOW};
    }
}
