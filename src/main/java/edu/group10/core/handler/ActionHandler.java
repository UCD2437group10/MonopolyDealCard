package edu.group10.core.handler;

import edu.group10.common.model.*;
import edu.group10.common.skill.SkillResolver;
import edu.group10.core.GameEngineException;
import edu.group10.core.engine.CommandExecutor;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;

import java.util.ArrayList;
import java.util.List;

public class ActionHandler {

    private final CommandExecutor commandExecutor;
    private SkillResolver skillResolver;

    public ActionHandler(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    /**
     * 设置技能解析器（由 Logic 模块调用）
     */
    public void setSkillResolver(SkillResolver resolver) {
        this.skillResolver = resolver;
    }

    /**
     * 处理行动卡
     */
    public List<GameEvent> handle(InternalGameState state, Player player,
                                  Card actionCard, PlayerAction action)
            throws GameEngineException {

        if (skillResolver == null) {
            throw new GameEngineException("SKILL_RESOLVER_NOT_READY",
                    "技能解析器未就绪");
        }

        // 构建技能上下文
        SkillContext context = buildContext(state, player, actionCard, action);

        // 调用 Logic 模块解析技能效果
        List<Command> commands = skillResolver.resolve(context);

        // 执行返回的指令
        return commandExecutor.executeAll(state, commands);
    }

    /**
     * 构建技能上下文
     */
    private SkillContext buildContext(InternalGameState state, Player player,
                                      Card actionCard, PlayerAction action) {

        SkillContext context = new SkillContext();

        context.setGameId(state.getGameId());
        context.setActorId(player.getPlayerId());
        context.setTargetId(action.getTargetPlayerId());
        context.setSkillCard(actionCard);
        context.setCurrentPhase(state.getPhase());
        context.setCurrentTurnPlayerIndex(state.getCurrentPlayerIndex());

        // 转换玩家状态
        context.setPlayers(convertToPlayerStateMap(state));

        // 目标玩家信息
        if (action.getTargetPlayerId() != null) {
            Player targetPlayer = state.getPlayer(action.getTargetPlayerId());
            if (targetPlayer != null) {
                context.setTargetPlayerMoney(targetPlayer.getMoney());
                context.setTargetPlayerProperties(targetPlayer.getProperties());
            }
        }

        // 从 PlayerAction 中获取颜色和租金（如果有）
        if (action.getSelectedColor() != null) {
            context.setSelectedColor(action.getSelectedColor());
        }
        if (action.getRentAmount() != null) {
            context.setBaseRent(action.getRentAmount());
        }

        return context;
    }

    /**
     * 将内部 Player 列表转换为对外 PlayerState Map
     */
    private java.util.Map<String, PlayerState> convertToPlayerStateMap(InternalGameState state) {
        java.util.Map<String, PlayerState> result = new java.util.HashMap<>();

        for (Player p : state.getPlayers()) {
            PlayerState ps = new PlayerState();
            ps.setPlayerId(p.getPlayerId());
            ps.setPlayerName(p.getPlayerName());
            ps.setMoney(p.getMoney());
            ps.setHandCardCount(p.getHandSize());
            ps.setStatus(p.getStatus());
            ps.setCompletedSets(p.getCompletedSets());

            List<String> propertyIds = p.getProperties().stream()
                    .map(Property::getCardId)
                    .toList();
            ps.setPropertyIds(propertyIds);

            result.put(p.getPlayerId(), ps);
        }

        return result;
    }
}
