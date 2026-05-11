package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.enums.PropertyColor;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.ArrayList;
import java.util.Collection;

public class ItsMyBirthdayCard extends ActionCard {
    public ItsMyBirthdayCard() {
        super("ACT_ITS_MY_BIRTHDAY", "It's My Birthday", 2);
    }

    public Collection<Command> returnCommand(String fromPlayer, String[] toAllPlayer) {
        ArrayList<Command> commands = new ArrayList<>();
        for (String id : toAllPlayer) {
            Command command = new Command(CommandType.REMOVE_MONEY, fromPlayer, id);
            command.setAmount(2);
            commands.add(command);
        }
        return commands;
    }

}
