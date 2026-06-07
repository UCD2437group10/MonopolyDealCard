package edu.group10.monopolydeal.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import org.junit.jupiter.api.Test;

class CardMoneyRulesTest {

    @Test
    void actionAndRentCardsCanBeBankedUsingTheirFaceValue() {
        // Action and rent cards should keep their printed money value in the bank.
        SimpleCard action = new SimpleCard("Debt Collector", CardType.ACTION, "-", 3);
        SimpleCard rent = new SimpleCard("Rent Wild", CardType.RENT, "Any", 3);

        assertTrue(CardMoneyRules.canBank(action));
        assertTrue(CardMoneyRules.canBank(rent));
        assertEquals(3, CardMoneyRules.bankValue(action));
        assertEquals(3, CardMoneyRules.bankValue(rent));
    }

    @Test
    void propertyCardsCannotBeBanked() {
        // Property cards must stay in the property area instead of the bank.
        SimpleCard property = new SimpleCard("Boardwalk", CardType.PROPERTY, "Deep Blue", 0);

        assertFalse(CardMoneyRules.canBank(property));
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CardMoneyRules.validateBankable(property)
        );

        assertEquals("property and multi-property cannot be placed into bank", exception.getMessage());
    }

    @Test
    void bankableCardMustStillHavePositiveValue() {
        // A bankable card with zero value should still be rejected.
        SimpleCard badMoney = new SimpleCard("Broken Money", CardType.MONEY, "Test", 0);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CardMoneyRules.validateBankable(badMoney)
        );

        assertEquals("bankable card must have positive bank value", exception.getMessage());
    }
}
