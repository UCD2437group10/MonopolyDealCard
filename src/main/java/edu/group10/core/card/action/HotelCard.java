package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.common.model.Command;
import edu.group10.core.card.ActionCard;

import java.util.Collection;

public class HotelCard extends ActionCard {
    public HotelCard() {
        super("ACT_HOTEL", "Hotel", 4);
    }

    public Collection<Command> returnCommand(Suit suit) {
        suit.setExtraValue(4);
        return null;
    }
}
