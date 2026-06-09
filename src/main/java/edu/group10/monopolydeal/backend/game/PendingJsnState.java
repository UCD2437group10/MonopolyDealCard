package edu.group10.monopolydeal.backend.game;

import java.util.List;
import java.util.Map;

/**
 * Tracks the current Just Say No interaction chain.
 */
final class PendingJsnState {

    private final String actorId;
    private final String sourceAction;
    private final PendingEffectType effectType;
    private final Map<String, String> payload;
    private final List<String> targets;
    private int targetIndex;
    private String waitingPlayerId;
    private int currentTargetUseCount;
    private long waitingSinceMs;

    PendingJsnState(String actorId, String sourceAction, PendingEffectType effectType, Map<String, String> payload, List<String> targets) {
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

    String actorId() {
        return actorId;
    }

    String sourceAction() {
        return sourceAction;
    }

    PendingEffectType effectType() {
        return effectType;
    }

    Map<String, String> payload() {
        return payload;
    }

    List<String> targets() {
        return targets;
    }

    int targetIndex() {
        return targetIndex;
    }

    void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    String waitingPlayerId() {
        return waitingPlayerId;
    }

    void setWaitingPlayerId(String waitingPlayerId) {
        this.waitingPlayerId = waitingPlayerId;
    }

    int currentTargetUseCount() {
        return currentTargetUseCount;
    }

    void incrementCurrentTargetUseCount() {
        currentTargetUseCount++;
    }

    void resetCurrentTargetUseCount() {
        currentTargetUseCount = 0;
    }

    long waitingSinceMs() {
        return waitingSinceMs;
    }

    void setWaitingSinceMs(long waitingSinceMs) {
        this.waitingSinceMs = waitingSinceMs;
    }
}
