package edu.group10.core.card.property;

import edu.group10.core.card.PropertyCard;
import edu.group10.common.enums.PropertyColor;

public class WildPropertyCard extends PropertyCard {
    public WildPropertyCard() {
        super("PROP_WILD", "Wild Property", 0);
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
            case PINK, ORANGE -> switch (cardCountInSet) {
                case 1, 2 -> 2;
                case 3 -> 4;
                default -> 0;
            };
            case RED, YELLOW -> switch (cardCountInSet) {
                case 1, 2 -> 3;
                case 3 -> 6;
                default -> 0;
            };
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
            case RAILROAD, UTILITY -> switch (cardCountInSet) {
                case 1 -> 1;
                case 2 -> 2;
                case 3 -> 3;
                case 4 -> 4;
                default -> 0;
            };
            case WILD -> 0;
        };
    }
    
    public PropertyColor[] getColours() {
        return new PropertyColor[]{
                PropertyColor.BROWN,
                PropertyColor.LIGHT_BLUE,
                PropertyColor.PINK,
                PropertyColor.ORANGE,
                PropertyColor.RED,
                PropertyColor.YELLOW,
                PropertyColor.GREEN,
                PropertyColor.DARK_BLUE,
                PropertyColor.RAILROAD,
                PropertyColor.UTILITY
        };
    }
}
