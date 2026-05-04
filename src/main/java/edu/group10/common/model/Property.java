package edu.group10.common.model;

import edu.group10.common.enums.CardType;
import edu.group10.common.enums.PropertyColor;

public class Property extends Card{
    private PropertyColor primaryColor;
    private PropertyColor secondaryColor; //For dual-color property cards
    private int rent;
    private int setSize; //Amount of cards needed to form a set
    private boolean hasHouse;
    private boolean hasHotel;

    public Property() {
        super.setCardType(CardType.PROPERTY);
    }

    public Property(String cardId, String cardName, PropertyColor primaryColor,
                    PropertyColor secondaryColor, int rent, int setSize) {
        super(cardId, cardName, CardType.PROPERTY, 0);
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.rent = rent;
        this.setSize = setSize;
        this.hasHouse = false;
        this.hasHotel = false;
    }

    //Decide if the card is dual-color
    public boolean isDualColor() {
        return secondaryColor != null;
    }

    public PropertyColor getCurrentColor() {
        return primaryColor;
    }
    public void setCurrentColor(PropertyColor primaryColor) {}

    //Switch color for wild property cards
    public void switchColor(PropertyColor newColor) {
        this.primaryColor = newColor;
    }

    //Getters and setters
    public PropertyColor getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(PropertyColor primaryColor) { this.primaryColor = primaryColor; }

    public PropertyColor getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(PropertyColor secondaryColor) { this.secondaryColor = secondaryColor; }

    public int getRent() { return rent; }
    public void setRent(int rent) { this.rent = rent; }

    public int getSetSize() { return setSize; }
    public void setSetSize(int setSize) { this.setSize = setSize; }

    public boolean isHasHouse() { return hasHouse; }
    public void setHasHouse(boolean hasHouse) { this.hasHouse = hasHouse; }

    public boolean isHasHotel() { return hasHotel; }
    public void setHasHotel(boolean hasHotel) { this.hasHotel = hasHotel; }

    //calculate rent of property containing houses/hotels
    public int getRentWithModifiers() {
        int finalRent = rent;
        if (hasHouse) finalRent += 3;
        if (hasHotel) finalRent += 4;
        return finalRent;
    }

    @Override
    public String toString() {
        return "Property{" +
                "cardId='" + getCardId() + '\'' +
                ", name='" + getCardName() + '\'' +
                ", primaryColor=" + primaryColor +
                ", secondaryColor=" + secondaryColor +
                ", rent=" + rent +
                ", setSize=" + setSize +
                ", hasHouse=" + hasHouse +
                ", hasHotel=" + hasHotel +
                '}';
    }
}
