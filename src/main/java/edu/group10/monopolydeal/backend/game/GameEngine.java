package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.service.CardMoneyRules;
import edu.group10.monopolydeal.backend.service.CardPropertyRules;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import edu.group10.monopolydeal.backend.service.DeckService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements the main Monopoly Deal game flow and rule enforcement.
 */
public class GameEngine {

    /** Initial hand size when a game starts. */
    private static final int START_HAND_COUNT = 5;
    /** Extra draw count when a player starts a turn with no hand cards. */
    private static final int EMPTY_HAND_BONUS_DRAW = 5;
    /** Normal draw count at the start of a turn. */
    private static final int TURN_START_DRAW = 2;
    /** Maximum number of card actions allowed per turn. */
    private static final int MAX_ACTION_PER_TURN = 3;
    /** Maximum hand size allowed before ending a turn. */
    private static final int END_HAND_LIMIT = 7;
    /** Builds and shuffles decks for new games. */
    private final DeckService deckService;
    /** Simple AI helper used for local bot players. */
    private final BotTurnService botTurnService = new BotTurnService();
    /** Player states keyed by player id. */
    private final Map<String, PlayerState> players = new LinkedHashMap<>();
    /** Draw pile used during normal play. */
    private final Deque<Card> drawPile = new ArrayDeque<>();
    /** Discard pile used for action cards and reshuffles. */
    private final Deque<Card> discardPile = new ArrayDeque<>();
    /** Fixed player order for turns and targeting. */
    private final List<String> turnOrder = new ArrayList<>();
    /** Ready flags collected before the game starts. */
    private final Set<String> readyPlayerIds = new LinkedHashSet<>();
    /** Handles draws and turn progression. */
    private final TurnManager turnManager = new TurnManager(
            players,
            drawPile,
            discardPile,
            turnOrder,
            EMPTY_HAND_BONUS_DRAW,
            TURN_START_DRAW,
            END_HAND_LIMIT
    );
    /** Checks completed sets and win conditions. */
    private final VictoryManager victoryManager = new VictoryManager();
    /** Resolves money and asset payments. */
    private final PaymentResolver paymentResolver = new PaymentResolver(players);
    /** Resolves property-stealing and swapping actions. */
    private final PropertyActionResolver propertyActionResolver = new PropertyActionResolver(players, victoryManager);
    /** Resolves rent amounts and Double The Rent handling. */
    private final RentResolver rentResolver = new RentResolver(players, discardPile);
    /** Owns the Just Say No chain state. */
    private final JustSayNoResolver justSayNoResolver = new JustSayNoResolver(
            players,
            this::addToDiscardPileIfAction,
            this::applyPendingEffectForTarget,
            this::refreshWinner,
            pending -> this.pendingJsn = pending,
            JSN_TIMEOUT_MS
    );
    /** Dispatches action cards to dedicated rule handlers. */
    private final ActionResolutionService actionResolutionService = new ActionResolutionService(
            turnManager,
            justSayNoResolver,
            this::addHouse,
            this::addHotel
    );

    private boolean started;
    private boolean gameOver;
    private String winnerPlayerId = "";
    private String hostPlayerId = "";
    private int turnIndex;
    private int actionUsed;
    private PendingJsnState pendingJsn;

    /** Timeout used by the Just Say No response window. */
    private static final long JSN_TIMEOUT_MS = 10_000L;

    /** Creates a game engine backed by the given deck service. */
    public GameEngine(DeckService deckService) {
        this.deckService = deckService;
    }

    /** Adds a player before the game has started. */
    public void addPlayer(Player player) {
        if (started) {
            throw new IllegalStateException("game already started");
        }
        if (players.containsKey(player.id())) {
            throw new IllegalArgumentException("player exists: " + player.id());
        }
        String joiningName = player.displayName() == null ? "" : player.displayName().trim();
        for (PlayerState existing : players.values()) {
            String existingName = existing.player().displayName() == null ? "" : existing.player().displayName().trim();
            if (!joiningName.isBlank() && joiningName.equalsIgnoreCase(existingName)) {
                throw new IllegalArgumentException("player name exists: " + player.displayName());
            }
        }
        players.put(player.id(), new PlayerState(player));
        turnOrder.add(player.id());
        if (hostPlayerId.isBlank()) {
            hostPlayerId = player.id();
        }
    }

