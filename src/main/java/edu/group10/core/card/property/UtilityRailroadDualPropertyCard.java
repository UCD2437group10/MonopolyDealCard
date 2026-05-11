package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class UtilityRailroadDualPropertyCard extends PropertyCard {
    public UtilityRailroadDualPropertyCard() {
        super("PROP_DUAL_UTILITY_RAILROAD", "Utility/Railroad Dual Property", 2);
    }
    
    @Override
    public int getSetProgressValue(PropertyColor color, int cardCountInSet) {
        if (color == null) {
            return 0;
        }
        return switch (color) {
            case RAILROAD, UTILITY -> switch (cardCountInSet) {
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
        return new PropertyColor[]{PropertyColor.UTILITY, PropertyColor.RAILROAD};
    }
}
