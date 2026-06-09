package edu.group10.monopolydeal.backend.game;

import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Orchestrates payment prompts so humans pay manually and bots pay automatically.
 */
final class PaymentRequestResolver {

    private final Map<String, PlayerState> players;
    private final PaymentResolver paymentResolver;
    private final Runnable winnerRefresher;
    private final Consumer<PendingPaymentState> stateSink;

    private PendingPaymentState pendingPayment;

    PaymentRequestResolver(Map<String, PlayerState> players,
                           PaymentResolver paymentResolver,
                           Runnable winnerRefresher,
                           Consumer<PendingPaymentState> stateSink) {
        this.players = players;
        this.paymentResolver = paymentResolver;
        this.winnerRefresher = winnerRefresher;
        this.stateSink = stateSink;
    }

    void requestPayments(String collectorPlayerId, int amount, String sourceAction, List<String> payerIds) {
        if (amount <= 0 || payerIds == null || payerIds.isEmpty()) {
            clearPending();
            return;
        }
        pendingPayment = new PendingPaymentState(collectorPlayerId, sourceAction, amount, payerIds);
        syncState();
        advanceUntilHumanOrDone();
    }

    void submitSelection(String payerId, PaymentSelection selection) {
        if (pendingPayment == null) {
            throw new IllegalStateException("no pending payment");
        }
        if (!pendingPayment.waitingPlayerId().equals(payerId)) {
            throw new IllegalStateException("not waiting for this player's payment");
        }
        paymentResolver.transferSelectedPayment(payerId, pendingPayment.collectorPlayerId(), pendingPayment.amount(), selection);
        winnerRefresher.run();
        moveToNextPayer();
        advanceUntilHumanOrDone();
    }

    boolean hasPending() {
        return pendingPayment != null;
    }

    private void advanceUntilHumanOrDone() {
        while (pendingPayment != null) {
            PlayerState payer = playerState(pendingPayment.waitingPlayerId());
            if (paymentResolver.totalAssetValue(payer) <= 0) {
                moveToNextPayer();
                continue;
            }
            if (payer.player().bot()) {
                paymentResolver.transferAutomaticPayment(
                        payer.player().id(),
                        pendingPayment.collectorPlayerId(),
                        pendingPayment.amount()
                );
                winnerRefresher.run();
                moveToNextPayer();
                continue;
            }
            syncState();
            return;
        }
    }

    private void moveToNextPayer() {
        if (pendingPayment == null) {
            return;
        }
        if (pendingPayment.advanceToNextPayer()) {
            syncState();
            return;
        }
        clearPending();
    }

    private void clearPending() {
        pendingPayment = null;
        syncState();
    }

    private void syncState() {
        stateSink.accept(pendingPayment);
    }

    private PlayerState playerState(String playerId) {
        PlayerState state = players.get(playerId);
        if (state == null) {
            throw new IllegalArgumentException("unknown player: " + playerId);
        }
        return state;
    }
}
