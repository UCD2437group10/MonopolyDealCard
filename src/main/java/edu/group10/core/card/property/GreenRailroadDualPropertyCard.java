package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class GreenRailroadDualPropertyCard extends PropertyCard {
    public GreenRailroadDualPropertyCard() {
        super("PROP_DUAL_GREEN_RAILROAD", "Green/Railroad Dual Property", 4);
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
        return new PropertyColor[]{PropertyColor.GREEN, PropertyColor.RAILROAD};
    }
}
