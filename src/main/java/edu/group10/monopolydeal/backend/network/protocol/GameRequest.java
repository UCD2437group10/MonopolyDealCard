package edu.group10.monopolydeal.backend.network.protocol;

import java.util.Map;

/**
 * 前端到后端的命令请求。
 */
public record GameRequest(String action, String playerId, Map<String, String> payload) {
}
