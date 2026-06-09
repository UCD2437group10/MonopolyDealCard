package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.card.Card;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Owns the Just Say No chain state and response flow.
 */
final class JustSayNoResolver {

    @FunctionalInterface
    interface PendingEffectApplier {
        void apply(PendingEffectType type, String actorId, Map<String, String> payload, String targetId);
    }

    private final Map<String, PlayerState> players;
    private final Consumer<Card> discardActionCard;
    private final PendingEffectApplier pendingEffectApplier;
    private final Runnable winnerRefresher;
    private final Consumer<PendingJsnState> stateSink;
    private final long timeoutMs;
    private PendingJsnState pendingJsn;

    JustSayNoResolver(
            Map<String, PlayerState> players,
            Consumer<Card> discardActionCard,
            PendingEffectApplier pendingEffectApplier,
            Runnable winnerRefresher,
            Consumer<PendingJsnState> stateSink,
            long timeoutMs
    ) {
        this.players = players;
        this.discardActionCard = discardActionCard;
        this.pendingEffectApplier = pendingEffectApplier;
        this.winnerRefresher = winnerRefresher;
        this.stateSink = stateSink;
        this.timeoutMs = timeoutMs;
    }

    // Open a Just Say No response chain for the given targets.
    void startPendingJsn(String actorId, String actionName, PendingEffectType effectType, Map<String, String> payload, List<String> targets) {
        if (targets == null || targets.isEmpty()) {
            pendingEffectApplier.apply(effectType, actorId, payload, "");
            return;
        }
        pendingJsn = new PendingJsnState(actorId, actionName, effectType, new LinkedHashMap<>(payload), List.copyOf(targets));
        syncState();
        skipAutoPassResponders();
    }

    // Advance expired prompts and throw if rule handling fails.
    void resolveTimeouts() {
        while (pendingJsn != null && System.currentTimeMillis() - pendingJsn.waitingSinceMs() >= timeoutMs) {
            advance(false);
        }
    }

    // Advance expired prompts and clear the chain if recovery is needed.
    void resolveTimeoutsSafely() {
        while (pendingJsn != null && System.currentTimeMillis() - pendingJsn.waitingSinceMs() >= timeoutMs) {
            try {
                advance(false);
            } catch (RuntimeException exception) {
                clearPending();
                winnerRefresher.run();
                break;
            }
        }
    }

    // Apply the current player's Just Say No decision.
    void respond(String playerId, boolean useCard) {
        resolveTimeouts();
        if (pendingJsn == null) {
            throw new IllegalStateException("no pending just-say-no prompt");
        }
        if (!playerId.equals(pendingJsn.waitingPlayerId())) {
            throw new IllegalStateException("not current just-say-no responder");
        }
        advance(useCard);
        resolveTimeouts();
    }

    boolean hasPending() {
        resolveTimeouts();
        return pendingJsn != null;
    }

    private void advance(boolean useCard) {
        if (pendingJsn == null) {
            return;
        }
        String targetId = pendingJsn.targets().get(pendingJsn.targetIndex());
        String responder = pendingJsn.waitingPlayerId();
        if (useCard) {
            removeJustSayNoCard(responder);
            pendingJsn.incrementCurrentTargetUseCount();
            pendingJsn.setWaitingPlayerId(responder.equals(targetId) ? pendingJsn.actorId() : targetId);
            pendingJsn.setWaitingSinceMs(System.currentTimeMillis());
            skipAutoPassResponders();
            return;
        }

        boolean canceled = pendingJsn.currentTargetUseCount() % 2 == 1;
        if (!canceled) {
            pendingEffectApplier.apply(pendingJsn.effectType(), pendingJsn.actorId(), pendingJsn.payload(), targetId);
        }

        pendingJsn.setTargetIndex(pendingJsn.targetIndex() + 1);
        if (pendingJsn.targetIndex() >= pendingJsn.targets().size()) {
            clearPending();
            winnerRefresher.run();
            return;
        }
        pendingJsn.resetCurrentTargetUseCount();
        pendingJsn.setWaitingPlayerId(pendingJsn.targets().get(pendingJsn.targetIndex()));
        pendingJsn.setWaitingSinceMs(System.currentTimeMillis());
        skipAutoPassResponders();
    }

    private void skipAutoPassResponders() {
        while (pendingJsn != null) {
            String responderId = pendingJsn.waitingPlayerId();
            if (!hasJustSayNoCard(responderId)) {
                advance(false);
                continue;
            }
            if (playerState(responderId).player().bot()) {
                advance(true);
                continue;
            }
            break;
        }
    }

    private boolean hasJustSayNoCard(String playerId) {
        if (playerId == null || playerId.isBlank() || !players.containsKey(playerId)) {
            return false;
        }
        for (Card card : playerState(playerId).hand()) {
            if ("Just Say No".equals(card.name())) {
                return true;
            }
        }
        return false;
    }

    private void removeJustSayNoCard(String playerId) {
        PlayerState state = playerState(playerId);
        for (int i = 0; i < state.hand().size(); i++) {
            if ("Just Say No".equals(state.hand().get(i).name())) {
                discardActionCard.accept(state.removeHandCard(i));
                return;
            }
        }
        throw new IllegalStateException(playerId + " has no Just Say No");
    }

    private void clearPending() {
        pendingJsn = null;
        syncState();
    }

    private void syncState() {
        stateSink.accept(pendingJsn);
    }

    private PlayerState playerState(String playerId) {
        PlayerState state = players.get(playerId);
        if (state == null) {
            throw new IllegalArgumentException("unknown player: " + playerId);
        }
        return state;
    }
}
