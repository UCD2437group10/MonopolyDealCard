package edu.group10.transport;

import edu.group10.monopolydeal.backend.game.GameEngine;
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
 * Provides in-process command handling and optional socket access.
 */
public class GameServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameServer.class);
    /** TCP port used when socket mode is active. */
    private final int port;
    /** Shared JSON codec for socket requests and responses. */
    private final JsonCodec jsonCodec = new JsonCodec();
    /** Command bridge that talks to the game engine. */
    private final GameCommandService commandService;
    /** Worker pool for connected TCP clients. */
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private volatile boolean started;

    /** Creates a game server with a fresh engine instance. */
    public GameServer(int port) {
        this.port = port;
        this.commandService = new GameCommandService(new GameEngine(new DeckService()));
    }

    /** Starts the background TCP accept loop once. */
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

    /** Handles a request directly without going through sockets. */
    public GameResponse handle(GameRequest request) {
        return commandService.handle(request);
    }

    /** Handles a raw JSON request and returns a raw JSON response. */
    public String handleRaw(String jsonRequest) {
        GameRequest request = jsonCodec.fromJson(jsonRequest, GameRequest.class);
        GameResponse response = commandService.handle(request);
        return jsonCodec.toJson(ProtocolMapper.toNet(response));
    }


    /** Accepts incoming TCP clients until the server stops. */
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

    /** Processes newline-delimited requests for one connected client. */
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
