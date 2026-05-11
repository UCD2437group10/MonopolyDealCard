package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.core.card.ActionCard;
import edu.group10.core.card.Operation;

public class SlyDealCard extends ActionCard {
    public SlyDealCard() {
        super("ACT_SLY_DEAL", "Sly Deal", 3);
    }

    @Override
    public Operation toOperation() {
        return Operation.of(
                Operation.TargetMode.SINGLE,
                Operation.step(CommandType.TRANSFER_PROPERTY).build()
        );
    }
}
