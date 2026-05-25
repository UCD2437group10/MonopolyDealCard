package edu.group10.monopolydeal.backend.model.card;

/**
 * 简化卡牌实现，满足第一版规则联调。
 */
public record SimpleCard(String name, CardType type, String color, int bankValue) implements Card {
}
