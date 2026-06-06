package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.ArrayList;
import java.util.Collection;

public class DoubleTheRentCard extends ActionCard {
    public DoubleTheRentCard() {
        super("ACT_DOUBLE_THE_RENT", "Double The Rent", 1);
    }

    public Collection<Command> returnCommand(String fromPlayer, String[] toAllPlayer, Suit suit) {
        ArrayList<Command> commands = new ArrayList<>();
        for (String id : toAllPlayer) {
            Command command = new Command(CommandType.REMOVE_MONEY, fromPlayer, id);
            command.setAmount(suit.returnMoney()*2);
            commands.add(command);
        }
        return commands;
    }
}
