package edu.group10;

import edu.group10.transport.GameServer;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * Monopoly Deal —— 后端主入口
 *
 * 启动流程：
 * 1. 创建 GameServer（TCP Socket 模式）
 * 2. GameServer 内部初始化 GameEngine + GameCommandService
 * 3. 开始监听指定端口，等待客户端连接
 *
 * 启动后，客户端可以通过 TCP Socket 连接到对应端口，
 * 发送换行分隔的 JSON 请求。
 *
 * 要停止服务器，直接 Ctrl+C 终止进程即可。
 */
public class BackEnd {

    private static final Logger logger = Logger.getLogger(BackEnd.class.getName());

    public static void main(String[] args) {
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
            server.start();
            logger.info("[BackEnd] ========================================");
            logger.info("[BackEnd]  Monopoly Deal 游戏服务器已启动!");
            logger.info("[BackEnd]  TCP 地址: localhost:" + port);
            logger.info("[BackEnd] ========================================");

            // 注册 JVM 关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("[BackEnd] Shutting down...");
            }));

            // 保持主线程存活（accept 线程是 daemon，主线程退出则 JVM 退出）
            new CountDownLatch(1).await();

        } catch (Exception e) {
            logger.severe("[BackEnd] Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}