package edu.group10.common.skill;

import edu.group10.common.model.*;
import edu.group10.common.skill.SkillResolver;
import edu.group10.core.card.action.*;
import edu.group10.common.enums.*;
import java.util.*;

public class DummySkillResolver implements SkillResolver {
    @Override
    public List<Command> resolve(SkillContext context) {
        String cardName = context.getSkillCard().getCardName();
        List<Command> commands = new ArrayList<>();
        String[] otherPlayers = context.getOtherPlayerIdsAsArray();
        List<String> otherPlayerList = context.getOtherPlayerIds();

        switch (cardName) {
            case "ACT_RENT_BROWN_LIGHT_BLUE":
                BrownLightBlueRentCard rentCard = new BrownLightBlueRentCard();

                String fromPlayer = context.getActorId();
                String[] toAllPlayer = context.getOtherPlayerIdsAsArray();
                Suit suit = new Suit(PropertyColor.BROWN, calculateRent(context, PropertyColor.BROWN));

                Collection<Command> result = rentCard.returnCommand(fromPlayer, toAllPlayer, suit);
                if (result != null) {
                    commands.addAll(result);
                }
                break;
        }
    }

    /**
     * 计算租金金额
     * 根据玩家拥有的指定颜色物业数量决定租金
     */
    private int calculateRent(SkillContext context, PropertyColor color) {
        PlayerState actor = context.getActorPlayerState();
        if (actor == null) return 1;

        // 统计玩家拥有该颜色的物业数量
        long count = actor.getPropertyIds().stream()
                .map(propertyId -> getPropertyColorById(propertyId))  // 需要实现这个方法
                .filter(c -> c == color)
                .count();

        // 简化的租金计算（实际应根据需求文档的租金表）
        if (count == 1) return 1;
        if (count == 2) return 2;
        return 3;
    }

    /**
     * 根据物业ID获取颜色（临时实现）
     */
    private PropertyColor getPropertyColorById(String propertyId) {
        // 临时：根据 ID 前缀判断
        if (propertyId.startsWith("brown")) return PropertyColor.BROWN;
        if (propertyId.startsWith("light_blue")) return PropertyColor.LIGHT_BLUE;
        if (propertyId.startsWith("pink")) return PropertyColor.PINK;
        if (propertyId.startsWith("orange")) return PropertyColor.ORANGE;
        if (propertyId.startsWith("red")) return PropertyColor.RED;
        if (propertyId.startsWith("yellow")) return PropertyColor.YELLOW;
        if (propertyId.startsWith("green")) return PropertyColor.GREEN;
        if (propertyId.startsWith("dark_blue")) return PropertyColor.DARK_BLUE;
        if (propertyId.startsWith("railroad")) return PropertyColor.RAILROAD;
        if (propertyId.startsWith("utility")) return PropertyColor.UTILITY;
        return PropertyColor.BROWN;  // 默认
    }
}
