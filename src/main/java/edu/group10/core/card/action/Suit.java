package edu.group10.core.card.action;

import edu.group10.common.enums.PropertyColor;
import edu.group10.core.card.PropertyCard;

import java.util.ArrayList;
import java.util.Collection;

public class Suit {
    private ArrayList<PropertyCard> suit = new ArrayList<>();
    private PropertyColor colour;
    private int extraValue = 0;

    public Suit() {}

    public Suit(PropertyColor colour, int rent) {
        this.colour = colour;
        this.extraValue = rent;
    }

    public boolean addCard(PropertyCard card, int chooseColour) {
        try {
            if (suit.isEmpty()) {
                colour = card.getColours()[chooseColour];
                suit.add(card);
                return true;
            } else {
                for (PropertyColor c : card.getColours()) {
                    if (c.equals(colour)) {
                        suit.add(card);
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("not dual colour card");
            return false;
        }
    }

    public int returnMoney() {
        if (suit.isEmpty() && extraValue > 0) {
            return extraValue;
        }

        if (suit.isEmpty()) return 0;
        if (suit.get(0).getColours().length != 1) {
            return suit.get(0).getSetProgressValue(colour, suit.size()) + extraValue;
        }
        else {
            return suit.get(0).getSetProgressValue(suit.size()) + extraValue;
        }
    }

    /**
     * @return the card in the suit can remove
     */
    public Collection<PropertyCard> returnCards() {
        return suit;
    }

    public PropertyColor getColour() {return colour;}

    public void setExtraValue(int value) {
        extraValue += value;
    }
}