    /** Clears all runtime match state and returns to lobby state. */
    public void resetGame() {
        players.clear();
        drawPile.clear();
        discardPile.clear();
        turnOrder.clear();
        readyPlayerIds.clear();
        started = false;
        gameOver = false;
        winnerPlayerId = "";
        hostPlayerId = "";
        turnIndex = 0;
        actionUsed = 0;
        pendingJsn = null;
    }

    /** Starts a new match after lobby validation succeeds. */
    public void startGame(String operatorId) {
        if (started) {
            throw new IllegalStateException("game already started");
        }
        if (hostPlayerId.isBlank()) {
            throw new IllegalStateException("no host player");
        }
        if (!hostPlayerId.equals(operatorId)) {
            throw new IllegalStateException("only host can start game");
        }
        if (players.size() < 2) {
            throw new IllegalStateException("need at least 2 players");
        }
        for (String playerId : turnOrder) {
            Player player = playerState(playerId).player();
            if (!player.bot() && !readyPlayerIds.contains(playerId)) {
                throw new IllegalStateException("all non-bot players must be ready");
            }
        }

        drawPile.clear();
        discardPile.clear();
        drawPile.addAll(deckService.createDeck());
        turnManager.dealOpeningHands(START_HAND_COUNT);

        started = true;
        gameOver = false;
        winnerPlayerId = "";
        turnIndex = 0;
        actionUsed = 0;
        turnManager.applyStartOfTurnDraw(turnIndex);
    }

    /** Updates one player's ready status in the lobby. */
    public void setReady(String playerId, boolean ready) {
        if (!players.containsKey(playerId)) {
            throw new IllegalArgumentException("unknown player: " + playerId);
        }
        if (ready) {
            readyPlayerIds.add(playerId);
            return;
        }
        readyPlayerIds.remove(playerId);
    }

    /** Plays a bankable card from hand into the bank area. */
    public void playMoneyCard(String playerId, int handIndex) {
        ensureTurnAction(playerId);
        PlayerState state = playerState(playerId);
        if (handIndex < 0 || handIndex >= state.hand().size()) {
            throw new IllegalArgumentException("invalid hand index");
        }
        Card card = state.hand().get(handIndex);
        CardMoneyRules.validateBankable(card);
        card = takeHandCard(playerId, handIndex);
        playerState(playerId).addToBank(card);
        actionUsed++;
    }

    /** Plays a property or multi-property card from hand. */
    public void playPropertyCard(String playerId, int handIndex, String colorChoice) {
        ensureTurnAction(playerId);
        Card card = takeHandCard(playerId, handIndex);
        if (card.type() != CardType.PROPERTY && card.type() != CardType.MULTI_PROPERTY) {
            throw new IllegalArgumentException("card is not property type");
        }
        String color = resolvePropertyColor(card, colorChoice);
        playerState(playerId).addProperty(color, card);
        actionUsed++;
        refreshWinner();
    }

    /** Changes the active color of a previously played multi-property card. */
    public void changePropertyColor(String playerId, String fromColor, int propertyIndex, String colorChoice) {
        justSayNoResolver.resolveTimeouts();
        ensureTurnAlive(playerId);
        PlayerState state = playerState(playerId);
        List<Card> group = state.properties().get(fromColor);
        if (group == null || propertyIndex < 0 || propertyIndex >= group.size()) {
            throw new IllegalArgumentException("invalid property index");
        }
        Card card = group.get(propertyIndex);
        if (card.type() != CardType.MULTI_PROPERTY) {
            throw new IllegalArgumentException("only multi-property cards can change color");
        }
        String targetColor = resolvePropertyColor(card, colorChoice);
        if (targetColor.equals(fromColor)) {
            throw new IllegalArgumentException("property is already in that color group");
        }
        if ((state.hasHouse(fromColor) || state.hasHotel(fromColor))
                && state.propertyCount(fromColor) - 1 < victoryManager.requiredSetSize(fromColor)) {
            throw new IllegalStateException("cannot move property out of a built complete set");
        }
        state.moveProperty(fromColor, propertyIndex, targetColor);
        refreshWinner();
    }

