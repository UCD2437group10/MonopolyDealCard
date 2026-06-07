package edu.group10.monopolydeal.backend.model.card;

/**
 * Basic card class
 */
public interface Card {

    String name();

    CardType type();

    String color();

    int bankValue();
}
