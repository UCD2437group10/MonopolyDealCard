package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.ArrayList;
import java.util.Collection;

public class ForcedDealCard extends ActionCard {
    public ForcedDealCard() {
        super("ACT_FORCED_DEAL", "Forced Deal", 3);
    }

    public Collection<Command> returnCommand(String fromPlayer, String[] toPlayer) {
        ArrayList<Command> arrayList = new ArrayList<>();
        Command command = new Command(CommandType.SWAP_PROPERTY, fromPlayer, toPlayer[0]);
        arrayList.add(command);
        return arrayList;
    }
}
