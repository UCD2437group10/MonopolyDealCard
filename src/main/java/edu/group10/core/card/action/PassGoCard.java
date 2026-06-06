package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.ArrayList;
import java.util.Collection;

public class PassGoCard extends ActionCard {
    public PassGoCard() {
        super("ACT_PASS_GO", "Pass Go", 1);
    }

    public Collection<Command> returnCommand(String fromPlayer) {
        ArrayList<Command> commands = new ArrayList<>();
        Command command = new Command();
        command.setFromPlayerId(fromPlayer);
        command.setType(CommandType.DRAW_CARD);
        command.setAmount(2);
        commands.add(command);
        return commands;
    }
}
