package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.ArrayList;
import java.util.Collection;

public class DebtCollectorCard extends ActionCard {
    public DebtCollectorCard() {
        super("ACT_DEBT_COLLECTOR", "Debt Collector", 3);
    }

    public Collection<Command> returnCommand(String fromPlayer, String[] toPlayer) {
        ArrayList<Command> arrayList = new ArrayList<>();
        Command command = new Command(CommandType.REMOVE_MONEY, fromPlayer, toPlayer[0]);
        command.setAmount(5);
        arrayList.add(command);
        return arrayList;
    }
}
