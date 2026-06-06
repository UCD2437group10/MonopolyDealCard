package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.service.CardMoneyRules;
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
 * Main process of the game
 */
public class GameEngine {

    private static final int START_HAND_COUNT = 5;
    private static final int EMPTY_HAND_BONUS_DRAW = 5;
    private static final int TURN_START_DRAW = 2;
    private static final int MAX_ACTION_PER_TURN = 3;
    private static final int END_HAND_LIMIT = 7;
    private final DeckService deckService;
    private final BotTurnService botTurnService = new BotTurnService();
    private final Map<String, PlayerState> players = new LinkedHashMap<>();
    private final Deque<Card> drawPile = new ArrayDeque<>();
    private final Deque<Card> discardPile = new ArrayDeque<>();
    private final List<String> turnOrder = new ArrayList<>();
    private final Set<String> readyPlayerIds = new LinkedHashSet<>();

    private boolean started;
    private boolean gameOver;
    private String winnerPlayerId = "";
    private String hostPlayerId = "";
    private int turnIndex;
    private int actionUsed;
    private PendingJsn pendingJsn;

    private static final long JSN_TIMEOUT_MS = 10_000L;

    private enum PendingEffectType {
        DEBT_COLLECTOR,
        SLY_DEAL,
        FORCED_DEAL,
        DEAL_BREAKER,
        ITS_MY_BIRTHDAY
    }

    private static final class PendingJsn {
        private final String actorId;
        private final String sourceAction;
        private final PendingEffectType effectType;
        private final Map<String, String> payload;
        private final List<String> targets;
        private int targetIndex;
        private String waitingPlayerId;
        private int currentTargetUseCount;
        private long waitingSinceMs;

        private PendingJsn(String actorId, String sourceAction, PendingEffectType effectType, Map<String, String> payload, List<String> targets) {
            this.actorId = actorId;
            this.sourceAction = sourceAction;
            this.effectType = effectType;
            this.payload = payload;
            this.targets = targets;
            this.targetIndex = 0;
            this.waitingPlayerId = targets.isEmpty() ? "" : targets.get(0);
            this.currentTargetUseCount = 0;
            this.waitingSinceMs = System.currentTimeMillis();
        }
    }

    public GameEngine(DeckService deckService) {
        this.deckService = deckService;
    }

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

        for (String playerId : turnOrder) {
            PlayerState playerState = playerState(playerId);
            while (!playerState.hand().isEmpty()) {
                discardPile.push(playerState.removeHandCard(0));
            }
            drawCardsInternal(playerId, START_HAND_COUNT);
        }

