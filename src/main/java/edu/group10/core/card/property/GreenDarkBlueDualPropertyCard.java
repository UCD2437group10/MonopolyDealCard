package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class GreenDarkBlueDualPropertyCard extends PropertyCard {
    public GreenDarkBlueDualPropertyCard() {
        super("PROP_DUAL_GREEN_DARK_BLUE", "Green/Dark Blue Dual Property", 4);
    }
    
    @Override
    public int getSetProgressValue(PropertyColor color, int cardCountInSet) {
        if (color == null) {
            return 0;
        }
        return switch (color) {
            case GREEN -> switch (cardCountInSet) {
                case 1, 2 -> 4;
                case 3 -> 8;
                default -> 0;
            };
            case DARK_BLUE -> switch (cardCountInSet) {
                case 1 -> 4;
                case 2, 3 -> 8;
                default -> 0;
            };
            default -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{PropertyColor.GREEN, PropertyColor.DARK_BLUE};
    }
}
