package edu.group10.monopolydeal.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides JSON serialization and deserialization helpers.
 */
public class JsonCodec {

    /** Shared Jackson mapper used by the client and server. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Serializes an object to JSON text. */
    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize object", exception);
        }
    }

    /** Deserializes JSON text into the requested type. */
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize json", exception);
        }
    }
}
