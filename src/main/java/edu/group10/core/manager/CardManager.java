package edu.group10.core.manager;

import edu.group10.common.enums.CardType;
import edu.group10.common.enums.PropertyColor;
import edu.group10.common.model.Card;
import edu.group10.common.model.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardManager {
    private Map<String, Card> cardRegistry; // cardId → Card object
    private List<Card> allCards; //All the card lists (used to initialize the deck)

    public CardManager() {
        this.cardRegistry = new HashMap<>();
        this.allCards = new ArrayList<>();
        initializeAllCards();
    }

    private void initializeAllCards() {
        //Single-Color properties
        //Brown (2 cards，1M)
        addProperty("brown_1", "Brown Property", PropertyColor.BROWN, null, 1, 2);
        addProperty("brown_2", "Brown Property", PropertyColor.BROWN, null, 1, 2);

        //Light blue (3 cards，1M)
        for (int i = 1; i <= 3; i++) {
            addProperty("light_blue_" + i, "Light Blue Property", PropertyColor.LIGHT_BLUE, null, 1, 3);
        }

        //Pink (3 cards，2M)
        for (int i = 1; i <= 3; i++) {
            addProperty("pink_" + i, "Pink Property", PropertyColor.PINK, null, 2, 3);
        }

        //Orange (3 cards，2M)
        for (int i = 1; i <= 3; i++) {
            addProperty("orange_" + i, "Orange Property", PropertyColor.ORANGE, null, 2, 3);
        }

        //Red (3 cards，3M)
        for (int i = 1; i <= 3; i++) {
            addProperty("red_" + i, "Red Property", PropertyColor.RED, null, 3, 3);
        }

        //Yellow (3 cards，3M)
        for (int i = 1; i <= 3; i++) {
            addProperty("yellow_" + i, "Yellow Property", PropertyColor.YELLOW, null, 3, 3);
        }

        //Green (3 cards，4M)
        for (int i = 1; i <= 3; i++) {
            addProperty("green_" + i, "Green Property", PropertyColor.GREEN, null, 4, 3);
        }

        //Dark blue (2 cards，4M)
        for (int i = 1; i <= 2; i++) {
            addProperty("dark_blue_" + i, "Dark Blue Property", PropertyColor.DARK_BLUE, null, 4, 2);
        }

        //Railroad (4 cards，2M)
        for (int i = 1; i <= 4; i++) {
            addProperty("railroad_" + i, "Railroad", PropertyColor.RAILROAD, null, 2, 4);
        }

        //Utility-Railroad (2张，2M)
        for (int i = 1; i <= 2; i++) {
            addProperty("utility_" + i, "Utility", PropertyColor.UTILITY, null, 2, 2);
        }

        //Dual-Color properties
        addProperty("pink_orange_1", "Pink/Orange Property", PropertyColor.PINK, PropertyColor.ORANGE, 2, 2);
        addProperty("pink_orange_2", "Pink/Orange Property", PropertyColor.PINK, PropertyColor.ORANGE, 2, 2);

        addProperty("utility_railroad_1", "Utility/Railroad Property", PropertyColor.UTILITY, PropertyColor.RAILROAD, 2, 1);

        addProperty("railroad_light_blue_1", "Railroad/Light Blue Property", PropertyColor.RAILROAD, PropertyColor.LIGHT_BLUE, 4, 1);

        addProperty("railroad_brown_1", "Railroad/Brown Property", PropertyColor.RAILROAD, PropertyColor.BROWN, 1, 1);

        addProperty("green_railroad_1", "Green/Railroad Property", PropertyColor.GREEN, PropertyColor.RAILROAD, 4, 1);

        addProperty("green_dark_blue_1", "Green/Dark Blue Property", PropertyColor.GREEN, PropertyColor.DARK_BLUE, 4, 1);

        addProperty("red_yellow_1", "Red/Yellow Property", PropertyColor.RED, PropertyColor.YELLOW, 3, 1);

        addProperty("brown_light_blue_1", "Brown/Light Blue Property", PropertyColor.BROWN, PropertyColor.LIGHT_BLUE, 1, 1);

        //Wild properties (2 cards)
        addProperty("wild_1", "Wild Property", PropertyColor.WILD, null, 0, 1);
        addProperty("wild_2", "Wild Property", PropertyColor.WILD, null, 0, 1);

        //Action cards
        addActionCard("sly_deal_1", "Sly Deal", 3);
        addActionCard("sly_deal_2", "Sly Deal", 3);
        addActionCard("sly_deal_3", "Sly Deal", 3);

        addActionCard("deal_breaker_1", "Deal Breaker", 5);
        addActionCard("deal_breaker_2", "Deal Breaker", 5);

        addActionCard("house_1", "House", 3);
        addActionCard("house_2", "House", 3);
        addActionCard("house_3", "House", 3);

        addActionCard("hotel_1", "Hotel", 4);
        addActionCard("hotel_2", "Hotel", 4);

        addActionCard("double_rent_1", "Double The Rent", 1);
        addActionCard("double_rent_2", "Double The Rent", 1);

        addActionCard("debt_collector_1", "Debt Collector", 3);
        addActionCard("debt_collector_2", "Debt Collector", 3);
        addActionCard("debt_collector_3", "Debt Collector", 3);

        addActionCard("birthday_1", "It's My Birthday", 2);
        addActionCard("birthday_2", "It's My Birthday", 2);
        addActionCard("birthday_3", "It's My Birthday", 2);

        addActionCard("forced_deal_1", "Forced Deal", 3);
        addActionCard("forced_deal_2", "Forced Deal", 3);
        addActionCard("forced_deal_3", "Forced Deal", 3);

        addActionCard("just_say_no_1", "Just Say No", 4);
        addActionCard("just_say_no_2", "Just Say No", 4);
        addActionCard("just_say_no_3", "Just Say No", 4);

        // Pass Go (10 cards)
        for (int i = 1; i <= 10; i++) {
            addActionCard("pass_go_" + i, "Pass Go", 1);
        }

        //Rent cards
        addActionCard("rent_wild_1", "Rent Wild", 3);
        addActionCard("rent_wild_2", "Rent Wild", 3);
        addActionCard("rent_wild_3", "Rent Wild", 3);

        addActionCard("rent_railroad_utility_1", "Railroad/Utility Rent", 1);
        addActionCard("rent_railroad_utility_2", "Railroad/Utility Rent", 1);

        addActionCard("rent_green_dark_blue_1", "Green/Dark Blue Rent", 1);
        addActionCard("rent_green_dark_blue_2", "Green/Dark Blue Rent", 1);

        addActionCard("rent_brown_light_blue_1", "Brown/Light Blue Rent", 1);
        addActionCard("rent_brown_light_blue_2", "Brown/Light Blue Rent", 1);

        addActionCard("rent_pink_orange_1", "Pink/Orange Rent", 1);
        addActionCard("rent_pink_orange_2", "Pink/Orange Rent", 1);

        addActionCard("rent_red_yellow_1", "Red/Yellow Rent", 1);
        addActionCard("rent_red_yellow_2", "Red/Yellow Rent", 1);

        //Money cards
        // 1M (6 cards)
        for (int i = 1; i <= 6; i++) {
            addMoneyCard("money_1m_" + i, "1M", 1);
        }
        // 2M (5 cards)
        for (int i = 1; i <= 5; i++) {
            addMoneyCard("money_2m_" + i, "2M", 2);
        }
        // 3M (3 cards)
        for (int i = 1; i <= 3; i++) {
            addMoneyCard("money_3m_" + i, "3M", 3);
        }
        // 4M (3 cards)
        for (int i = 1; i <= 3; i++) {
            addMoneyCard("money_4m_" + i, "4M", 4);
        }
        // 5M (2 cards)
        for (int i = 1; i <= 2; i++) {
            addMoneyCard("money_5m_" + i, "5M", 5);
        }
        // 10M (1 card)
        addMoneyCard("money_10m_1", "10M", 10);
    }

    private void addProperty(String cardId, String name, PropertyColor primary,
                             PropertyColor secondary, int rent, int setSize) {
        Property p = new Property(cardId, name, primary, secondary, rent, setSize);
        cardRegistry.put(cardId, p);
        allCards.add(p);
    }

    private void addActionCard(String cardId, String name, int value) {
        Card card = new Card(cardId, name, CardType.ACTION, value);
        cardRegistry.put(cardId, card);
        allCards.add(card);
    }

    private void addMoneyCard(String cardId, String name, int value) {
        Card card = new Card(cardId, name, CardType.MONEY, value);
        cardRegistry.put(cardId, card);
        allCards.add(card);
    }

    public Card getCardById(String cardId) {
        return cardRegistry.get(cardId);
    }

    public List<Card> getAllCards() {
        return new ArrayList<>(allCards);
    }

    public List<Card> getShuffledDeck() {
        List<Card> shuffled = new ArrayList<>(allCards);
        java.util.Collections.shuffle(shuffled);
        return shuffled;
    }
}
