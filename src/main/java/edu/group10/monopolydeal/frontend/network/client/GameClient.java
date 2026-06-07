package edu.group10.monopolydeal.frontend.network.client;

import edu.group10.monopolydeal.backend.network.protocol.GameRequest;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.backend.network.protocol.NetGameResponse;
import edu.group10.monopolydeal.backend.network.protocol.ProtocolMapper;
import edu.group10.monopolydeal.backend.network.server.GameServer;
import edu.group10.monopolydeal.common.JsonCodec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frontend client that can talk to a local server or a TCP server.
 */
public class GameClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameClient.class);
    /** JSON codec shared by local and TCP request paths. */
    private final JsonCodec jsonCodec = new JsonCodec();
    /** Optional in-process server used by local mode. */
    private GameServer localServer;
    /** Active TCP socket when running in remote mode. */
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    /** Opens a TCP connection unless a local server is already bound. */
    public void connect(String host, int port) {
        if (localServer != null) {
            LOGGER.info("Using local bound server, skip tcp connect {}:{}", host, port);
            return;
        }
        try {
            closeSocketQuietly();
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            LOGGER.info("Connected to {}:{}", host, port);
        } catch (Exception exception) {
            closeSocketQuietly();
            throw new IllegalStateException("connect failed: " + exception.getMessage(), exception);
        }
    }


    /** Sends one command and returns the mapped domain response. */
    public synchronized GameResponse send(String action, String playerId, Map<String, String> payload) {
        GameRequest request = new GameRequest(action, playerId, payload);
        if (localServer != null) {
            return localServer.handle(request);
        }
        if (socket == null || writer == null || reader == null || socket.isClosed()) {
            throw new IllegalStateException("client is not connected to server");
        }
        try {
            writer.write(jsonCodec.toJson(request));
            writer.newLine();
            writer.flush();
            String line = reader.readLine();
            if (line == null) {
                throw new IllegalStateException("server closed connection");
            }
            NetGameResponse netResponse = jsonCodec.fromJson(line, NetGameResponse.class);
            return ProtocolMapper.toDomain(netResponse);
        } catch (Exception exception) {
            throw new IllegalStateException("send failed: " + exception.getMessage(), exception);
        }
    }

    /** Closes the current socket resources without throwing. */
    private void closeSocketQuietly() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
        reader = null;
        writer = null;
        socket = null;
    }
}
