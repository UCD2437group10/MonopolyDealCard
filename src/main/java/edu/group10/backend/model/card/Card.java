package edu.group10.backend.model.card;

/**
 * Represents the shared data exposed by every card type.
 */
public interface Card {
    String name();

    CardType type();

    String color();

    int bankValue();
}
