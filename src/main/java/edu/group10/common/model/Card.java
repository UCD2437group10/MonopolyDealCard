package edu.group10.common.model;

import edu.group10.common.enums.CardType;

public class Card {
    private String cardId; //Unique sign of the card
    private String cardName;
    private CardType cardType;
    private int cardValue;

    public Card() {}

    public Card(String cardID, String cardName, CardType cardType, int cardValue) {
        this.cardId = cardID;
        this.cardName = cardName;
        this.cardType = cardType;
        this.cardValue = cardValue;
    }

    public String getCardId() {return cardId;}
    public void setCardId(String cardId) {this.cardId = cardId;}

    public String getCardName() {return cardName;}
    public void setCardName(String cardName) {this.cardName = cardName;}

    public CardType getCardType() {return cardType;}
    public void setCardType(CardType cardType) {this.cardType = cardType;}

    public int getCardValue() {return cardValue;}
    public void setCardValue(int cardValue) {this.cardValue = cardValue;}

    @Override
    public String toString() {
        return "Card{" +
                "cardId='" + cardId + '\'' +
                ", name='" + cardName + '\'' +
                ", type=" + cardType +
                ", value=" + cardValue +
                '}';
    }
}
