package edu.group10.core;

import edu.group10.common.model.GameActionResult;
import edu.group10.common.model.GameEvent;
import edu.group10.common.model.GameState;
import edu.group10.common.model.PlayerAction;
import edu.group10.core.engine.*;
import edu.group10.core.manager.CardManager;
import edu.group10.core.manager.GameStateManager;
import edu.group10.core.model.InternalGameState;

import java.util.List;
import java.util.logging.Logger;

/**
 * 游戏引擎实现
 *
 * Core 模块的主入口，实现 GameEngine 接口
 *
 * TODO: 等 Logic 模块完成后需要：
 * 1. 注入 SkillResolver 到 ActionExecutor
 * 2. 取消注释 setSkillResolver() 相关代码
 */
public class GameEngineImpl implements GameEngine{
    private final GameStateManager stateManager;
    private final CardManager cardManager;
    private final RuleValidator ruleValidator;
    private final CommandExecutor commandExecutor;
    private final VictoryChecker victoryChecker;
    private final ActionExecutor actionExecutor;
    private final TurnManager turnManager;
    private static final Logger logger = Logger.getLogger(GameEngineImpl.class.getName());

    // TODO: 等 Logic 模块完成后，添加这个字段
    // private SkillResolver skillResolver;

    public GameEngineImpl() {
        this.cardManager = new CardManager();
        this.stateManager = new GameStateManager(cardManager);
        this.ruleValidator = new RuleValidator();
        this.commandExecutor = new CommandExecutor();
        this.victoryChecker = new VictoryChecker();
        this.actionExecutor = new ActionExecutor(cardManager, ruleValidator,
                commandExecutor, victoryChecker);
        this.turnManager = new TurnManager();
    }

    // TODO: 等 Logic 模块完成后，取消注释这个方法
    /**
     * 设置技能解析器（由 Logic 模块调用）
     *
     *  @param
     */
    // public void setSkillResolver(SkillResolver resolver) {
    //     this.skillResolver = resolver;
    //     this.actionExecutor.setSkillResolver(resolver);
    //     logger.info("SkillResolver has been set to ActionExecutor");
    // }

    @Override
    public GameActionResult executeAction(String gameId, PlayerAction action) {
        if (gameId == null || gameId.trim().isEmpty()) {
            return GameActionResult.failure("INVALID_GAME_ID", "Game ID cannot be null");
        }

        if (action == null) {
            return GameActionResult.failure("INVALID_ACTION", "Action cannot be null");
        }

        if (action.getPlayerId() == null || action.getPlayerId().trim().isEmpty()) {
            return GameActionResult.failure("INVALID_PLAYER", "Player ID cannot be null");
        }

        try {
            InternalGameState state = stateManager.getGame(gameId);
            if (state == null) {
                return GameActionResult.failure("GAME_NOT_FOUND", "Game unexists: " + gameId);
            }

            List<GameEvent> timeoutEvents = turnManager.handleTimeout(state);

            ActionExecutor.ExecutionResult result = actionExecutor.execute(state, action);

            List<GameEvent> allEvents = result.getEvents();
            if (timeoutEvents != null) {
                allEvents.addAll(0, timeoutEvents);
            }

            GameState externalState = state.toExternalGameState();
            GameActionResult gameResult = GameActionResult.success(externalState, allEvents);
            gameResult.setActionSequence(action.getActionSequence());

            logger.info(String.format("[GameEngine] Execute action: gameId=%s, playerId=%s, type=%s, success=true",
                    gameId, action.getPlayerId(), action.getType()));

            return gameResult;

        } catch (GameEngineException e) {
            logger.warning(String.format("[GameEngine] Failed to execute action: gameId=%s, playerId=%s, type=%s, error=%s",
                    gameId, action.getPlayerId(), action.getType(), e.getErrorCode()));
            return GameActionResult.failure(e.getErrorCode(), e.getMessage());

        } catch (Exception e) {
            logger.severe(String.format("[GameEngine] System error: gameId=%s, playerId=%s, error=%s",
                    gameId, action.getPlayerId(), e.getMessage()));
            e.printStackTrace();
            return GameActionResult.failure("INTERNAL_ERROR", "Internal system error: " + e.getMessage());
        }
    }

    @Override
    public GameState getGameState(String gameId) {
        if (gameId == null || gameId.trim().isEmpty()) {
            return null;
        }

        InternalGameState state = stateManager.getGame(gameId);
        if (state == null) {
            return null;
        }

        return state.toExternalGameState();
    }

    // Management of game lifecycle (will called by Infra)

    /**
     * Create a new game
     *
     * @param gameId Game ID
     * @param playerIds Player ID list
     * @param playerNames Player name list
     * @return Whether it is successful to create
     */
    public boolean createGame(String gameId, List<String> playerIds, List<String> playerNames) {
        if (stateManager.gameExists(gameId)) {
            logger.warning("[GameEngine] Game is already exists: " + gameId);
            return false;
        }

        if (playerIds == null || playerIds.size() < 2 || playerIds.size() > 5) {
            logger.warning("[GameEngine] Player number must between 2 and 5: " + (playerIds == null ? 0 : playerIds.size()));
            return false;
        }

        stateManager.createGame(gameId, playerIds, playerNames);
        logger.info("[GameEngine] Success to create game: " + gameId);
        return true;
    }

    /**
     * Start the game
     *
     * @param gameId Game ID
     * @return Whether it is successful to start
     */
    public boolean startGame(String gameId) {
        InternalGameState state = stateManager.getGame(gameId);
        if (state == null) {
            logger.warning("[GameEngine] Failed to start the game. The game does not exist: " + gameId);
            return false;
        }

        stateManager.startGame(gameId);
        logger.info("[GameEngine] The game starts: " + gameId);
        return true;
    }

    /**
     * Finish and remove the game
     *
     * @param gameId Game ID
     */
    public void endGame(String gameId) {
        stateManager.removeGame(gameId);
        logger.info("[GameEngine] Finish and remove the game: " + gameId);
    }

    /**
     * Get the card manager (being used to debug)
     */
    public CardManager getCardManager() {
        return cardManager;
    }

    /**
     * Get the rule validator (being used to debug)
     */
    public RuleValidator getRuleValidator() {
        return ruleValidator;
    }
}
