package edu.group10.monopolydeal.backend.model.player;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.service.PropertySetRules;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the mutable in-game state for one player.
 */
public class PlayerState {

    /** Immutable player identity. */
    private final Player player;
    /** Cards currently held in hand. */
    private final List<Card> hand = new ArrayList<>();
    /** Cards placed in the bank area. */
    private final List<Card> bank = new ArrayList<>();
    /** Property groups keyed by their current color label. */
    private final Map<String, List<Card>> properties = new LinkedHashMap<>();
    /** House count for each property group. */
    private final Map<String, Integer> houseByColor = new LinkedHashMap<>();
    /** Hotel count for each property group. */
    private final Map<String, Integer> hotelByColor = new LinkedHashMap<>();

    /** Creates a state container for the given player. */
    public PlayerState(Player player) {
        this.player = player;
    }

    /** Returns the owner of this state object. */
    public Player player() {
        return player;
    }

    /** Returns a read-only view of the player's hand. */
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

    /** Computes the total value of the bank area. */
    public int bankTotal() {
        int total = 0;
        for (Card card : bank) {
            total += card.bankValue();
        }
        return total;
    }

    /** Adds a property to the most suitable stack for the chosen color. */
    public void addProperty(String color, Card card) {
        String targetGroup = findPropertyGroupForAdd(color);
        addPropertyToExactGroup(targetGroup, card);
    }

    public void addPropertyToExactGroup(String color, Card card) {
        properties.computeIfAbsent(color, key -> new ArrayList<>()).add(card);
    }

    public void moveProperty(String fromColor, int index, String toColor) {
        Card card = removeProperty(fromColor, index);
        addProperty(toColor, card);
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

    /** Chooses the best destination stack when duplicate color groups exist. */
    private String findPropertyGroupForAdd(String color) {
        if (color == null || color.isBlank()) {
            return color;
        }
        String baseColor = PropertySetRules.baseColor(color);
        String bestGroup = null;
        int bestCount = Integer.MAX_VALUE;
        int maxIndex = 1;
        for (String key : properties.keySet()) {
            if (!PropertySetRules.baseColor(key).equals(baseColor)) {
                continue;
            }
            maxIndex = Math.max(maxIndex, groupIndex(key));
            int size = propertyCount(key);
            if (size < PropertySetRules.requiredSetSize(baseColor) && size < bestCount) {
                bestCount = size;
                bestGroup = key;
            }
        }
        if (bestGroup != null) {
            return bestGroup;
        }
        return properties.containsKey(baseColor) ? baseColor + " (" + (maxIndex + 1) + ")" : baseColor;
    }

    private int groupIndex(String color) {
        if (color == null) {
            return 1;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(" \\((\\d+)\\)$").matcher(color);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 1;
    }
}
