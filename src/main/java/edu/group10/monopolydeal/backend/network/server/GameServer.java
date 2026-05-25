package edu.group10.monopolydeal.backend.network.server;

import edu.group10.monopolydeal.backend.game.GameEngine;
import edu.group10.monopolydeal.backend.network.protocol.GameRequest;
import edu.group10.monopolydeal.backend.network.protocol.GameResponse;
import edu.group10.monopolydeal.backend.network.protocol.ProtocolMapper;
import edu.group10.monopolydeal.backend.service.DeckService;
import edu.group10.monopolydeal.backend.service.GameCommandService;
import edu.group10.monopolydeal.common.JsonCodec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端门面：当前先提供统一命令入口，后续再接真实 socket 监听。
 */
public class GameServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameServer.class);
    private final int port;
    private final JsonCodec jsonCodec = new JsonCodec();
    private final GameCommandService commandService;
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private volatile boolean started;

    public GameServer(int port) {
        this.port = port;
        this.commandService = new GameCommandService(new GameEngine(new DeckService()));
    }

    public void start() {
        if (started) {
            return;
        }
        started = true;
        Thread acceptThread = new Thread(this::runAcceptLoop, "md-server-accept-" + port);
        acceptThread.setDaemon(true);
        acceptThread.start();
        LOGGER.info("Game server started on port {} (tcp mode)", port);
    }

    public GameResponse handle(GameRequest request) {
        return commandService.handle(request);
    }

    public String handleRaw(String jsonRequest) {
        GameRequest request = jsonCodec.fromJson(jsonRequest, GameRequest.class);
        GameResponse response = commandService.handle(request);
        return jsonCodec.toJson(ProtocolMapper.toNet(response));
    }

    public GameCommandService commandService() {
        return commandService;
    }

    private void runAcceptLoop() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (started) {
                Socket socket = serverSocket.accept();
                clientPool.submit(() -> handleClient(socket));
            }
        } catch (Exception exception) {
            LOGGER.error("Server accept loop stopped: {}", exception.getMessage(), exception);
        }
    }

    private void handleClient(Socket socket) {
        String remote = socket.getRemoteSocketAddress() == null ? "unknown" : socket.getRemoteSocketAddress().toString();
        LOGGER.info("Client connected: {}", remote);
        try (socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String responseJson;
                try {
                    responseJson = handleRaw(line);
                } catch (Exception exception) {
                    responseJson = jsonCodec.toJson(new GameResponse(false, "bad request: " + exception.getMessage(), null));
                }
                writer.write(responseJson);
                writer.newLine();
                writer.flush();
            }
        } catch (Exception exception) {
            LOGGER.info("Client disconnected: {} ({})", remote, exception.getMessage());
        }
    }
}
