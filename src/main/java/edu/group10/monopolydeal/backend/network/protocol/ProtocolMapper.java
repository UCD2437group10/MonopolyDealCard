package edu.group10.monopolydeal.backend.network.protocol;

import edu.group10.monopolydeal.backend.game.GameState;
import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts between domain state objects and transport records.
 */
public final class ProtocolMapper {

    /** Utility class. */
    private ProtocolMapper() {
    }

    /** Converts a domain response into its transport form. */
    public static NetGameResponse toNet(GameResponse response) {
        if (response == null) {
            return new NetGameResponse(false, "empty response", null);
        }
        return new NetGameResponse(response.success(), response.message(), toNet(response.gameState()));
    }

    /** Converts a transport response back into its domain form. */
    public static GameResponse toDomain(NetGameResponse response) {
        if (response == null) {
            return new GameResponse(false, "empty response", null);
        }
        return new GameResponse(response.success(), response.message(), toDomain(response.gameState()));
    }

    /** Converts the runtime game snapshot to a network-safe snapshot. */
    private static NetGameState toNet(GameState state) {
        if (state == null) {
            return null;
        }
        List<NetPlayerState> players = state.players().stream()
                .map(ProtocolMapper::toNet)
                .toList();
        return new NetGameState(
                state.started(),
                state.gameOver(),
                state.winnerPlayerId(),
                state.currentPlayerId(),
                state.pendingPaymentPayerPlayerId(),
                state.pendingPaymentCollectorPlayerId(),
                state.pendingPaymentAmount(),
                state.pendingPaymentSourceAction(),
                state.jsnResponderPlayerId(),
                state.jsnActorPlayerId(),
                state.jsnTargetPlayerId(),
                state.jsnSourceAction(),
                state.drawPileCount(),
                state.discardPileCount(),
                toSimpleCards(state.discardPileCards()),
                players,
                state.readyPlayerIds()
        );
    }

    /** Converts one player snapshot to a transport record. */
    private static NetPlayerState toNet(PlayerState state) {
        return new NetPlayerState(
                state.player(),
                toSimpleCards(state.hand()),
                toSimpleCards(state.bank()),
                toSimplePropertyMap(state.properties()),
                new LinkedHashMap<>(state.houseByColor()),
                new LinkedHashMap<>(state.hotelByColor())
        );
    }

    /** Restores a domain game snapshot from transport data. */
    private static GameState toDomain(NetGameState state) {
        if (state == null) {
            return null;
        }
        List<PlayerState> players = state.players() == null
                ? List.of()
                : state.players().stream().map(ProtocolMapper::toDomain).toList();
        return new GameState(
                state.started(),
                state.gameOver(),
                state.winnerPlayerId(),
                state.currentPlayerId(),
                state.pendingPaymentPayerPlayerId(),
                state.pendingPaymentCollectorPlayerId(),
                state.pendingPaymentAmount(),
                state.pendingPaymentSourceAction(),
                state.jsnResponderPlayerId(),
                state.jsnActorPlayerId(),
                state.jsnTargetPlayerId(),
                state.jsnSourceAction(),
                state.drawPileCount(),
                state.discardPileCount(),
                state.discardPileCards() == null ? List.of() : new ArrayList<>(state.discardPileCards()),
                players,
                state.readyPlayerIds() == null ? java.util.Set.of() : state.readyPlayerIds()
        );
    }

    /** Restores a mutable player state from transport data. */
    private static PlayerState toDomain(NetPlayerState state) {
        PlayerState playerState = new PlayerState(state.player());
        if (state.hand() != null) {
            for (SimpleCard card : state.hand()) {
                playerState.addToHand(card);
            }
        }
        if (state.bank() != null) {
            for (SimpleCard card : state.bank()) {
                playerState.addToBank(card);
            }
        }
        if (state.properties() != null) {
            for (Map.Entry<String, List<SimpleCard>> entry : state.properties().entrySet()) {
                String color = entry.getKey();
                for (SimpleCard card : entry.getValue()) {
                    playerState.addPropertyToExactGroup(color, card);
                }
            }
        }
        if (state.houseByColor() != null) {
            for (Map.Entry<String, Integer> entry : state.houseByColor().entrySet()) {
                int count = entry.getValue() == null ? 0 : entry.getValue();
                for (int i = 0; i < count; i++) {
                    playerState.addHouse(entry.getKey());
                }
            }
        }
        if (state.hotelByColor() != null) {
            for (Map.Entry<String, Integer> entry : state.hotelByColor().entrySet()) {
                int count = entry.getValue() == null ? 0 : entry.getValue();
                for (int i = 0; i < count; i++) {
                    playerState.addHotel(entry.getKey());
                }
            }
        }
        return playerState;
    }

    private static List<SimpleCard> toSimpleCards(List<Card> cards) {
        List<SimpleCard> result = new ArrayList<>();
        for (Card card : cards) {
            result.add(toSimpleCard(card));
        }
        return result;
    }

    private static Map<String, List<SimpleCard>> toSimplePropertyMap(Map<String, List<Card>> properties) {
        Map<String, List<SimpleCard>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Card>> entry : properties.entrySet()) {
            result.put(entry.getKey(), toSimpleCards(entry.getValue()));
        }
        return result;
    }

    private static SimpleCard toSimpleCard(Card card) {
        if (card instanceof SimpleCard simple) {
            return simple;
        }
        CardType type = card.type() == null ? CardType.ACTION : card.type();
        return new SimpleCard(card.name(), type, card.color(), card.bankValue());
    }
}
