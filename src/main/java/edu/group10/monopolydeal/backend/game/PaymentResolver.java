package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Resolves payments by moving bank and property assets.
 */
final class PaymentResolver {

    private final Map<String, PlayerState> players;

    PaymentResolver(Map<String, PlayerState> players) {
        this.players = players;
    }

    int totalAssetValue(PlayerState state) {
        int total = 0;
        for (Card card : state.bank()) {
            total += card.bankValue();
        }
        for (List<Card> group : state.properties().values()) {
            for (Card card : group) {
                total += card.bankValue();
            }
        }
        return total;
    }

    // Collect payment from bank cards first, then from properties if needed.
    void transferAutomaticPayment(String fromId, String toId, int amount) {
        if (amount <= 0) {
            return;
        }
        PlayerState from = playerState(fromId);
        PlayerState to = playerState(toId);

        int remain = amount;
        for (Card card : from.drainBankForPayment(remain)) {
            to.addToBank(card);
            remain -= card.bankValue();
        }

        if (remain <= 0) {
            return;
        }

        for (String color : new ArrayList<>(from.properties().keySet())) {
            while (remain > 0 && from.propertyCount(color) > 0) {
                Card property = from.removeProperty(color, from.propertyCount(color) - 1);
                to.addProperty(color, property);
                remain -= property.bankValue();
            }
            if (remain <= 0) {
                break;
            }
        }
    }

    void transferSelectedPayment(String fromId, String toId, int amount, PaymentSelection selection) {
        if (amount <= 0) {
            return;
        }
        PlayerState from = playerState(fromId);
        PlayerState to = playerState(toId);
        PaymentSelection chosen = selection == null ? new PaymentSelection(List.of(), Map.of()) : selection;
        int total = chosen.totalValue(from);
        if (total < amount && !chosen.selectsAllAssets(from)) {
            throw new IllegalArgumentException("selected assets do not cover the required amount");
        }

        List<Integer> bankIndexes = new ArrayList<>(chosen.bankIndexes());
        bankIndexes.sort(Comparator.reverseOrder());
        for (int index : bankIndexes) {
            to.addToBank(from.bank().get(index));
            from.drainBankCard(index);
        }

        for (Map.Entry<String, List<Integer>> entry : chosen.propertyIndexesByColor().entrySet()) {
            List<Integer> propertyIndexes = new ArrayList<>(entry.getValue());
            propertyIndexes.sort(Comparator.reverseOrder());
            for (int index : propertyIndexes) {
                Card property = from.removeProperty(entry.getKey(), index);
                to.addProperty(entry.getKey(), property);
            }
        }
    }

    private PlayerState playerState(String playerId) {
        PlayerState state = players.get(playerId);
        if (state == null) {
            throw new IllegalArgumentException("unknown player: " + playerId);
        }
        return state;
    }
}
