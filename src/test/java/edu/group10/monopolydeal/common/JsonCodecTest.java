package edu.group10.monopolydeal.common;

import static org.junit.jupiter.api.Assertions.*;

import edu.group10.monopolydeal.backend.network.protocol.GameRequest;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.backend.network.protocol.NetGameResponse;
import edu.group10.monopolydeal.backend.network.protocol.NetGameState;
import edu.group10.monopolydeal.backend.network.protocol.ProtocolMapper;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests for JSON serialization/deserialization of protocol objects.
 */
class JsonCodecTest {

    private final JsonCodec codec = new JsonCodec();

    // ========== GameRequest JSON ==========

    @Test
    void serializeGameRequest() {
        GameRequest request = new GameRequest("JOIN", "p1",
                Map.of("name", "Alice", "bot", "false"));
        String json = codec.toJson(request);

        assertNotNull(json);
        assertTrue(json.contains("JOIN"));
        assertTrue(json.contains("p1"));
        assertTrue(json.contains("Alice"));
    }

    @Test
    void deserializeGameRequest() {
        String json = "{\"action\":\"JOIN\",\"playerId\":\"p1\",\"payload\":{\"name\":\"Alice\",\"bot\":\"false\"}}";
        GameRequest request = codec.fromJson(json, GameRequest.class);

        assertEquals("JOIN", request.action());
        assertEquals("p1", request.playerId());
        assertEquals("Alice", request.payload().get("name"));
        assertEquals("false", request.payload().get("bot"));
    }

    @Test
    void gameRequestRoundtrip() {
        GameRequest original = new GameRequest("PLAY_ACTION", "p2",
                Map.of("handIndex", "1", "targetPlayerId", "p3", "color", "Brown"));
        String json = codec.toJson(original);
        GameRequest restored = codec.fromJson(json, GameRequest.class);

        assertEquals(original.action(), restored.action());
        assertEquals(original.playerId(), restored.playerId());
        assertEquals(original.payload().get("handIndex"), restored.payload().get("handIndex"));
        assertEquals(original.payload().get("targetPlayerId"), restored.payload().get("targetPlayerId"));
        assertEquals(original.payload().get("color"), restored.payload().get("color"));
    }

    @Test
    void gameRequestWithEmptyPayload() {
        GameRequest request = new GameRequest("STATE", "p1", Map.of());
        String json = codec.toJson(request);
        GameRequest restored = codec.fromJson(json, GameRequest.class);

        assertEquals("STATE", restored.action());
        assertEquals("p1", restored.playerId());
        assertTrue(restored.payload().isEmpty());
    }

    // ========== GameResponse JSON ==========

    @Test
    void serializeGameResponse() {
        GameResponse response = new GameResponse(true, "game started", null);
        String json = codec.toJson(response);

        assertNotNull(json);
        assertTrue(json.contains("true"));
        assertTrue(json.contains("game started"));
    }

    @Test
    void deserializeGameResponse() {
        String json = "{\"success\":true,\"message\":\"state\",\"gameState\":null}";
        GameResponse response = codec.fromJson(json, GameResponse.class);

        assertTrue(response.success());
        assertEquals("state", response.message());
        assertNull(response.gameState());
    }

    // ========== NetGameResponse JSON round-trip ==========

    @Test
    void netGameResponseRoundtrip() {
        NetGameState netState = new NetGameState(
                false, false, "", "", "", "", "", "",
                10, 3, List.of(), List.of(), Set.of()
        );
        NetGameResponse original = new NetGameResponse(true, "ok", netState);

        String json = codec.toJson(original);
        NetGameResponse restored = codec.fromJson(json, NetGameResponse.class);

        assertTrue(restored.success());
        assertEquals("ok", restored.message());
        assertNotNull(restored.gameState());
        assertEquals(10, restored.gameState().drawPileCount());
        assertEquals(3, restored.gameState().discardPileCount());
        assertFalse(restored.gameState().started());
    }

    // ========== End-to-end JSON flow (mimics server communication) ==========

    @Test
    void endToEndJoinFlow() {
        // Client sends JOIN
        GameRequest joinReq = new GameRequest("JOIN", "p1",
                Map.of("name", "Alice", "bot", "false"));
        String clientJson = codec.toJson(joinReq);

        // Server receives
        GameRequest serverReq = codec.fromJson(clientJson, GameRequest.class);
        assertEquals("JOIN", serverReq.action());
        assertEquals("p1", serverReq.playerId());

        // Server responds
        GameResponse serverResp = new GameResponse(true, "player joined", null);
        String serverJson = codec.toJson(serverResp);

        // Client receives
        GameResponse clientResp = codec.fromJson(serverJson, GameResponse.class);
        assertTrue(clientResp.success());
        assertEquals("player joined", clientResp.message());
    }

    @Test
    void endToEndStateFlow() {
        // Simulate a full state response through JSON
        NetGameState netState = new NetGameState(
                true, false, "", "p1", "", "", "", "",
                15, 2, List.of(), List.of(), Set.of("p1", "p2")
        );
        NetGameResponse netResponse = new NetGameResponse(true, "state", netState);

        // Server serializes to JSON
        String serverJson = codec.toJson(netResponse);

        // Client deserializes from JSON
        NetGameResponse clientNet = codec.fromJson(serverJson, NetGameResponse.class);

        // Client converts to domain
        GameResponse domainResp = ProtocolMapper.toDomain(clientNet);

        assertTrue(domainResp.success());
        assertTrue(domainResp.gameState().started());
        assertEquals("p1", domainResp.gameState().currentPlayerId());
    }

    // ========== Error scenarios ==========

    @Test
    void invalidJsonThrows() {
        assertThrows(IllegalStateException.class, () ->
                codec.fromJson("invalid json {{{", GameRequest.class));
    }

    @Test
    void emptyJsonThrows() {
        assertThrows(IllegalStateException.class, () ->
                codec.fromJson("", GameRequest.class));
    }

    // ========== Various payload types ==========

    @Test
    void numericPayloadValuesAsStrings() {
        GameRequest request = new GameRequest("PLAY_RENT", "p1",
                Map.of("handIndex", "2", "doubleRentCount", "1"));
        String json = codec.toJson(request);
        GameRequest restored = codec.fromJson(json, GameRequest.class);

        assertEquals("2", restored.payload().get("handIndex"));
        assertEquals("1", restored.payload().get("doubleRentCount"));
    }

    @Test
    void booleanPayloadValuesAsStrings() {
        GameRequest request = new GameRequest("READY", "p1", Map.of());
        String json = codec.toJson(request);
        assertNotNull(json);

        GameRequest respJsn = new GameRequest("RESPOND_JSN", "p2",
                Map.of("useCard", "true"));
        String json2 = codec.toJson(respJsn);
        GameRequest restored = codec.fromJson(json2, GameRequest.class);
        assertEquals("true", restored.payload().get("useCard"));
    }
}
