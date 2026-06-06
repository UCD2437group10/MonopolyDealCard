package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.enums.PropertyColor;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.ArrayList;
import java.util.Collection;

public class SlyDealCard extends ActionCard {
    public SlyDealCard() {
        super("ACT_SLY_DEAL", "Sly Deal", 3);
    }

    public Collection<Command> returnCommand(String fromPlayer, String toAllPlayer, Suit suit) {
        ArrayList<Command> commands = new ArrayList<>();
        Command command = new Command(CommandType.REMOVE_MONEY, fromPlayer, toAllPlayer);
        commands.add(command);
        return commands;
    }
}
