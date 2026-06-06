package edu.group10.monopolydeal.backend.model.player;

import edu.group10.monopolydeal.backend.model.card.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Player status: hand, bank, and properties
 */
public class PlayerState {

    private final Player player;
    private final List<Card> hand = new ArrayList<>();
    private final List<Card> bank = new ArrayList<>();
    private final Map<String, List<Card>> properties = new LinkedHashMap<>();
    private final Map<String, Integer> houseByColor = new LinkedHashMap<>();
    private final Map<String, Integer> hotelByColor = new LinkedHashMap<>();

    public PlayerState(Player player) {
        this.player = player;
    }

    public Player player() {
        return player;
    }

    public List<Card> hand() {
        return Collections.unmodifiableList(hand);
    }

    public List<Card> bank() {
        return Collections.unmodifiableList(bank);
    }

    public Map<String, List<Card>> properties() {
        Map<String, List<Card>> view = new LinkedHashMap<>();
        for (Map.Entry<String, List<Card>> entry : properties.entrySet()) {
            view.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(view);
    }

    public Map<String, Integer> houseByColor() {
        return Collections.unmodifiableMap(houseByColor);
    }

    public Map<String, Integer> hotelByColor() {
        return Collections.unmodifiableMap(hotelByColor);
    }

    public void addToHand(Card card) {
        hand.add(card);
    }

    public Card removeHandCard(int index) {
        return hand.remove(index);
    }

    public void addToBank(Card card) {
        bank.add(card);
    }

    public int bankTotal() {
        int total = 0;
        for (Card card : bank) {
            total += card.bankValue();
        }
        return total;
    }

    public void addProperty(String color, Card card) {
        properties.computeIfAbsent(color, key -> new ArrayList<>()).add(card);
    }

    public Card removeProperty(String color, int index) {
        List<Card> group = properties.get(color);
        if (group == null || index < 0 || index >= group.size()) {
            throw new IllegalArgumentException("invalid property index");
        }
        Card card = group.remove(index);
        if (group.isEmpty()) {
            properties.remove(color);
            houseByColor.remove(color);
            hotelByColor.remove(color);
        }
        return card;
    }

    public List<Card> removeAllProperties(String color) {
        List<Card> group = properties.remove(color);
        if (group == null) {
            return List.of();
        }
        return new ArrayList<>(group);
    }

    public void setAllProperties(String color, List<Card> cards) {
        properties.put(color, new ArrayList<>(cards));
    }

    public int propertyCount(String color) {
        List<Card> group = properties.get(color);
        return group == null ? 0 : group.size();
    }

    public boolean hasProperty(String color) {
        return propertyCount(color) > 0;
    }

    public List<Card> drainBankForPayment(int amount) {
        List<Card> paid = new ArrayList<>();
        int remain = amount;
        while (remain > 0 && !bank.isEmpty()) {
            Card card = bank.remove(bank.size() - 1);
            paid.add(card);
            remain -= card.bankValue();
        }
        return paid;
    }

    public boolean hasHouse(String color) {
        return houseByColor.getOrDefault(color, 0) > 0;
    }

    public boolean hasHotel(String color) {
        return hotelByColor.getOrDefault(color, 0) > 0;
    }

    public void addHouse(String color) {
        houseByColor.put(color, houseByColor.getOrDefault(color, 0) + 1);
    }

    public void addHotel(String color) {
        hotelByColor.put(color, hotelByColor.getOrDefault(color, 0) + 1);
    }

    public int clearHouse(String color) {
        return houseByColor.remove(color) == null ? 0 : 1;
    }

    public int clearHotel(String color) {
        return hotelByColor.remove(color) == null ? 0 : 1;
    }
}
