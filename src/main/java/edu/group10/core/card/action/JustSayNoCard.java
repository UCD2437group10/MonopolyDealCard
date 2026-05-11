package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.ArrayList;

public class JustSayNoCard extends ActionCard {
    public JustSayNoCard() {
        super("ACT_JUST_SAY_NO", "Just Say No", 4);
    }

    public ArrayList<Command> returnOperation(String fromPlayer, String toPlayer) {
        ArrayList<Command> arrayList = new ArrayList<>();
        Command command = new Command(CommandType.STOP_CARD,fromPlayer, toPlayer);
        arrayList.add(command);
        return arrayList;
    }

}
