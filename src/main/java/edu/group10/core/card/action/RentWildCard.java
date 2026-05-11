package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.enums.PropertyColor;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.ArrayList;
import java.util.Collection;

public class RentWildCard extends ActionCard {
    public RentWildCard() {
        super("ACT_RENT_WILD", "Rent Wild", 3);
    }

    public Collection<Command> returnCommand(String fromPlayer, String[] toAllPlayer, Suit suit) {
        ArrayList<Command> commands = new ArrayList<>();
        for (String id : toAllPlayer) {
            Command command = new Command(CommandType.REMOVE_MONEY, fromPlayer, id);
            command.setAmount(suit.returnMoney());
            commands.add(command);
        }
        return commands;
    }
}
