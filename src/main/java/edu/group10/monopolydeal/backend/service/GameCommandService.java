package edu.group10.monopolydeal.backend.service;

import edu.group10.monopolydeal.backend.game.GameEngine;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.network.protocol.GameRequest;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import java.util.List;
import java.util.Map;

/**
 * Handles command dispatch between transport code and the game engine.
 */
public class GameCommandService {

    /** Core game engine that executes validated commands. */
    private final GameEngine gameEngine;

    /** Creates a command service for one game engine instance. */
    public GameCommandService(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    /** Executes a request and converts failures into a response object. */
    public GameResponse handle(GameRequest request) {
        try {
            String action = request.action();
            Map<String, String> payload = request.payload() == null ? Map.of() : request.payload();
            if ("JOIN".equals(action)) {
                boolean bot = Boolean.parseBoolean(payload.getOrDefault("bot", "false"));
                gameEngine.addPlayer(new Player(request.playerId(), payload.getOrDefault("name", request.playerId()), bot));
                return ok("player joined");
            }
            if ("START".equals(action)) {
                gameEngine.startGame(request.playerId());
                return ok("game started");
            }
            if ("READY".equals(action)) {
                gameEngine.setReady(request.playerId(), true);
                return ok("player ready");
            }
            if ("UNREADY".equals(action)) {
                gameEngine.setReady(request.playerId(), false);
                return ok("player unready");
            }
            if ("DRAW".equals(action)) {
                return new GameResponse(false, "manual draw is disabled; cards are auto-drawn at turn start or by action effects", currentSnapshot());
            }
            if ("DISCARD".equals(action)) {
                return new GameResponse(false, "manual discard is disabled; cards are only discarded by overflow", currentSnapshot());
            }
            if ("PLAY_MONEY".equals(action)) {
                int handIndex = Integer.parseInt(payload.getOrDefault("handIndex", "-1"));
                gameEngine.playMoneyCard(request.playerId(), handIndex);
                return ok("money placed to bank");
            }
            if ("PLAY_PROPERTY".equals(action)) {
                int handIndex = Integer.parseInt(payload.getOrDefault("handIndex", "-1"));
                String colorChoice = payload.getOrDefault("colorChoice", "");
                gameEngine.playPropertyCard(request.playerId(), handIndex, colorChoice);
                return ok("property played");
            }
            if ("PLAY_RENT".equals(action)) {
                int handIndex = Integer.parseInt(payload.getOrDefault("handIndex", "-1"));
                int doubleRentCount = Integer.parseInt(payload.getOrDefault("doubleRentCount", "0"));
                gameEngine.playRentCard(
                        request.playerId(),
                        handIndex,
                        payload.getOrDefault("targetPlayerId", ""),
                        payload.getOrDefault("colorChoice", ""),
                        doubleRentCount
                );
                return ok("rent charged");
            }
            if ("CHANGE_PROPERTY_COLOR".equals(action)) {
                int propertyIndex = Integer.parseInt(payload.getOrDefault("propertyIndex", "-1"));
                gameEngine.changePropertyColor(
                        request.playerId(),
                        payload.getOrDefault("fromColor", ""),
                        propertyIndex,
                        payload.getOrDefault("colorChoice", "")
                );
                return ok("property color changed");
            }
            if ("PLAY_ACTION".equals(action)) {
                int handIndex = Integer.parseInt(payload.getOrDefault("handIndex", "-1"));
                gameEngine.playActionCard(request.playerId(), handIndex, payload);
                return ok("action card played");
            }
            if ("RESPOND_JSN".equals(action)) {
                boolean useCard = Boolean.parseBoolean(payload.getOrDefault("useCard", "false"));
                gameEngine.respondJustSayNo(request.playerId(), useCard);
                return ok("jsn response submitted");
            }
            if ("SUBMIT_PAYMENT".equals(action)) {
                gameEngine.submitPendingPayment(request.playerId(), edu.group10.monopolydeal.backend.game.PaymentSelection.fromPayload(payload));
                return ok("payment submitted");
            }
            if ("END_TURN".equals(action)) {
                gameEngine.endTurn(request.playerId());
                return ok("turn ended");
            }
            if ("BOT_TURN".equals(action)) {
                gameEngine.playBotTurn(request.playerId());
                return ok("bot turn played");
            }
            if ("STATE".equals(action)) {
                return new GameResponse(true, "state", gameEngine.pollStateSnapshot());
            }
            if ("RESET".equals(action)) {
                gameEngine.resetGame();
                return ok("game reset");
            }
            return new GameResponse(false, "unknown action: " + action, currentSnapshot());
        } catch (Exception exception) {
            return new GameResponse(false, exception.getMessage(), currentSnapshot());
        }
    }

    /** Builds a successful response using the latest engine snapshot. */
    private GameResponse ok(String message) {
        return new GameResponse(true, message, currentSnapshot());
    }

    /** Returns a stable snapshot without triggering extra rule processing. */
    private edu.group10.monopolydeal.backend.game.GameState currentSnapshot() {
        return gameEngine.snapshot();
    }
}
