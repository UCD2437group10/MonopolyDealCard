package edu.group10.monopolydeal.backend.model.card;

/**
 * Simple card implementation
 */
public record SimpleCard(String name, CardType type, String color, int bankValue) implements Card {
}
