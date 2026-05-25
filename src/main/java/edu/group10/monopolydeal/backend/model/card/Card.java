package edu.group10.monopolydeal.backend.model.card;

/**
 * 卡牌基类。
 */
public interface Card {
    String name();

    CardType type();

    String color();

    int bankValue();
}
