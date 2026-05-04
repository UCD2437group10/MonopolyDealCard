package edu.group10.common.model;

import edu.group10.common.enums.CommandType;

public class Command {
    private CommandType type;
    private String fromPlayerId;
    private String toPlayerId;
    private int amount; //Amount of money
    private String cardId; //Involved card
    private String propertyId; //Involved property
    private String description;

    public Command() {}

    public Command(CommandType type, String fromPlayerId, String toPlayerId) {
        this.type = type;
        this.fromPlayerId = fromPlayerId;
        this.toPlayerId = toPlayerId;
    }

    //Fast construction of transferring money
    public static Command transferMoney(String fromId, String toId, int amount) {
        Command cmd = new Command(CommandType.TRANSFER_MONEY, fromId, toId);
        cmd.setAmount(amount);
        return cmd;
    }

    //Fast construction of transferring property
    public static Command transferProperty(String fromId, String toId, String propertyId) {
        Command cmd = new Command(CommandType.TRANSFER_PROPERTY, fromId, toId);
        cmd.setPropertyId(propertyId);
        return cmd;
    }

    //Getters and setters
    public CommandType getType() { return type; }
    public void setType(CommandType type) { this.type = type; }

    public String getFromPlayerId() { return fromPlayerId; }
    public void setFromPlayerId(String fromPlayerId) { this.fromPlayerId = fromPlayerId; }

    public String getToPlayerId() { return toPlayerId; }
    public void setToPlayerId(String toPlayerId) { this.toPlayerId = toPlayerId; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Command{" +
                "type=" + type +
                ", from='" + fromPlayerId + '\'' +
                ", to='" + toPlayerId + '\'' +
                ", amount=" + amount +
                ", propertyId='" + propertyId + '\'' +
                '}';
    }
}
