package edu.group10.monopolydeal.backend.network.protocol;

import static org.junit.jupiter.api.Assertions.*;

import edu.group10.monopolydeal.backend.game.GameState;
import edu.group10.monopolydeal.backend.model.card.CardType;
import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.model.player.Player;
import edu.group10.monopolydeal.backend.model.player.PlayerState;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests for ProtocolMapper: GameResponse ↔ NetGameResponse conversion.
 */
class ProtocolMapperTest {

    // ========== toNet (domain → network) ==========

    @Test
    void toNetNullResponseReturnsEmptyResponse() {
        NetGameResponse result = ProtocolMapper.toNet(null);
        assertNotNull(result);
        assertFalse(result.success());
        assertEquals("empty response", result.message());
        assertNull(result.gameState());
    }

    @Test
    void toNetNullGameState() {
        GameResponse response = new GameResponse(true, "ok", null);
        NetGameResponse result = ProtocolMapper.toNet(response);
        assertTrue(result.success());
        assertEquals("ok", result.message());
        assertNull(result.gameState());
    }

    @Test
    void toNetFullGameState() {
        // Build a domain GameState with all fields populated
        List<PlayerState> players = new ArrayList<>();
        PlayerState ps1 = new PlayerState(new Player("p1", "Alice", false));
        ps1.addToHand(new SimpleCard("1M Money", CardType.MONEY, "Yellow", 1));
        ps1.addToBank(new SimpleCard("5M Money", CardType.MONEY, "Purple", 5));
        ps1.addProperty("Brown", new SimpleCard("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        ps1.addHouse("Brown");
        players.add(ps1);

        List<edu.group10.monopolydeal.backend.model.card.Card> discard = new ArrayList<>();
        discard.add(new SimpleCard("Pass Go", CardType.ACTION, "-", 1));

        GameState state = new GameState(
                true,       // started
                false,      // gameOver
                "",         // winnerPlayerId
                "p1",       // currentPlayerId
                "p2",       // jsnResponderPlayerId
                "p1",       // jsnActorPlayerId
                "p2",       // jsnTargetPlayerId
                "Debt Collector", // jsnSourceAction
                10,         // drawPileCount
                3,          // discardPileCount
                discard,    // discardPileCards
                players,    // players
                Set.of("p1") // readyPlayerIds
        );

        GameResponse response = new GameResponse(true, "state", state);
        NetGameResponse result = ProtocolMapper.toNet(response);

        assertTrue(result.success());
        assertEquals("state", result.message());
        assertNotNull(result.gameState());
        assertTrue(result.gameState().started());
        assertFalse(result.gameState().gameOver());
        assertEquals("p1", result.gameState().currentPlayerId());
        assertEquals("p2", result.gameState().jsnResponderPlayerId());
        assertEquals("p1", result.gameState().jsnActorPlayerId());
        assertEquals("p2", result.gameState().jsnTargetPlayerId());
        assertEquals("Debt Collector", result.gameState().jsnSourceAction());
        assertEquals(10, result.gameState().drawPileCount());
        assertEquals(3, result.gameState().discardPileCount());
        assertEquals(1, result.gameState().discardPileCards().size());
        assertEquals(1, result.gameState().players().size());
        assertTrue(result.gameState().readyPlayerIds().contains("p1"));
    }

    @Test
    void toNetPlayerStateHasCorrectFields() {
        PlayerState ps = new PlayerState(new Player("p1", "Alice", false));
        ps.addToHand(new SimpleCard("1M Money", CardType.MONEY, "Yellow", 1));
        ps.addToHand(new SimpleCard("Pass Go", CardType.ACTION, "-", 1));
        ps.addToBank(new SimpleCard("5M Money", CardType.MONEY, "Purple", 5));
        ps.addProperty("Brown", new SimpleCard("Baltic Avenue", CardType.PROPERTY, "Brown", 0));
        ps.addProperty("Brown", new SimpleCard("Mediterranean Avenue", CardType.PROPERTY, "Brown", 0));
        ps.addHouse("Brown");
        ps.addHotel("Brown");

        List<PlayerState> players = List.of(ps);
        GameState state = new GameState(
                true, false, "", "p1", "", "", "", "",
                10, 0, List.of(), players, Set.of()
        );
        GameResponse response = new GameResponse(true, "", state);
        NetGameResponse result = ProtocolMapper.toNet(response);

        NetPlayerState netPs = result.gameState().players().get(0);
        assertEquals("p1", netPs.player().id());
        assertEquals("Alice", netPs.player().displayName());
        assertEquals(2, netPs.hand().size());
        assertEquals(1, netPs.bank().size());
        assertEquals(2, netPs.properties().get("Brown").size());
        assertEquals(1, netPs.houseByColor().getOrDefault("Brown", 0));
        assertEquals(1, netPs.hotelByColor().getOrDefault("Brown", 0));
    }

    @Test
    void toNetBotPlayerState() {
        PlayerState ps = new PlayerState(new Player("bot1", "SimpleBot", true));
        List<PlayerState> players = List.of(ps);
        GameState state = new GameState(
                true, false, "", "p1", "", "", "", "",
                10, 0, List.of(), players, Set.of()
        );
        GameResponse response = new GameResponse(true, "", state);
        NetGameResponse result = ProtocolMapper.toNet(response);

        NetPlayerState netPs = result.gameState().players().get(0);
        assertTrue(netPs.player().bot());
        assertEquals("SimpleBot", netPs.player().displayName());
    }

    // ========== toDomain (network → domain) ==========

    @Test
    void toDomainNullResponse() {
        GameResponse result = ProtocolMapper.toDomain(null);
        assertNotNull(result);
        assertFalse(result.success());
        assertEquals("empty response", result.message());
        assertNull(result.gameState());
    }

    @Test
    void toDomainNullGameState() {
        NetGameResponse response = new NetGameResponse(true, "ok", null);
        GameResponse result = ProtocolMapper.toDomain(response);
        assertTrue(result.success());
        assertNull(result.gameState());
    }

    @Test
    void toDomainFullRoundtrip() {
        // Build a domain player state and convert through Net → back to domain
        PlayerState originalPs = new PlayerState(new Player("p1", "Alice", false));
        originalPs.addToHand(new SimpleCard("1M Money", CardType.MONEY, "Yellow", 1));
        originalPs.addToBank(new SimpleCard("10M Money", CardType.MONEY, "Gold-Orange", 10));
        originalPs.addProperty("Railroad", new SimpleCard("Reading Railroad", CardType.PROPERTY, "Railroad", 0));
        originalPs.addHouse("Railroad");

        List<PlayerState> players = List.of(originalPs);
        List<edu.group10.monopolydeal.backend.model.card.Card> discard = List.of(
                new SimpleCard("Deal Breaker", CardType.ACTION, "-", 5)
        );
        GameState originalState = new GameState(
                true, false, "", "p1", "p2", "p1", "p2", "Sly Deal",
                5, 2, discard, players, Set.of("p1", "p2")
        );
        GameResponse original = new GameResponse(true, "test", originalState);

        // Convert to net
        NetGameResponse net = ProtocolMapper.toNet(original);
        // Convert back to domain
        GameResponse back = ProtocolMapper.toDomain(net);

        assertTrue(back.success());
        assertEquals("test", back.message());
        assertNotNull(back.gameState());
        assertTrue(back.gameState().started());
        assertEquals("p1", back.gameState().currentPlayerId());
        assertEquals("p2", back.gameState().jsnResponderPlayerId());
        assertEquals(5, back.gameState().drawPileCount());
        assertEquals(2, back.gameState().discardPileCount());
        assertEquals(1, back.gameState().discardPileCards().size());
        assertEquals(1, back.gameState().players().size());
        assertTrue(back.gameState().readyPlayerIds().contains("p1"));
        assertTrue(back.gameState().readyPlayerIds().contains("p2"));

        PlayerState backPs = back.gameState().players().get(0);
        assertEquals("p1", backPs.player().id());
        assertEquals("Alice", backPs.player().displayName());
        assertEquals(1, backPs.hand().size());
        assertEquals(1, backPs.bank().size());
        assertTrue(backPs.hasProperty("Railroad"));
        assertTrue(backPs.hasHouse("Railroad"));
    }

    @Test
    void toDomainHandlesNullCollections() {
        NetGameState netState = new NetGameState(
                false, false, "", "", "", "", "", "",
                0, 0, null, null, null
        );
        NetGameResponse netResponse = new NetGameResponse(true, "ok", netState);
        GameResponse result = ProtocolMapper.toDomain(netResponse);

        assertTrue(result.success());
        assertNotNull(result.gameState());
        assertNotNull(result.gameState().discardPileCards());
        assertTrue(result.gameState().discardPileCards().isEmpty());
        assertNotNull(result.gameState().players());
        assertTrue(result.gameState().players().isEmpty());
        assertNotNull(result.gameState().readyPlayerIds());
        assertTrue(result.gameState().readyPlayerIds().isEmpty());
    }

    // ========== Edge cases ==========

    @Test
    void toNetWithMultiplePlayers() {
        List<PlayerState> players = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            PlayerState ps = new PlayerState(new Player("p" + i, "Player" + i, i > 2));
            ps.addToHand(new SimpleCard("1M Money", CardType.MONEY, "Yellow", 1));
            players.add(ps);
        }

        GameState state = new GameState(
                true, false, "", "p1", "", "", "", "",
                20, 5, List.of(), players, Set.of("p1", "p2")
        );
        GameResponse response = new GameResponse(true, "", state);
        NetGameResponse result = ProtocolMapper.toNet(response);

        assertEquals(4, result.gameState().players().size());
        assertEquals("p4", result.gameState().players().get(3).player().id());
        assertTrue(result.gameState().players().get(2).player().bot());
        assertFalse(result.gameState().players().get(1).player().bot());
    }

    @Test
    void toNetEmptyGameState() {
        GameState state = new GameState(
                false, false, "", "", "", "", "", "",
                0, 0, List.of(), List.of(), Set.of()
        );
        GameResponse response = new GameResponse(false, "not started", state);
        NetGameResponse result = ProtocolMapper.toNet(response);

        assertFalse(result.success());
        assertEquals("not started", result.message());
        assertNotNull(result.gameState());
        assertFalse(result.gameState().started());
        assertTrue(result.gameState().players().isEmpty());
    }

    @Test
    void toNetGameOverState() {
        List<PlayerState> players = new ArrayList<>();
        players.add(new PlayerState(new Player("p1", "Winner", false)));
        players.add(new PlayerState(new Player("p2", "Loser", false)));

        GameState state = new GameState(
                true, true, "p1", "", "", "", "", "",
                3, 1, List.of(), players, Set.of()
        );
        GameResponse response = new GameResponse(true, "game over", state);
        NetGameResponse result = ProtocolMapper.toNet(response);

        assertTrue(result.gameState().gameOver());
        assertEquals("p1", result.gameState().winnerPlayerId());
    }
}