        started = true;
        gameOver = false;
        winnerPlayerId = "";
        turnIndex = 0;
        actionUsed = 0;
        applyStartOfTurnDraw();
    }

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

    public void playRentCard(String playerId, int handIndex, String targetPlayerId, String colorChoice, int doubleRentCount) {
        ensureTurnAction(playerId);
        Card rentCard = takeHandCard(playerId, handIndex);
        if (rentCard.type() != CardType.RENT) {
            throw new IllegalArgumentException("card is not rent card");
        }

        int rent = calculateRent(playerId, rentCard, colorChoice);
        if (doubleRentCount > 0) {
            consumeDoubleRent(playerId, doubleRentCount);
            rent = rent * (1 << doubleRentCount);
        }

        // All two-color rent cards affect every opponent; wild rent remains single target.
        if (rentCard.color() != null && rentCard.color().contains("/")) {
            for (String opponentId : turnOrder) {
                if (opponentId.equals(playerId)) {
                    continue;
                }
                transferPayment(opponentId, playerId, rent);
            }
        } else {
            if (playerId.equals(targetPlayerId)) {
                throw new IllegalArgumentException("target player must be another player");
            }
            transferPayment(targetPlayerId, playerId, rent);
        }

        discardPile.push(rentCard);
        actionUsed++;
        refreshWinner();
    }

    public void playActionCard(String playerId, int handIndex, Map<String, String> payload) {
        ensureTurnAction(playerId);
        Card actionCard = takeHandCard(playerId, handIndex);
        if (actionCard.type() != CardType.ACTION) {
            throw new IllegalArgumentException("card is not action card");
        }

        try {
            String targetId = payload.getOrDefault("targetPlayerId", "");
            switch (actionCard.name()) {
                case "Pass Go" -> drawCardsInternal(playerId, 2);
                case "Debt Collector" -> startPendingJsn(playerId, actionCard.name(), PendingEffectType.DEBT_COLLECTOR, payload, List.of(targetId));
                case "It's My Birthday" -> {
                    List<String> targets = turnOrder.stream().filter(id -> !id.equals(playerId)).toList();
                    startPendingJsn(playerId, actionCard.name(), PendingEffectType.ITS_MY_BIRTHDAY, payload, targets);
                }
                case "Sly Deal" -> startPendingJsn(playerId, actionCard.name(), PendingEffectType.SLY_DEAL, payload, List.of(targetId));
                case "Forced Deal" -> startPendingJsn(playerId, actionCard.name(), PendingEffectType.FORCED_DEAL, payload, List.of(targetId));
                case "Deal Breaker" -> startPendingJsn(playerId, actionCard.name(), PendingEffectType.DEAL_BREAKER, payload, List.of(targetId));
                case "House" -> addHouse(playerId, payload.getOrDefault("color", ""));
                case "Hotel" -> addHotel(playerId, payload.getOrDefault("color", ""));
                case "Just Say No", "Double The Rent" -> throw new IllegalArgumentException(actionCard.name() + " can only be used reactively");
                default -> throw new IllegalArgumentException("unsupported action card: " + actionCard.name());
            }
            discardPile.push(actionCard);
            actionUsed++;
            resolvePendingJsnTimeouts();
            if (pendingJsn == null) {
                refreshWinner();
            }
        } catch (RuntimeException exception) {
            playerState(playerId).addToHand(actionCard);
            throw exception;
        }
    }

    public void respondJustSayNo(String playerId, boolean useCard) {
        resolvePendingJsnTimeouts();
        if (pendingJsn == null) {
            throw new IllegalStateException("no pending just-say-no prompt");
        }
        if (!playerId.equals(pendingJsn.waitingPlayerId)) {
            throw new IllegalStateException("not current just-say-no responder");
        }
        advancePendingJsn(useCard);
        resolvePendingJsnTimeouts();
    }

    public void endTurn(String playerId) {
        ensureTurnAlive(playerId);
        if (playerState(playerId).hand().size() > END_HAND_LIMIT) {
            throw new IllegalStateException("hand size must be <= 7 before end turn");
        }
        turnIndex = (turnIndex + 1) % turnOrder.size();
        actionUsed = 0;
        applyStartOfTurnDraw();
    }

    public GameState snapshot() {
        resolvePendingJsnTimeouts();
        String currentPlayer = turnOrder.isEmpty() ? "" : turnOrder.get(turnIndex);
        return new GameState(
                started,
                gameOver,
                winnerPlayerId,
                currentPlayer,
                pendingJsn == null ? "" : pendingJsn.waitingPlayerId,
                pendingJsn == null ? "" : pendingJsn.actorId,
                pendingJsn == null || pendingJsn.targets.isEmpty() ? "" : pendingJsn.targets.get(pendingJsn.targetIndex),
                pendingJsn == null ? "" : pendingJsn.sourceAction,
                drawPile.size(),
                discardPile.size(),
                List.copyOf(players.values()),
                Set.copyOf(readyPlayerIds)
        );
    }

    public void playBotTurn(String playerId) {
        ensureTurnAlive(playerId);
        PlayerState bot = playerState(playerId);
        if (!bot.player().bot()) {
            throw new IllegalArgumentException("player is not bot");
        }
        botTurnService.playTurn(this, playerId);
    }

    private void ensureTurnAction(String playerId) {
        resolvePendingJsnTimeouts();
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

    private void drawCardsInternal(String playerId, int count) {
        for (int i = 0; i < count; i++) {
            if (drawPile.isEmpty()) {
                reshuffleIfNeeded();
            }
            if (drawPile.isEmpty()) {
                throw new IllegalStateException("no cards left");
            }
            playerState(playerId).addToHand(drawPile.pop());
        }
        applyHandOverflow(playerId);
    }

    private void applyStartOfTurnDraw() {
        if (turnOrder.isEmpty()) {
            return;
        }
        String current = turnOrder.get(turnIndex);
        int count = playerState(current).hand().isEmpty() ? EMPTY_HAND_BONUS_DRAW : TURN_START_DRAW;
        drawCardsInternal(current, count);
    }

    private void applyHandOverflow(String playerId) {
        PlayerState state = playerState(playerId);
        while (state.hand().size() > END_HAND_LIMIT) {
            discardPile.push(state.removeHandCard(state.hand().size() - 1));
        }
    }

    private String resolvePropertyColor(Card card, String colorChoice) {
        if (card.type() == CardType.PROPERTY) {
            return card.color();
        }
        if ("Wild".equals(card.color())) {
            if (colorChoice == null || colorChoice.isBlank()) {
                throw new IllegalArgumentException("wild property requires colorChoice");
            }
            return colorChoice;
        }
        String[] choices = card.color().split("/");
        if (colorChoice == null || colorChoice.isBlank()) {
            throw new IllegalArgumentException("multi property requires colorChoice");
        }
        for (String choice : choices) {
            if (choice.equals(colorChoice)) {
                return colorChoice;
            }
        }
        throw new IllegalArgumentException("invalid colorChoice for card");
    }

    private int calculateRent(String ownerId, Card rentCard, String colorChoice) {
        String color = resolveRentColor(rentCard, colorChoice, ownerId);
        int count = playerState(ownerId).propertyCount(color);
        if (count == 0) {
            throw new IllegalStateException("no property in chosen color");
        }
        int baseRent = baseRentByColorAndCount(color, count);
        if (playerState(ownerId).hasHouse(color)) {
            baseRent += 3;
        }
        if (playerState(ownerId).hasHotel(color)) {
            baseRent += 4;
        }
        return baseRent;
    }

    private int baseRentByColorAndCount(String color, int count) {
        return switch (color) {
            case "Brown" -> count >= 2 ? 2 : 1;
            case "Light Blue", "Pink", "Orange", "Red", "Yellow", "Green" -> {
                if (count == 1) {
                    yield 1;
                }
                if (count == 2) {
                    yield 2;
                }
                yield 4;
            }
            case "Deep Blue" -> count >= 2 ? 4 : 2;
            case "Railroad" -> Math.min(count, 4);
            case "Utility" -> count >= 2 ? 2 : 1;
            default -> throw new IllegalArgumentException("unsupported rent color: " + color);
        };
    }

    private String resolveRentColor(Card rentCard, String colorChoice, String ownerId) {
        if (rentCard.color().contains("/")) {
            if (colorChoice == null || colorChoice.isBlank()) {
                throw new IllegalArgumentException("rent card requires colorChoice");
            }
            String[] colors = rentCard.color().split("/");
            for (String color : colors) {
                if (color.equals(colorChoice)) {
                    return colorChoice;
                }
            }
            throw new IllegalArgumentException("invalid colorChoice");
        }
        if ("Any".equals(rentCard.color())) {
            if (colorChoice == null || colorChoice.isBlank()) {
                throw new IllegalArgumentException("wild rent requires colorChoice");
            }
            if (!playerState(ownerId).hasProperty(colorChoice)) {
                throw new IllegalArgumentException("player has no such color property");
            }
            return colorChoice;
        }
        return rentCard.color();
    }

    private void consumeDoubleRent(String playerId, int count) {
        PlayerState state = playerState(playerId);
        for (int i = 0; i < count; i++) {
            int index = findCardIndexByName(state.hand(), "Double The Rent");
            if (index < 0) {
                throw new IllegalStateException("not enough Double The Rent cards");
            }
            discardPile.push(state.removeHandCard(index));
        }
    }

    private int findCardIndexByName(List<Card> cards, String cardName) {
        for (int i = 0; i < cards.size(); i++) {
            if (cardName.equals(cards.get(i).name())) {
                return i;
            }
        }
        return -1;
    }

    private void startPendingJsn(String actorId, String actionName, PendingEffectType effectType, Map<String, String> payload, List<String> targets) {
        if (targets == null || targets.isEmpty()) {
            applyPendingEffectForTarget(effectType, actorId, payload, "");
            return;
        }
        pendingJsn = new PendingJsn(actorId, actionName, effectType, new LinkedHashMap<>(payload), List.copyOf(targets));
        skipAutoPassResponders();
    }

    private void resolvePendingJsnTimeouts() {
        while (pendingJsn != null && System.currentTimeMillis() - pendingJsn.waitingSinceMs >= JSN_TIMEOUT_MS) {
            advancePendingJsn(false);
        }
    }

    private void advancePendingJsn(boolean useCard) {
        if (pendingJsn == null) {
            return;
        }
        String targetId = pendingJsn.targets.get(pendingJsn.targetIndex);
        String responder = pendingJsn.waitingPlayerId;
        if (useCard) {
            removeJustSayNoCard(responder);
            pendingJsn.currentTargetUseCount++;
            pendingJsn.waitingPlayerId = responder.equals(targetId) ? pendingJsn.actorId : targetId;
            pendingJsn.waitingSinceMs = System.currentTimeMillis();
            skipAutoPassResponders();
            return;
        }

        boolean canceled = pendingJsn.currentTargetUseCount % 2 == 1;
        if (!canceled) {
            applyPendingEffectForTarget(pendingJsn.effectType, pendingJsn.actorId, pendingJsn.payload, targetId);
        }

        pendingJsn.targetIndex++;
        if (pendingJsn.targetIndex >= pendingJsn.targets.size()) {
            pendingJsn = null;
            refreshWinner();
            return;
        }
        pendingJsn.currentTargetUseCount = 0;
        pendingJsn.waitingPlayerId = pendingJsn.targets.get(pendingJsn.targetIndex);
        pendingJsn.waitingSinceMs = System.currentTimeMillis();
        skipAutoPassResponders();
    }

    private void skipAutoPassResponders() {
        while (pendingJsn != null) {
            if (!hasJustSayNoCard(pendingJsn.waitingPlayerId)) {
                advancePendingJsn(false);
                continue;
            }
            break;
        }
    }

    private boolean hasJustSayNoCard(String playerId) {
        if (playerId == null || playerId.isBlank() || !players.containsKey(playerId)) {
            return false;
        }
        return findCardIndexByName(playerState(playerId).hand(), "Just Say No") >= 0;
    }

    private void applyPendingEffectForTarget(PendingEffectType type, String actorId, Map<String, String> payload, String targetId) {
        switch (type) {
            case DEBT_COLLECTOR -> transferPayment(targetId, actorId, 5);
            case SLY_DEAL -> {
                String color = payload.getOrDefault("color", "");
                int propertyIndex = Integer.parseInt(payload.getOrDefault("propertyIndex", "0"));
                stealSingleProperty(actorId, targetId, color, propertyIndex);
            }
            case FORCED_DEAL -> {
                String myColor = payload.getOrDefault("myColor", "");
                String targetColor = payload.getOrDefault("targetColor", "");
                int myIndex = Integer.parseInt(payload.getOrDefault("myIndex", "0"));
                int targetIndex = Integer.parseInt(payload.getOrDefault("targetIndex", "0"));
                forcedSwapProperty(actorId, targetId, myColor, myIndex, targetColor, targetIndex);
            }
            case DEAL_BREAKER -> {
                String color = payload.getOrDefault("color", "");
                stealCompleteSet(actorId, targetId, color);
            }
            case ITS_MY_BIRTHDAY -> transferPayment(targetId, actorId, 2);
        }
    }

    private void removeJustSayNoCard(String playerId) {
        PlayerState state = playerState(playerId);
        int index = findCardIndexByName(state.hand(), "Just Say No");
        if (index < 0) {
            throw new IllegalStateException(playerId + " has no Just Say No");
        }
        discardPile.push(state.removeHandCard(index));
    }

    private void transferPayment(String fromId, String toId, int amount) {
        if (amount <= 0) {
            return;
        }
        PlayerState from = playerState(fromId);
        PlayerState to = playerState(toId);

        int remain = amount;
        List<Card> paidBank = from.drainBankForPayment(remain);
        for (Card card : paidBank) {
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

    private void stealSingleProperty(String actorId, String targetId, String color, int propertyIndex) {
        if (actorId.equals(targetId)) {
            throw new IllegalArgumentException("target must be another player");
        }
        PlayerState target = playerState(targetId);
        if (isCompleteSet(target, color)) {
            throw new IllegalStateException("cannot steal from complete set");
        }
        Card card = target.removeProperty(color, propertyIndex);
        playerState(actorId).addProperty(color, card);
    }

    private void forcedSwapProperty(String actorId, String targetId, String myColor, int myIndex, String targetColor, int targetIndex) {
        if (actorId.equals(targetId)) {
            throw new IllegalArgumentException("target must be another player");
        }
        if (isCompleteSet(playerState(actorId), myColor) || isCompleteSet(playerState(targetId), targetColor)) {
            throw new IllegalStateException("forced deal cannot use complete-set property");
        }
        PlayerState actor = playerState(actorId);
        PlayerState target = playerState(targetId);
        Card mine = actor.removeProperty(myColor, myIndex);
        Card targetCard = target.removeProperty(targetColor, targetIndex);
        actor.addProperty(targetColor, targetCard);
        target.addProperty(myColor, mine);
    }

    private void stealCompleteSet(String actorId, String targetId, String color) {
        if (actorId.equals(targetId)) {
            throw new IllegalArgumentException("target must be another player");
        }
        PlayerState target = playerState(targetId);
        PlayerState actor = playerState(actorId);
        if (!isCompleteSet(target, color)) {
            throw new IllegalStateException("target color is not complete set");
        }

        actor.setAllProperties(color, target.removeAllProperties(color));
        if (target.clearHouse(color) > 0) {
            actor.addHouse(color);
        }
        if (target.clearHotel(color) > 0) {
            actor.addHotel(color);
        }
    }

    private void addHouse(String playerId, String color) {
        PlayerState playerState = playerState(playerId);
        if ("Railroad".equals(color) || "Utility".equals(color)) {
            throw new IllegalArgumentException("house cannot be used on Railroad/Utility");
        }
        if (!isCompleteSet(playerState, color)) {
            throw new IllegalStateException("house requires complete set");
        }
        playerState.addHouse(color);
    }

    private void addHotel(String playerId, String color) {
        PlayerState playerState = playerState(playerId);
        if ("Railroad".equals(color) || "Utility".equals(color)) {
            throw new IllegalArgumentException("hotel cannot be used on Railroad/Utility");
        }
        if (!isCompleteSet(playerState, color)) {
            throw new IllegalStateException("hotel requires complete set");
        }
        playerState.addHotel(color);
    }

    private void refreshWinner() {
        for (String playerId : turnOrder) {
            PlayerState ps = playerState(playerId);
            int fullSet = 0;
            for (String color : ps.properties().keySet()) {
                if (isCompleteSet(ps, color)) {
                    fullSet++;
                }
            }
            if (fullSet >= 3) {
                gameOver = true;
                winnerPlayerId = playerId;
                return;
            }
        }
    }

    private boolean isCompleteSet(PlayerState playerState, String color) {
        int need = requiredSetSize(color);
        return playerState.propertyCount(color) >= need;
    }

    private int requiredSetSize(String color) {
        return switch (color) {
            case "Brown", "Deep Blue", "Utility" -> 2;
            case "Railroad" -> 4;
            default -> 3;
        };
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

    private void reshuffleIfNeeded() {
        if (discardPile.isEmpty()) {
            return;
        }
        while (!discardPile.isEmpty()) {
            drawPile.push(discardPile.pop());
        }
    }

}
