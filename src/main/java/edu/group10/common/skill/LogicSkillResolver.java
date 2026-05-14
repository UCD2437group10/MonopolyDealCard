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
        String[] otherPlayers = context.getOtherPlayerIdsAsArray();
        List<String> otherPlayerList = context.getOtherPlayerIds();
        String cardId = context.getSkillCard().getCardId();

        switch (cardName) {
            case "ACT_RENT_BROWN_LIGHT_BLUE":
                BrownLightBlueRentCard brownBlueRent = new BrownLightBlueRentCard();

                PropertyColor brownBlueColor = context.getSelectedColor();
                if (brownBlueColor == null) {
                    brownBlueColor = PropertyColor.BROWN;  //Default
                }

                Suit brownBlueSuit = buildSuitForColor(context, brownBlueColor);

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

                Collection<Command> greenDarkBlueResult = greenDarkBlueRent.returnCommand(
                        context.getActorId(),
                        context.getOtherPlayerIdsAsArray(),
                        greenDarkBlueSuit
                );

                if (greenDarkBlueResult != null) {
                    commands.addAll(greenDarkBlueResult);
                }
                break;

                /*
            case "ACT_RENT_PINK_ORANGE":

                 */
            case "ACT_RENT_RED_YELLOW":
                RedYellowRentCard redYellowRent = new RedYellowRentCard();

                PropertyColor redYellowColor = context.getSelectedColor();
                if (redYellowColor == null) {
                    redYellowColor = PropertyColor.RED;  //Default
                }

                Suit redYellowSuit = buildSuitForColor(context, redYellowColor);

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
                PropertyColor color = context.getSelectedColor();
                int baseRent = context.getBaseRent();
                Suit doubleRentSuit = new Suit(color, baseRent);  // 临时租金2M
                Collection<Command> doubleResult = doubleRent.returnCommand(
                        context.getActorId(),
                        context.getOtherPlayerIdsAsArray(),
                        doubleRentSuit
                );
                if (doubleResult != null) {
                    commands.addAll(doubleResult);
                }
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
        }
        return commands;
    }

    /**
     * 为指定颜色构建 Suit 对象
     */
    private Suit buildSuitForColor(SkillContext context, PropertyColor color) {
        Suit suit = new Suit();
        PlayerState actor = context.getActorPlayerState();

        if (actor == null) return suit;

        for (String propertyId : actor.getPropertyIds()) {
            PropertyCard propertyCard = LogicCardManager.getPropertyCard(propertyId);
            if (propertyCard != null) {
                // 检查这张物业卡是否包含该颜色
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
