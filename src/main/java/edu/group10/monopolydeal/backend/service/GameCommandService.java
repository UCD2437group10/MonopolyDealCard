package edu.group10.monopolydeal.backend.service;

import edu.group10.monopolydeal.backend.game.GameEngine;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.network.protocol.GameRequest;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import java.util.List;
import java.util.Map;

/**
 * 统一命令入口，作为前后端桥接层。
 */
public class GameCommandService {

    private final GameEngine gameEngine;

    public GameCommandService(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

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
                return new GameResponse(false, "manual draw is disabled; cards are auto-drawn at turn start or by action effects", gameEngine.snapshot());
            }
            if ("DISCARD".equals(action)) {
                return new GameResponse(false, "manual discard is disabled; cards are only discarded by overflow", gameEngine.snapshot());
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
            if ("END_TURN".equals(action)) {
                gameEngine.endTurn(request.playerId());
                return ok("turn ended");
            }
            if ("BOT_TURN".equals(action)) {
                gameEngine.playBotTurn(request.playerId());
                return ok("bot turn played");
            }
            if ("STATE".equals(action)) {
                return ok("state");
            }
            if ("RESET".equals(action)) {
                gameEngine.resetGame();
                return ok("game reset");
            }
            return new GameResponse(false, "unknown action: " + action, gameEngine.snapshot());
        } catch (Exception exception) {
            return new GameResponse(false, exception.getMessage(), gameEngine.snapshot());
        }
    }

    private GameResponse ok(String message) {
        return new GameResponse(true, message, gameEngine.snapshot());
    }

    public List<String> supportedActions() {
        return List.of("JOIN", "READY", "UNREADY", "START", "PLAY_MONEY", "PLAY_PROPERTY", "PLAY_RENT", "PLAY_ACTION", "RESPOND_JSN", "END_TURN", "BOT_TURN", "STATE", "RESET");
    }

    public Map<String, String> actionDoc(String action) {
        return switch (action) {
            case "JOIN" -> Map.of("payload", "name, bot", "description", "加入房间");
            case "READY" -> Map.of("payload", "无", "description", "玩家标记就绪");
            case "UNREADY" -> Map.of("payload", "无", "description", "玩家取消就绪");
            case "START" -> Map.of("payload", "无", "description", "开始游戏并发牌");
            case "PLAY_MONEY" -> Map.of("payload", "handIndex", "description", "把手牌放入银行");
            case "PLAY_PROPERTY" -> Map.of("payload", "handIndex, colorChoice", "description", "打出物业或双面物业");
            case "PLAY_RENT" -> Map.of("payload", "handIndex, targetPlayerId, colorChoice, doubleRentCount", "description", "收租");
            case "PLAY_ACTION" -> Map.of("payload", "handIndex + action参数", "description", "打出行动牌（可能进入 JSN 反制）");
            case "RESPOND_JSN" -> Map.of("payload", "useCard(true/false)", "description", "响应 JSN 弹窗");
            case "END_TURN" -> Map.of("payload", "无", "description", "结束回合（手牌必须<=7）");
            case "BOT_TURN" -> Map.of("payload", "无", "description", "让当前 BOT 自动完成本回合");
            case "STATE" -> Map.of("payload", "无", "description", "获取状态快照");
            case "RESET" -> Map.of("payload", "无", "description", "清空当前对局状态（玩家/牌堆/回合）");
            default -> Map.of();
        };
    }
}
