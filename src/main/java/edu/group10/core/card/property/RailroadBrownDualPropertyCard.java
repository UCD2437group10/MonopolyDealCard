package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class RailroadBrownDualPropertyCard extends PropertyCard {
    public RailroadBrownDualPropertyCard() {
        super("PROP_DUAL_RAILROAD_BROWN", "Railroad/Brown Dual Property", 1);
    }
    
    @Override
    public int getSetProgressValue(PropertyColor color, int cardCountInSet) {
        if (color == null) {
            return 0;
        }
        return switch (color) {
            case BROWN -> switch (cardCountInSet) {
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
        return new PropertyColor[]{PropertyColor.RAILROAD, PropertyColor.BROWN};
    }
}
