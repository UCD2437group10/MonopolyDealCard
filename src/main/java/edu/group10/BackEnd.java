package edu.group10;

import edu.group10.transport.GameServer;

import java.util.logging.Logger;

/**
 * Monopoly Deal —— 后端主入口
 *
 * 启动流程：
 * 1. 创建 GameServer（嵌入式 Jetty + WebSocket）
 * 2. GameServer 内部初始化 GameEngineImpl（游戏引擎）
 * 3. GameWebSocketEndpoint 注册到 /game 路径
 * 4. 开始监听 8080 端口，等待客户端连接
 *
 * 启动后，前端可以通过以下地址连接：
 *   WebSocket: ws://localhost:8080/game
 *   HTTP:      http://localhost:8080
 *
 * 要停止服务器，直接 Ctrl+C 终止进程即可。
 */
public class BackEnd {

    private static final Logger logger = Logger.getLogger(BackEnd.class.getName());

    public static void main(String[] args) {
        // 从命令行参数读取端口号（可选），默认 8080
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0] + ", using default 8080");
            }
        }

        GameServer server = new GameServer(port);

        try {
            // 启动服务器（初始化 WebSocket、游戏引擎）
            server.start();

            // 注册 JVM 关闭钩子 —— 当用户按 Ctrl+C 时，优雅地停止服务器
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("[BackEnd] Shutting down...");
                    server.stop();
                } catch (Exception e) {
                    logger.severe("[BackEnd] Error during shutdown: " + e.getMessage());
                }
            }));

            // 阻塞主线程，让服务器持续运行
            // server.join() 不会返回，直到服务器停止
            server.join();

        } catch (Exception e) {
            logger.severe("[BackEnd] Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}