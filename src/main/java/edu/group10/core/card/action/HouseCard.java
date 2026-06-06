package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.Collection;

public class HouseCard extends ActionCard {
    public HouseCard() {
        super("ACT_HOUSE", "House", 3);
    }

    public Collection<Command> returnCommand(Suit suit) {
        suit.setExtraValue(3);
        return null;
    }
}
