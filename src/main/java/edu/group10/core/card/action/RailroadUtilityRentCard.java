package edu.group10.core.card.action;

import edu.group10.common.enums.CommandType;
import edu.group10.core.card.ActionCard;
import edu.group10.core.card.Operation;

public class RailroadUtilityRentCard extends ActionCard {
    public RailroadUtilityRentCard() {
        super("ACT_RENT_RAILROAD_UTILITY", "Railroad/Utility Rent", 1);
    }

    @Override
    public Operation toOperation() {
        Operation.TargetMode mode = Operation.TargetMode.SINGLE;
        return Operation.of(
                mode,
                Operation.step(CommandType.TRANSFER_MONEY).build()
        );
    }
}
