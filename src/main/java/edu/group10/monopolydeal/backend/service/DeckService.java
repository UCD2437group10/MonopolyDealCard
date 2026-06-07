package edu.group10.monopolydeal.backend.service;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds the Monopoly Deal deck from the project card definitions.
 */
public class DeckService {

    /** Creates and shuffles a full deck for a new match. */
    public List<Card> createDeck() {
        List<Card> cards = new ArrayList<>();

        // Property (28)
        add(cards, "Mediterranean Avenue", CardType.PROPERTY, "Brown", 0, 1);
        add(cards, "Baltic Avenue", CardType.PROPERTY, "Brown", 0, 1);
        add(cards, "Oriental Avenue", CardType.PROPERTY, "Light Blue", 0, 1);
        add(cards, "Vermont Avenue", CardType.PROPERTY, "Light Blue", 0, 1);
        add(cards, "Connecticut Avenue", CardType.PROPERTY, "Light Blue", 0, 1);
        add(cards, "St. Charles Place", CardType.PROPERTY, "Pink", 0, 1);
        add(cards, "States Avenue", CardType.PROPERTY, "Pink", 0, 1);
        add(cards, "Virginia Avenue", CardType.PROPERTY, "Pink", 0, 1);
        add(cards, "St. James Place", CardType.PROPERTY, "Orange", 0, 1);
        add(cards, "Tennessee Avenue", CardType.PROPERTY, "Orange", 0, 1);
        add(cards, "New York Avenue", CardType.PROPERTY, "Orange", 0, 1);
        add(cards, "Kentucky Avenue", CardType.PROPERTY, "Red", 0, 1);
        add(cards, "Indiana Avenue", CardType.PROPERTY, "Red", 0, 1);
        add(cards, "Illinois Avenue", CardType.PROPERTY, "Red", 0, 1);
        add(cards, "Atlantic Avenue", CardType.PROPERTY, "Yellow", 0, 1);
        add(cards, "Ventnor Avenue", CardType.PROPERTY, "Yellow", 0, 1);
        add(cards, "Marvin Gardens", CardType.PROPERTY, "Yellow", 0, 1);
        add(cards, "Pacific Avenue", CardType.PROPERTY, "Green", 0, 1);
        add(cards, "North Carolina Avenue", CardType.PROPERTY, "Green", 0, 1);
        add(cards, "Pennsylvania Avenue", CardType.PROPERTY, "Green", 0, 1);
        add(cards, "Boardwalk", CardType.PROPERTY, "Deep Blue", 0, 1);
        add(cards, "Park Place", CardType.PROPERTY, "Deep Blue", 0, 1);
        add(cards, "Reading Railroad", CardType.PROPERTY, "Railroad", 0, 1);
        add(cards, "Pennsylvania Railroad", CardType.PROPERTY, "Railroad", 0, 1);
        add(cards, "B&O Railroad", CardType.PROPERTY, "Railroad", 0, 1);
        add(cards, "Short Line", CardType.PROPERTY, "Railroad", 0, 1);
        add(cards, "Electric Company", CardType.PROPERTY, "Utility", 0, 1);
        add(cards, "Water Works", CardType.PROPERTY, "Utility", 0, 1);

        // Multi property (11)
        add(cards, "Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0, 1);
        add(cards, "Light Blue/Railroad Multi", CardType.MULTI_PROPERTY, "Light Blue/Railroad", 0, 1);
        add(cards, "Pink/Orange Multi", CardType.MULTI_PROPERTY, "Pink/Orange", 0, 2);
        add(cards, "Red/Yellow Multi", CardType.MULTI_PROPERTY, "Red/Yellow", 0, 2);
        add(cards, "Deep Blue/Green Multi", CardType.MULTI_PROPERTY, "Deep Blue/Green", 0, 1);
        add(cards, "Green/Railroad Multi", CardType.MULTI_PROPERTY, "Green/Railroad", 0, 1);
        add(cards, "Railroad/Utility Multi", CardType.MULTI_PROPERTY, "Railroad/Utility", 0, 1);
        add(cards, "Wild Property", CardType.MULTI_PROPERTY, "Wild", 0, 2);

        // Action (34)
        add(cards, "Deal Breaker", CardType.ACTION, "-", 5, 2);
        add(cards, "Just Say No", CardType.ACTION, "-", 4, 3);
        add(cards, "Sly Deal", CardType.ACTION, "-", 3, 3);
        add(cards, "Forced Deal", CardType.ACTION, "-", 3, 4);
        add(cards, "Debt Collector", CardType.ACTION, "-", 3, 3);
        add(cards, "It's My Birthday", CardType.ACTION, "-", 2, 3);
        add(cards, "Pass Go", CardType.ACTION, "-", 1, 10);
        add(cards, "House", CardType.ACTION, "-", 3, 3);
        add(cards, "Hotel", CardType.ACTION, "-", 4, 4);
        add(cards, "Double The Rent", CardType.ACTION, "-", 1, 2);

        // Rent cards are multi-color or wild, following the project data sheet.
        add(cards, "Rent Light Blue-Brown", CardType.RENT, "Light Blue/Brown", 1, 2);
        add(cards, "Rent Orange-Pink", CardType.RENT, "Orange/Pink", 1, 2);
        add(cards, "Rent Yellow-Red", CardType.RENT, "Yellow/Red", 1, 2);
        add(cards, "Rent Utility-Railroad", CardType.RENT, "Utility/Railroad", 1, 2);
        add(cards, "Rent Blue-Green", CardType.RENT, "Deep Blue/Green", 1, 2);
        add(cards, "Rent Wild", CardType.RENT, "Any", 3, 3);

        // Money (24)
        add(cards, "1M Money", CardType.MONEY, "Yellow", 1, 6);
        add(cards, "2M Money", CardType.MONEY, "Orange-Red", 2, 5);
        add(cards, "3M Money", CardType.MONEY, "Green", 3, 3);
        add(cards, "4M Money", CardType.MONEY, "Blue", 4, 3);
        add(cards, "5M Money", CardType.MONEY, "Purple", 5, 2);
        add(cards, "10M Money", CardType.MONEY, "Gold-Orange", 10, 1);

        if (cards.size() != 109) {
            throw new IllegalStateException("deck size must be 109, actual: " + cards.size());
        }
        Collections.shuffle(cards);
        return cards;
    }

    /** Adds repeated copies of one card definition to the deck list. */
    private void add(List<Card> cards, String name, CardType cardType, String color, int value, int count) {
        for (int i = 0; i < count; i++) {
            cards.add(new SimpleCard(name, cardType, color, value));
        }
    }
}
