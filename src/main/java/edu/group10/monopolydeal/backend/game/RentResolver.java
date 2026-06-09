package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Resolves rent calculation and Double The Rent consumption.
 */
final class RentResolver {

    private final Map<String, PlayerState> players;
    private final Deque<Card> discardPile;

    RentResolver(Map<String, PlayerState> players, Deque<Card> discardPile) {
        this.players = players;
        this.discardPile = discardPile;
    }

    // Compute rent for the chosen color and applied buildings.
    int calculateRent(String ownerId, Card rentCard, String colorChoice) {
        String color = resolveRentColor(rentCard, colorChoice, ownerId);
        int count = playerState(ownerId).propertyCount(color);
        if (count == 0) {
            throw new IllegalStateException("no property in chosen color");
        }
        int baseRent = baseRentByColorAndCount(color, count);
        if (playerState(ownerId).hasHouse(color)) {
            baseRent += 3;
        }
        if (playerState(ownerId).hasHotel(color)) {
            baseRent += 4;
        }
        return baseRent;
    }

    // Discard the requested number of Double The Rent cards from hand.
    void consumeDoubleRent(String playerId, int count) {
        PlayerState state = playerState(playerId);
        for (int i = 0; i < count; i++) {
            int index = findCardIndexByName(state.hand(), "Double The Rent");
            if (index < 0) {
                throw new IllegalStateException("not enough Double The Rent cards");
            }
            addToDiscardPileIfAction(state.removeHandCard(index));
        }
    }

    private int baseRentByColorAndCount(String color, int count) {
        return switch (color) {
            case "Brown" -> count >= 2 ? 2 : 1;
            case "Light Blue", "Pink", "Orange", "Red", "Yellow", "Green" -> {
                if (count == 1) {
                    yield 1;
                }
                if (count == 2) {
                    yield 2;
                }
                yield 4;
            }
            case "Deep Blue" -> count >= 2 ? 4 : 2;
            case "Railroad" -> Math.min(count, 4);
            case "Utility" -> count >= 2 ? 2 : 1;
            default -> throw new IllegalArgumentException("unsupported rent color: " + color);
        };
    }

    private String resolveRentColor(Card rentCard, String colorChoice, String ownerId) {
        if (rentCard.color().contains("/")) {
            if (colorChoice == null || colorChoice.isBlank()) {
                throw new IllegalArgumentException("rent card requires colorChoice");
            }
            String[] colors = rentCard.color().split("/");
            for (String color : colors) {
                if (color.equals(colorChoice)) {
                    return colorChoice;
                }
            }
            throw new IllegalArgumentException("invalid colorChoice");
        }
        if ("Any".equals(rentCard.color())) {
            if (colorChoice == null || colorChoice.isBlank()) {
                throw new IllegalArgumentException("wild rent requires colorChoice");
            }
            if (!playerState(ownerId).hasProperty(colorChoice)) {
                throw new IllegalArgumentException("player has no such color property");
            }
            return colorChoice;
        }
        return rentCard.color();
    }

    private void addToDiscardPileIfAction(Card card) {
        if (card != null && card.type() == CardType.ACTION) {
            discardPile.push(card);
        }
    }

    private int findCardIndexByName(List<Card> cards, String cardName) {
        for (int i = 0; i < cards.size(); i++) {
            if (cardName.equals(cards.get(i).name())) {
                return i;
            }
        }
        return -1;
    }

    private PlayerState playerState(String playerId) {
        PlayerState state = players.get(playerId);
        if (state == null) {
            throw new IllegalArgumentException("unknown player: " + playerId);
        }
        return state;
    }
}
