package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class RailroadLightBlueDualPropertyCard extends PropertyCard {
    public RailroadLightBlueDualPropertyCard() {
        super("PROP_DUAL_RAILROAD_LIGHT_BLUE", "Railroad/Light Blue Dual Property", 4);
    }
    
    @Override
    public int getSetProgressValue(PropertyColor color, int cardCountInSet) {
        if (color == null) {
            return 0;
        }
        return switch (color) {
            case LIGHT_BLUE -> switch (cardCountInSet) {
                case 1 -> 1;
                case 2, 3 -> 2;
                default -> 0;
            };
            case RAILROAD -> switch (cardCountInSet) {
                case 1 -> 1;
                case 2 -> 2;
                case 3 -> 3;
                case 4 -> 4;
                default -> 0;
            };
            default -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{PropertyColor.RAILROAD, PropertyColor.LIGHT_BLUE};
    }
}