    /** Plays a rent card and resolves its payment effect. */
    public void playRentCard(String playerId, int handIndex, String targetPlayerId, String colorChoice, int doubleRentCount) {
        ensureTurnAction(playerId);
        Card rentCard = takeHandCard(playerId, handIndex);
        if (rentCard.type() != CardType.RENT) {
            throw new IllegalArgumentException("card is not rent card");
        }

        int rent = rentResolver.calculateRent(playerId, rentCard, colorChoice);
        if (doubleRentCount > 0) {
            rentResolver.consumeDoubleRent(playerId, doubleRentCount);
            rent = rent * (1 << doubleRentCount);
        }

        // Two-color rent cards affect every opponent, while wild rent stays single-target.
        if (rentCard.color() != null && rentCard.color().contains("/")) {
            for (String opponentId : turnOrder) {
                if (opponentId.equals(playerId)) {
                    continue;
                }
                paymentResolver.transferPayment(opponentId, playerId, rent);
            }
        } else {
            if (playerId.equals(targetPlayerId)) {
                throw new IllegalArgumentException("target player must be another player");
            }
            paymentResolver.transferPayment(targetPlayerId, playerId, rent);
        }

        actionUsed++;
        refreshWinner();
    }

    /** Plays an action card and applies its rule-specific effect. */
    public void playActionCard(String playerId, int handIndex, Map<String, String> payload) {
        ensureTurnAction(playerId);
        Card actionCard = takeHandCard(playerId, handIndex);
        if (actionCard.type() != CardType.ACTION) {
            throw new IllegalArgumentException("card is not action card");
        }

        try {
            actionResolutionService.resolve(playerId, actionCard, payload, turnOrder);
            addToDiscardPileIfAction(actionCard);
            actionUsed++;
            justSayNoResolver.resolveTimeouts();
            if (pendingJsn == null) {
                refreshWinner();
            }
        } catch (RuntimeException exception) {
            playerState(playerId).addToHand(actionCard);
            throw exception;
        }
    }

    /** Submits a Just Say No response for the current waiting player. */
    public void respondJustSayNo(String playerId, boolean useCard) {
        justSayNoResolver.respond(playerId, useCard);
    }

    /** Ends the current turn after validating hand size. */
    public void endTurn(String playerId) {
        ensureTurnAlive(playerId);
        turnIndex = turnManager.advanceTurn(playerId, turnIndex);
        actionUsed = 0;
    }

    /** Builds a UI-friendly snapshot of the current engine state. */
    public GameState snapshot() {
        String currentPlayer = turnOrder.isEmpty() ? "" : turnOrder.get(turnIndex);
        return new GameState(
                started,
                gameOver,
                winnerPlayerId,
                currentPlayer,
                pendingJsn == null ? "" : pendingJsn.waitingPlayerId(),
                pendingJsn == null ? "" : pendingJsn.actorId(),
                pendingJsn == null || pendingJsn.targets().isEmpty() ? "" : pendingJsn.targets().get(pendingJsn.targetIndex()),
                pendingJsn == null ? "" : pendingJsn.sourceAction(),
                drawPile.size(),
                discardPile.size(),
                List.copyOf(discardPile),
                List.copyOf(players.values()),
                Set.copyOf(readyPlayerIds)
        );
    }

    /**
     * Advances timeout-based background state in a safe way, then returns a snapshot.
     * This keeps UI polling from crashing when a delayed effect can no longer be applied.
     */
    public GameState pollStateSnapshot() {
        justSayNoResolver.resolveTimeoutsSafely();
        return snapshot();
    }

    /** Lets an automated player complete its turn. */
    public void playBotTurn(String playerId) {
        ensureTurnAlive(playerId);
        PlayerState bot = playerState(playerId);
        if (!bot.player().bot()) {
            throw new IllegalArgumentException("player is not bot");
        }
        botTurnService.playTurn(this, playerId);
    }

    private void ensureTurnAction(String playerId) {
        justSayNoResolver.resolveTimeouts();
        if (pendingJsn != null) {
            throw new IllegalStateException("pending just-say-no response");
        }
        ensureTurnAlive(playerId);
        if (actionUsed >= MAX_ACTION_PER_TURN) {
            throw new IllegalStateException("max 3 actions per turn");
        }
    }

