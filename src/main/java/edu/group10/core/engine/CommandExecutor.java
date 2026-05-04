package edu.group10.core.engine;

import edu.group10.common.enums.CommandType;
import edu.group10.common.model.Command;
import edu.group10.common.model.GameEvent;
import edu.group10.core.model.InternalGameState;
import edu.group10.core.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Command executor
 * Execute Command returned from Logic model
 */
public class CommandExecutor {
    public List<GameEvent> execute(InternalGameState state, Command cmd) {
        // TODO: 等 Logic 模块完成后实现
        // 目前先返回空事件
        return new ArrayList<>();
    }

    public List<GameEvent> executeAll(InternalGameState state, List<Command> commands) {
        List<GameEvent> events = new ArrayList<>();
        for (Command cmd : commands) {
            events.addAll(execute(state, cmd));
        }
        return events;
    }
}
