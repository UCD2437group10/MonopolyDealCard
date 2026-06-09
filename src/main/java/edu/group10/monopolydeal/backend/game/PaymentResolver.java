package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.ArrayList;
import java.util.Map;

/**
 * Resolves payments by moving bank and property assets.
 */
final class PaymentResolver {

    private final Map<String, PlayerState> players;

    PaymentResolver(Map<String, PlayerState> players) {
        this.players = players;
    }

    // Collect payment from bank cards first, then from properties if needed.
    void transferPayment(String fromId, String toId, int amount) {
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
                remain -= 1;
            }
            if (remain <= 0) {
                break;
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
