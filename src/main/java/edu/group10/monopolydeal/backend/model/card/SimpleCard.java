package edu.group10.monopolydeal.backend.model.card;

/**
 * Immutable card implementation used across the current project.
 */
public record SimpleCard(String name, CardType type, String color, int bankValue) implements Card {
}
