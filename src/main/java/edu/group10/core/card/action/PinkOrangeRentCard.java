package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.enums.PropertyColor;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.ArrayList;
import java.util.Collection;

public class PinkOrangeRentCard extends ActionCard {
    public PinkOrangeRentCard() {
        super("ACT_RENT_PINK_ORANGE", "Pink/Orange Rent", 1);
    }

    public Collection<Command> returnCommand(String fromPlayer, String[] toAllPlayer, Suit suit) {
        if (suit.getColour().equals(PropertyColor.PINK) || suit.getColour().equals(PropertyColor.ORANGE)) {
            ArrayList<Command> commands = new ArrayList<>();
            for (String id : toAllPlayer) {
                Command command = new Command(CommandType.REMOVE_MONEY, fromPlayer, id);
                command.setAmount(suit.returnMoney());
                commands.add(command);
            }
            return commands;
        }
        return null;
    }
}
