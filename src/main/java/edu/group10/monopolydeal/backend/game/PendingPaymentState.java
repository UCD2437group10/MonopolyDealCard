package edu.group10.monopolydeal.backend.game;

import java.util.List;

/**
 * Tracks a payment sequence that is waiting on one payer at a time.
 */
final class PendingPaymentState {

    private final String collectorPlayerId;
    private final String sourceAction;
    private final int amount;
    private final List<String> payerIds;
    private int payerIndex;

    PendingPaymentState(String collectorPlayerId, String sourceAction, int amount, List<String> payerIds) {
        this.collectorPlayerId = collectorPlayerId;
        this.sourceAction = sourceAction;
        this.amount = amount;
        this.payerIds = List.copyOf(payerIds);
    }

    String collectorPlayerId() {
        return collectorPlayerId;
    }

    String sourceAction() {
        return sourceAction;
    }

    int amount() {
        return amount;
    }

    String waitingPlayerId() {
        return payerIds.get(payerIndex);
    }

    boolean advanceToNextPayer() {
        payerIndex++;
        return payerIndex < payerIds.size();
    }
}
