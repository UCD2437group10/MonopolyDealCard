package edu.group10.monopolydeal.backend.model.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import org.junit.jupiter.api.Test;

class PlayerStatePropertyGroupTest {

    @Test
    void addPropertyCreatesNewGroupAfterCompleteSet() {
        PlayerState playerState = new PlayerState(new Player("p1", "Player1", false));

        playerState.addProperty("Brown", new SimpleCard("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        playerState.addProperty("Brown", new SimpleCard("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        playerState.addProperty("Brown", new SimpleCard("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0));

        assertEquals(2, playerState.properties().get("Brown").size());
        assertTrue(playerState.properties().containsKey("Brown (2)"));
        assertEquals(1, playerState.properties().get("Brown (2)").size());
    }

    @Test
    void removePropertyRefillsEarlierIncompleteGroupFromSameColor() {
        PlayerState playerState = new PlayerState(new Player("p1", "Player1", false));

        playerState.addProperty("Brown", new SimpleCard("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        playerState.addProperty("Brown", new SimpleCard("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        playerState.addProperty("Brown", new SimpleCard("Light Blue/Brown Multi", CardType.MULTI_PROPERTY, "Light Blue/Brown", 0));
        playerState.removeProperty("Brown", 0);

        assertEquals(2, playerState.properties().get("Brown").size());
        assertFalse(playerState.properties().containsKey("Brown (2)"));
    }
}
