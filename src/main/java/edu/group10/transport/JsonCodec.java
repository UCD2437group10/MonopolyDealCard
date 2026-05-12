package edu.group10.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.group10.common.model.GameMessage;
import edu.group10.common.model.GameActionResult;
import edu.group10.common.model.GameState;
import edu.group10.common.model.PlayerAction;

/**
 * JSON 编解码器（单例模式）
 *
 * 职责：Java 对象 与 JSON 字符串 之间的双向转换。
 *
 * 为什么用单例？
 * ObjectMapper 是线程安全的，创建成本较高（需要扫描类路径、构建序列化器缓存）。
 * 全局共用一个实例可以避免重复创建，提升性能。
 *
 * 核心 API（Jackson 的 ObjectMapper）：
 * - writeValueAsString(obj)  → Java对象 → JSON字符串（序列化）
 * - readValue(json, Class)    → JSON字符串 → Java对象（反序列化）
 *
 * 本项目中的使用流程：
 * 1. 客户端发来 JSON → decodeMessage(json) → GameMessage 对象 → MessageRouter 处理
 * 2. 服务器返回结果 → encodeResult(result) 或 encodeState(state) → JSON → 发回客户端
 */
public class JsonCodec {

    /** 全局唯一的 ObjectMapper 实例 */
    private static final ObjectMapper MAPPER = createMapper();

    /** 单例 */
    private static final JsonCodec INSTANCE = new JsonCodec();

    private JsonCodec() {
        // 私有构造器，防止外部 new
    }

    public static JsonCodec getInstance() {
        return INSTANCE;
    }

    /**
     * 工厂方法：创建并配置 ObjectMapper
     *
     * 配置说明：
     * - FAIL_ON_UNKNOWN_PROPERTIES = false
     *   客户端可能发了多余的字段（如调试信息），不要因此报错，忽略即可
     * - JavaTimeModule
     *   让 Jackson 能正确处理 Java 8 的时间类型（Instant、LocalDateTime 等）
     * - INDENT_OUTPUT = false
     *   WebSocket 消息不需要换行缩进（节省带宽），调试时改为 true
     */
    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 忽略 JSON 中未知的字段，保证向前兼容
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 注册 Java 8 时间模块（处理 Instant、LocalDateTime 等）
        mapper.registerModule(new JavaTimeModule());

        // 时间戳不要序列化成数组 [2026,5,12,10,30,0]，而是 ISO 字符串 "2026-05-12T10:30:00Z"
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
    }

    /**
     * 暴露 ObjectMapper 供特殊场景使用（如需要手动处理 JsonNode）
     */
    public ObjectMapper getMapper() {
        return MAPPER;
    }

    // ======================== 序列化（Java对象 → JSON字符串） ========================

    /**
     * 将任意 Java 对象序列化为 JSON 字符串
     *
     * @param obj 要序列化的对象
     * @return JSON 字符串，如果序列化失败则返回错误 JSON
     */
    public String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // 序列化失败时返回一个错误 JSON，避免客户端收到空响应
            return "{\"success\":false,\"errorCode\":\"SERIALIZATION_ERROR\",\"errorMessage\":\"" +
                    escapeJson(e.getMessage()) + "\"}";
        }
    }

    /**
     * 将 GameActionResult 序列化为 JSON
     * （MessageRouter 中最常用的方法）
     */
    public String encodeResult(GameActionResult result) {
        return toJson(result);
    }

    /**
     * 将 GameState 序列化为 JSON
     * （用于广播状态更新、断线重连回复）
     */
    public String encodeState(GameState state) {
        return toJson(state);
    }

    // ======================== 反序列化（JSON字符串 → Java对象） ========================

    /**
     * 将 JSON 字符串反序列化为 GameMessage 信封
     *
     * 这是 WebSocket 消息处理的入口 ——
     * 所有客户端发来的消息都先经过此方法，解析为统一的 GameMessage 格式。
     *
     * @param json 客户端发来的原始 JSON 字符串
     * @return GameMessage 对象，解析失败返回 null
     */
    public GameMessage decodeMessage(String json) {
        try {
            return MAPPER.readValue(json, GameMessage.class);
        } catch (JsonProcessingException e) {
            System.err.println("[JsonCodec] Failed to decode GameMessage: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将 payload（JsonNode）反序列化为 PlayerAction
     *
     * 为什么分两步（先解析 GameMessage，再解析 payload）？
     * 因为 payload 的内容取决于 type 字段 ——
     * type="PLAYER_ACTION" 时 payload 是 PlayerAction，
     * type="CREATE_GAME" 时 payload 是 {playerIds, playerNames}。
     * 分步解析让 MessageRouter 能根据 type 选择正确的解析方式。
     *
     * @param payloadNode GameMessage 中的 payload 字段（JsonNode）
     * @return PlayerAction 对象，解析失败返回 null
     */
    public PlayerAction decodePlayerAction(JsonNode payloadNode) {
        if (payloadNode == null) {
            return null;
        }
        try {
            // Jackson 的 treeToValue：把 JsonNode 树转换为指定 Java 类型
            return MAPPER.treeToValue(payloadNode, PlayerAction.class);
        } catch (JsonProcessingException e) {
            System.err.println("[JsonCodec] Failed to decode PlayerAction: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的 Java 对象（通用方法）
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @return 反序列化后的对象，失败返回 null
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            System.err.println("[JsonCodec] Failed to decode " + clazz.getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    // ======================== 工具方法 ========================

    /**
     * 转义 JSON 字符串中的特殊字符
     * 用于构造错误消息时的简单防御，防止序列化错误信息中包含引号等破坏 JSON 结构
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}