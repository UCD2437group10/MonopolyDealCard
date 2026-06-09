package edu.group10.monopolydeal.frontend.viewmodel;

import edu.group10.monopolydeal.backend.game.GameState;
import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.PropertySetRules;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Projects backend state into strings and helpers used by the UI.
 */
public class GameViewModel {
    /** Latest full game snapshot received by the frontend. */
    private GameState state;
    /** Ready player ids mirrored from the latest snapshot. */
    private Set<String> readyPlayers = Set.of();
    /** Local player id used for turn checks and lookups. */
    private String currentPlayerId = "";

    /** Replaces the currently displayed state with fresh data. */
    public void update(GameState state, Set<String> readyPlayers, String currentPlayerId) {
        this.state = state;
        this.readyPlayers = readyPlayers == null ? Set.of() : Set.copyOf(readyPlayers);
        this.currentPlayerId = currentPlayerId == null ? "" : currentPlayerId;
    }

    public String title() {
        return "Monopoly Deal";
    }

    /** Returns whether the local player is the current turn owner. */
    public boolean isMyTurn() {
        return state != null && state.currentPlayerId() != null && state.currentPlayerId().equals(currentPlayerId);
    }

    /** Finds the local player's state inside the snapshot. */
    public PlayerState findMe() {
        return findById(currentPlayerId);
    }

    public PlayerState findById(String id) {
        if (state == null || id == null || id.isBlank()) {
            return null;
        }
        for (PlayerState p : state.players()) {
            if (p.player().id().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /** Returns whether every human player is ready to start. */
    public boolean allHumanReady() {
        if (state == null) {
            return false;
        }
        for (PlayerState p : state.players()) {
            if (!p.player().bot() && !readyPlayers.contains(p.player().id())) {
                return false;
            }
        }
        return state.players().size() >= 2;
    }

    public String turnText() {
        String current = state == null ? "unknown" : String.valueOf(state.currentPlayerId());
        return "Turn: current=" + current + " | mine=" + (isMyTurn() ? "yes" : "no");
    }

    public String playersSummaryText() {
        if (state == null) {
            return "No state yet";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Lobby players / ready:\n");
        for (PlayerState p : state.players()) {
            String ready = p.player().bot() ? "BOT" : (readyPlayers.contains(p.player().id()) ? "READY" : "NOT_READY");
            builder.append("- ").append(p.player().id()).append("(").append(p.player().displayName()).append(") => ").append(ready).append("\n");
        }
        builder.append("\nGame: started=").append(state.started())
                .append(", gameOver=").append(state.gameOver())
                .append(", current=").append(state.currentPlayerId())
                .append(", winner=").append(state.winnerPlayerId())
                .append(", drawPile=").append(state.drawPileCount())
                .append(", discardPile=").append(state.discardPileCount())
                .append("\n\n");
        for (PlayerState p : state.players()) {
            builder.append("[").append(p.player().id()).append("] hand=").append(p.hand().size())
                    .append(", bankTotal=").append(p.bankTotal())
                    .append("\nproperties=").append(p.properties())
                    .append("\n\n");
        }
        return builder.toString();
    }

    public int requiredSetSize(String color) {
        return PropertySetRules.requiredSetSize(color);
    }

    public String propertyGroupDetail(String color, List<Card> cards, int house, int hotel) {
        int need = requiredSetSize(color);
        int progress = Math.min(cards.size(), need);
        return "Color group: " + color
                + "\nCompletion: " + progress + "/" + need
                + "\nCards: " + cards.size()
                + "\nHouse: " + house
                + "\nHotel: " + hotel
                + "\n\nCards:\n- "
                + cards.stream().map(card -> card.name() + " ($" + card.bankValue() + ")").collect(Collectors.joining("\n- "));
    }

    public String playerDetail(PlayerState p) {
        String bank = p.bank().isEmpty() ? "None" : p.bank().stream()
                .map(card -> card.name() + "($" + card.bankValue() + ")")
                .collect(Collectors.joining(", "));
        String properties = p.properties().isEmpty() ? "None" : p.properties().entrySet().stream()
                .map(entry -> {
                    String color = entry.getKey();
                    List<Card> cards = entry.getValue();
                    int need = requiredSetSize(color);
                    int progress = Math.min(cards.size(), need);
                    int house = p.houseByColor().getOrDefault(color, 0);
                    int hotel = p.hotelByColor().getOrDefault(color, 0);
                    String names = cards.stream()
                            .map(card -> card.name() + "($" + card.bankValue() + ")")
                            .collect(Collectors.joining(", "));
                    return color + " progress " + progress + "/" + need + "  H:" + house + " T:" + hotel + "\n  - " + names;
                })
                .collect(Collectors.joining("\n"));
        return "Player: " + p.player().displayName() + " (" + p.player().id() + ")"
                + "\nHand cards: " + p.hand().size()
                + "\nBank total: $" + p.bankTotal()
                + "\n\nBank:\n" + bank
                + "\n\nProperties:\n" + properties;
    }
}