    private void ensureTurnAlive(String playerId) {
        ensureStarted();
        if (gameOver) {
            throw new IllegalStateException("game is over");
        }
        ensureCurrentPlayer(playerId);
    }

    private Card takeHandCard(String playerId, int handIndex) {
        PlayerState state = playerState(playerId);
        if (handIndex < 0 || handIndex >= state.hand().size()) {
            throw new IllegalArgumentException("invalid hand index");
        }
        return state.removeHandCard(handIndex);
    }

    private String resolvePropertyColor(Card card, String colorChoice) {
        return CardPropertyRules.resolvePropertyColor(card, colorChoice);
    }

    private void applyPendingEffectForTarget(PendingEffectType type, String actorId, Map<String, String> payload, String targetId) {
        switch (type) {
            case DEBT_COLLECTOR -> paymentResolver.transferPayment(targetId, actorId, 5);
            case SLY_DEAL -> {
                String color = payload.getOrDefault("color", "");
                int propertyIndex = Integer.parseInt(payload.getOrDefault("propertyIndex", "0"));
                propertyActionResolver.stealSingleProperty(actorId, targetId, color, propertyIndex);
            }
            case FORCED_DEAL -> {
                String myColor = payload.getOrDefault("myColor", "");
                String targetColor = payload.getOrDefault("targetColor", "");
                int myIndex = Integer.parseInt(payload.getOrDefault("myIndex", "0"));
                int targetIndex = Integer.parseInt(payload.getOrDefault("targetIndex", "0"));
                propertyActionResolver.forcedSwapProperty(actorId, targetId, myColor, myIndex, targetColor, targetIndex);
            }
            case DEAL_BREAKER -> {
                String color = payload.getOrDefault("color", "");
                propertyActionResolver.stealCompleteSet(actorId, targetId, color);
            }
            case ITS_MY_BIRTHDAY -> paymentResolver.transferPayment(targetId, actorId, 2);
        }
    }

    private void addToDiscardPileIfAction(Card card) {
        if (card != null && card.type() == CardType.ACTION) {
            discardPile.push(card);
        }
    }

    boolean hasPendingJsn() {
        return justSayNoResolver.hasPending();
    }

    private void addHouse(String playerId, String color) {
        PlayerState playerState = playerState(playerId);
        if ("Railroad".equals(color) || "Utility".equals(color)) {
            throw new IllegalArgumentException("house cannot be used on Railroad/Utility");
        }
        if (!victoryManager.isCompleteSet(playerState, color)) {
            throw new IllegalStateException("house requires complete set");
        }
        playerState.addHouse(color);
    }

    private void addHotel(String playerId, String color) {
        PlayerState playerState = playerState(playerId);
        if ("Railroad".equals(color) || "Utility".equals(color)) {
            throw new IllegalArgumentException("hotel cannot be used on Railroad/Utility");
        }
        if (!playerState.hasHouse(color)) {
            throw new IllegalStateException("hotel requires house first");
        }
        if (!victoryManager.isCompleteSet(playerState, color)) {
            throw new IllegalStateException("hotel requires complete set");
        }
        if (playerState.hasHotel(color)) {
            throw new IllegalStateException("hotel already exists on this set");
        }
        playerState.addHotel(color);
    }

    private void refreshWinner() {
        String winner = victoryManager.findWinner(turnOrder, players);
        if (winner.isBlank()) {
            gameOver = false;
            winnerPlayerId = "";
            return;
        }
        gameOver = true;
        winnerPlayerId = winner;
    }

    PlayerState playerState(String playerId) {
        PlayerState state = players.get(playerId);
        if (state == null) {
            throw new IllegalArgumentException("unknown player: " + playerId);
        }
        return state;
    }

    List<String> turnOrder() {
        return List.copyOf(turnOrder);
    }

    private void ensureStarted() {
        if (!started) {
            throw new IllegalStateException("game not started");
        }
    }

    private void ensureCurrentPlayer(String playerId) {
        if (!playerId.equals(turnOrder.get(turnIndex))) {
            throw new IllegalStateException("not current player's turn");
        }
    }
}
