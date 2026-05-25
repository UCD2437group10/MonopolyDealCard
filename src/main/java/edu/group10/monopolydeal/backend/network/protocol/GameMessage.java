package edu.group10.monopolydeal.backend.network.protocol;

/**
 * 通信消息体。
 */
public record GameMessage(String type, String payload) {
}
