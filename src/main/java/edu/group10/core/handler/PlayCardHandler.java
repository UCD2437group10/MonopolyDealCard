package edu.group10.core.handler;

import edu.group10.common.model.*;
import edu.group10.core.GameEngineException;
import edu.group10.core.engine.RuleValidator;
import edu.group10.core.manager.CardManager;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;
import edu.group10.core.engine.CommandExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * 出牌处理器
 * 处理物业卡、钱卡、行动卡
 */
public class PlayCardHandler {
    private CommandExecutor commandExecutor;

    // TODO: 等 Logic 模块完成后，添加这个字段
    // private SkillResolver skillResolver;

    public void ActionHandler(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    // TODO: 等 Logic 模块完成后，添加这个方法
    // public void setSkillResolver(SkillResolver skillResolver) {
    //     this.skillResolver = skillResolver;
    // }

    /**
     * 处理行动卡
     *
     * TODO: 等 Logic 模块完成后实现完整逻辑
     * 当前返回空事件列表
     */
    public List<GameEvent> handle(InternalGameState state, Player player,
                                  Card actionCard, PlayerAction action)
            throws GameEngineException {

        // TODO: 等 Logic 模块完成后，取消注释以下代码

        // if (skillResolver == null) {
        //     throw new GameEngineException("SKILL_RESOLVER_NOT_READY", "技能解析器未就绪");
        // }
        //
        // SkillContext ctx = buildContext(state, player, actionCard, action);
        // List<Command> commands = skillResolver.resolve(ctx);
        // return commandExecutor.executeAll(state, commands);

        // 临时返回空事件
        System.out.println("[ActionHandler] 行动卡暂未实现: " + actionCard.getCardName());
        return new ArrayList<>();
    }

    /**
     * 构建技能上下文
     *
     * TODO: 等 Logic 模块完成后，根据需要补充更多字段
     */
    private SkillContext buildContext(InternalGameState state, Player player,
                                      Card actionCard, PlayerAction action) {
        SkillContext ctx = new SkillContext();
        ctx.setGameId(state.getGameId());
        ctx.setActorId(player.getPlayerId());
        ctx.setTargetId(action.getTargetPlayerId());
        ctx.setSkillCard(actionCard);

        // TODO: 根据需要添加更多上下文信息
        // ctx.setPlayers(state.getPlayers());
        // ctx.setCurrentPhase(state.getPhase());
        // ctx.setCurrentTurnPlayerIndex(state.getCurrentPlayerIndex());

        return ctx;
    }
}
