package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.ArrayList;
import java.util.Collection;

public class DealBreakerCard extends ActionCard {
    public DealBreakerCard() {
        super("ACT_DEAL_BREAKER", "Deal Breaker", 5);
    }

    public Collection<Command> returnCommand(String fromPlayer, String[] toAllPlayer, Suit suit) {
        ArrayList<Command> commands = new ArrayList<>();
        Command command = new Command(CommandType.CHANGE_OWN, fromPlayer, toAllPlayer[0]);
        return commands;
    }
}
