package edu.group10.core.manager;

import edu.group10.common.enums.PropertyColor;
import edu.group10.core.card.ActionCard;
import edu.group10.core.card.PropertyCard;
import edu.group10.core.card.action.*;
import edu.group10.core.card.property.*;

import java.util.HashMap;
import java.util.Map;

public class LogicCardManager {
    private static final Map<String, ActionCard> actionCardRegistry = new HashMap<>();
    private static final Map<String, PropertyCard> propertyCardRegistry = new HashMap<>();

    static {
        initializeActionCards();
        initializePropertyCards();
    }

    private static void initializeActionCards() {
        registerAction(new BrownLightBlueRentCard());
        registerAction(new GreenDarkBlueRentCard());
        registerAction(new PinkOrangeRentCard());
        registerAction(new RedYellowRentCard());
        registerAction(new RailroadUtilityRentCard());
        registerAction(new RentWildCard());

        registerAction(new SlyDealCard());
        registerAction(new DealBreakerCard());
        registerAction(new DebtCollectorCard());
        registerAction(new DoubleTheRentCard());
        registerAction(new ForcedDealCard());
        registerAction(new ItsMyBirthdayCard());
        registerAction(new JustSayNoCard());
        registerAction(new PassGoCard());
        registerAction(new HouseCard());
        registerAction(new HotelCard());
    }

    private static void initializePropertyCards() {
        for (int i = 1; i <= 2; i++) {
            registerProperty(new BrownPropertyCard());
        }
        for (int i = 1; i <= 3; i++) {
            registerProperty(new LightBluePropertyCard());
        }
        for (int i = 1; i <= 3; i++) {
            registerProperty(new PinkPropertyCard());
        }
        for (int i = 1; i <= 3; i++) {
            registerProperty(new OrangePropertyCard());
        }
        for (int i = 1; i <= 3; i++) {
            registerProperty(new RedPropertyCard());
        }
        for (int i = 1; i <= 3; i++) {
            registerProperty(new YellowPropertyCard());
        }
        for (int i = 1; i <= 3; i++) {
            registerProperty(new GreenPropertyCard());
        }
        for (int i = 1; i <= 2; i++) {
            registerProperty(new DarkBluePropertyCard());
        }
        for (int i = 1; i <= 4; i++) {
            registerProperty(new RailroadPropertyCard());
        }
        for (int i = 1; i <= 2; i++) {
            registerProperty(new UtilityPropertyCard());
        }

        //Dual property cards
        registerProperty(new PinkOrangeDualPropertyCard());
        registerProperty(new PinkOrangeDualPropertyCard());
        registerProperty(new UtilityRailroadDualPropertyCard());
        registerProperty(new RailroadLightBlueDualPropertyCard());
        registerProperty(new RailroadBrownDualPropertyCard());
        registerProperty(new GreenRailroadDualPropertyCard());
        registerProperty(new GreenDarkBlueDualPropertyCard());
        registerProperty(new RedYellowDualPropertyCard());
        registerProperty(new BrownLightBlueDualPropertyCard());

        //Wild property cards
        registerProperty(new WildPropertyCard());
        registerProperty(new WildPropertyCard());
    }

    private static void registerAction(ActionCard card) {
        actionCardRegistry.put(card.getCardId(), card);
    }

    private static void registerProperty(PropertyCard card) {
        propertyCardRegistry.put(card.getCardId(), card);
    }

    /**
     * Get action cards based on card ID
     */
    public static ActionCard getActionCard(String cardId) {
        return actionCardRegistry.get(cardId);
    }

    /**
     * Get property cards based on card ID
     */
    public static PropertyCard getPropertyCard(String cardId) {
        return propertyCardRegistry.get(cardId);
    }

    /**
     * Check if the action card exists
     */
    public static boolean hasActionCard(String cardId) {
        return actionCardRegistry.containsKey(cardId);
    }

    /**
     * Check if the property card exists
     */
    public static boolean hasPropertyCard(String cardId) {
        return propertyCardRegistry.containsKey(cardId);
    }
}
