package edu.group10.common.skill;

import edu.group10.common.model.*;
import edu.group10.core.card.PropertyCard;
import edu.group10.core.card.action.*;
import edu.group10.common.enums.*;
import edu.group10.core.manager.LogicCardManager;

import java.util.*;

public class LogicSkillResolver implements SkillResolver {
    @Override
    public List<Command> resolve(SkillContext context) {
        String cardName = context.getSkillCard().getCardName();
        List<Command> commands = new ArrayList<>();
        String cardId = context.getSkillCard().getCardId();

        switch (cardName) {
            case "ACT_RENT_BROWN_LIGHT_BLUE":
                BrownLightBlueRentCard brownBlueRent = new BrownLightBlueRentCard();

                PropertyColor brownBlueColor = context.getSelectedColor();
                if (brownBlueColor == null) {
                    brownBlueColor = PropertyColor.BROWN;  //Default
                }

                Suit brownBlueSuit = buildSuitForColor(context, brownBlueColor);

                context.setLastRentSuit(brownBlueSuit);

                Collection<Command> brownBlueResult = brownBlueRent.returnCommand(
                        context.getActorId(),
                        context.getOtherPlayerIdsAsArray(),
                        brownBlueSuit
                );

                if (brownBlueResult != null) {
                    commands.addAll(brownBlueResult);
                }
                break;

            case "ACT_RENT_GREEN_DARK_BLUE":
                GreenDarkBlueRentCard greenDarkBlueRent = new GreenDarkBlueRentCard();

                PropertyColor greenDarkBlueColor = context.getSelectedColor();
                if (greenDarkBlueColor == null) {
                    greenDarkBlueColor = PropertyColor.GREEN;  //Default
                }

                Suit greenDarkBlueSuit = buildSuitForColor(context, greenDarkBlueColor);

                context.setLastRentSuit(greenDarkBlueSuit);

                Collection<Command> greenDarkBlueResult = greenDarkBlueRent.returnCommand(
                        context.getActorId(),
                        context.getOtherPlayerIdsAsArray(),
                        greenDarkBlueSuit
                );

                if (greenDarkBlueResult != null) {
                    commands.addAll(greenDarkBlueResult);
                }
                break;

            case "ACT_RENT_PINK_ORANGE":
                PinkOrangeRentCard pinkOrangeRent = new PinkOrangeRentCard();

                PropertyColor pinkOrangeColor = context.getSelectedColor();
                if (pinkOrangeColor == null) {
                    pinkOrangeColor = PropertyColor.PINK;  //Default
                }

                Suit pinkOrangeSuit = buildSuitForColor(context, pinkOrangeColor);

                context.setLastRentSuit(pinkOrangeSuit);

                Collection<Command> pinkOrangeResult = pinkOrangeRent.returnCommand(
                        context.getActorId(),
                        context.getOtherPlayerIdsAsArray(),
                        pinkOrangeSuit
                );

                if (pinkOrangeResult != null) {
                    commands.addAll(pinkOrangeResult);
                }
                break;

            case "ACT_RENT_RED_YELLOW":
                RedYellowRentCard redYellowRent = new RedYellowRentCard();

                PropertyColor redYellowColor = context.getSelectedColor();
                if (redYellowColor == null) {
                    redYellowColor = PropertyColor.RED;  //Default
                }

                Suit redYellowSuit = buildSuitForColor(context, redYellowColor);

                context.setLastRentSuit(redYellowSuit);

                Collection<Command> redYellowResult = redYellowRent.returnCommand(
                        context.getActorId(),
                        context.getOtherPlayerIdsAsArray(),
                        redYellowSuit
                );

                if (redYellowResult != null) {
                    commands.addAll(redYellowResult);
                }
                break;
            case "ACT_DEAL_BREAKER":
                DealBreakerCard dealBreaker = new DealBreakerCard();
                String[] dealBreakerTarget = {context.getTargetId()};
                Collection<Command> dealBreakerResult = dealBreaker.returnCommand(
                        context.getActorId(),
                        dealBreakerTarget,
                        null
                );
                if (dealBreakerResult != null) {
                    commands.addAll(dealBreakerResult);
                }
                break;

            case "ACT_DEBT_COLLECTOR":
                DebtCollectorCard debtCollector = new DebtCollectorCard();
                String[] debtTarget = {context.getTargetId()};
                Collection<Command> debtResult = debtCollector.returnCommand(
                        context.getActorId(),
                        debtTarget
                );
                if (debtResult != null) {
                    commands.addAll(debtResult);
                }
                break;

            case "ACT_DOUBLE_THE_RENT":
                DoubleTheRentCard doubleRent = new DoubleTheRentCard();

                Suit lastRentSuit = context.getLastRentSuit();

                if (lastRentSuit == null) {
                    System.out.println("[LogicSkillResolver] Double The Rent must be used with rent cards");
                    break;
                }

                Collection<Command> doubleResult = doubleRent.returnCommand(
                        context.getActorId(),
                        context.getOtherPlayerIdsAsArray(),
                        lastRentSuit
                );

                if (doubleResult != null) {
                    commands.addAll(doubleResult);
                }

                context.setLastRentSuit(null);
                break;

            case "ACT_FORCED_DEAL":
                ForcedDealCard forcedDeal = new ForcedDealCard();
                String[] forcedTarget = {context.getTargetId()};
                Collection<Command> forcedResult = forcedDeal.returnCommand(
                        context.getActorId(),
                        forcedTarget
                );
                if (forcedResult != null) {
                    commands.addAll(forcedResult);
                }
                break;

            case "ACT_HOTEL":
                HotelCard hotel = new HotelCard();
                Suit hotelSuit = new Suit();
                hotel.returnCommand(hotelSuit);
                break;

            case "ACT_HOUSE":
                HouseCard house = new HouseCard();
                Suit houseSuit = new Suit();
                house.returnCommand(houseSuit);
                break;

            case "ACT_ITS_MY_BIRTHDAY":
                ItsMyBirthdayCard birthdayCard = new ItsMyBirthdayCard();
                Collection<Command> birthdayResult = birthdayCard.returnCommand(
                        context.getActorId(),
                        context.getOtherPlayerIdsAsArray()
                );
                if (birthdayResult != null) {
                    commands.addAll(birthdayResult);
                }
                break;

            case "ACT_JUST_SAY_NO":
                JustSayNoCard justSayNo = new JustSayNoCard();
                String targetId = context.getTargetId();
                if (targetId != null) {
                    ArrayList<Command> justSayNoResult = justSayNo.returnOperation(
                            context.getActorId(),
                            targetId
                    );
                    if (justSayNoResult != null) {
                        commands.addAll(justSayNoResult);
                    }
                }
                break;

            case "ACT_PASS_GO":
                PassGoCard passGo = new PassGoCard();
                Collection<Command> passGoResult = passGo.returnCommand(context.getActorId());
                if (passGoResult != null) {
                    commands.addAll(passGoResult);
                }
                break;

            case "ACT_RENT_WILD":
                RentWildCard rentWild = (RentWildCard) LogicCardManager.getActionCard(cardId);

                PropertyColor selectedColor = context.getSelectedColor();
                if (selectedColor == null) {
                    selectedColor = PropertyColor.BROWN;
                }

                Suit suit = buildSuitForColor(context, selectedColor);

                Collection<Command> result = rentWild.returnCommand(
                        context.getActorId(),
                        context.getOtherPlayerIdsAsArray(),
                        suit
                );

                if (result != null) {
                    commands.addAll(result);
                }
                break;

            case "ACT_RENT_RAILROAD_UTILITY":
                RailroadUtilityRentCard railroadUtilityRent = new RailroadUtilityRentCard();

                PropertyColor railroadUtilityColor = context.getSelectedColor();
                if (railroadUtilityColor == null) {
                    railroadUtilityColor = PropertyColor.RAILROAD;  //Default
                }

                Suit railroadUtilitySuit = buildSuitForColor(context, railroadUtilityColor);

                Collection<Command> railroadUtilityResult = railroadUtilityRent.returnCommand(
                        context.getActorId(),
                        context.getOtherPlayerIdsAsArray(),
                        railroadUtilitySuit
                );

                if (railroadUtilityResult != null) {
                    commands.addAll(railroadUtilityResult);
                }
                break;

            case "ACT_SLY_DEAL":
                SlyDealCard slyDeal = new SlyDealCard();

                String targetIdS = context.getTargetId();
                if (targetIdS != null) {
                    Collection<Command> slyDealResult = slyDeal.returnCommand(
                            context.getActorId(),
                            targetIdS,
                            null
                    );

                    if (slyDealResult != null) {
                        commands.addAll(slyDealResult);
                    }
                }
                break;
        }
        return commands;
    }

    /**
     * Create Suit object with a specific color
     */
    private Suit buildSuitForColor(SkillContext context, PropertyColor color) {
        Suit suit = new Suit();
        PlayerState actor = context.getActorPlayerState();

        if (actor == null) return suit;

        for (String propertyId : actor.getPropertyIds()) {
            PropertyCard propertyCard = LogicCardManager.getPropertyCard(propertyId);
            if (propertyCard != null) {
                for (PropertyColor cardColor : propertyCard.getColours()) {
                    if (cardColor == color) {
                        suit.addCard(propertyCard, 0);
                        break;
                    }
                }
            }
        }

        return suit;
    }
}
