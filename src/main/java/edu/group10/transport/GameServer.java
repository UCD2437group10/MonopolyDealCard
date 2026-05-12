package edu.group10.transport;

import edu.group10.core.GameEngineImpl;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.javax.server.config.JavaxWebSocketServletContainerInitializer;

import java.util.logging.Logger;

/**
 * 嵌入式 WebSocket 游戏服务器
 *
 * 职责：
 * 1. 创建并配置 Jetty HTTP 服务器
 * 2. 注册 WebSocket 端点（GameWebSocketEndpoint）
 * 3. 初始化游戏引擎和消息路由
 * 4. 提供 start() / stop() 生命周期管理
 *
 * 为什么用嵌入式服务器？
 * 传统的 Java Web 应用需要打成 WAR 包部署到 Tomcat/Jetty 容器中。
 * 嵌入式服务器则是"服务器在代码里" —— 在 main() 中直接 new 一个 Server 对象并启动。
 * 好处：
 * - 无需安装、配置外部服务器
 * - 一键启动（java -jar monopoly.jar）
 * - 适合演示答辩（评委不用搭环境，直接跑）
 *
 * 架构层次（自上而下）：
 *   GameServer           ← 本类：HTTP 服务器 + 端口 + 启动/停止
 *       │
 *   GameWebSocketEndpoint  ← WebSocket 端点：管理每个连接的 onOpen/onMessage/onClose
 *       │
 *   MessageRouter        ← 消息路由器：根据 type 分派到对应处理方法
 *       │
 *   GameEngineImpl       ← 游戏引擎：执行核心业务逻辑
 *
 * 端口说明：
 *   默认 8080，如果被占用可以改为其他端口（如 8081、9000 等）。
 *   浏览器中通过 ws://localhost:8080/game 连接。
 */
public class GameServer {

    private static final Logger logger = Logger.getLogger(GameServer.class.getName());

    /** 默认监听端口 */
    private static final int DEFAULT_PORT = 8080;

    /** Jetty 服务器实例 */
    private final Server server;

    /** 服务器监听端口 */
    private final int port;

    /** 游戏引擎 */
    private final GameEngineImpl gameEngine;

    // ======================== 构造器 ========================

    /**
     * 创建游戏服务器
     *
     * @param port 监听端口号
     */
    public GameServer(int port) {
        this.port = port;
        this.server = new Server(port);
        this.gameEngine = new GameEngineImpl();
    }

    /**
     * 使用默认端口（8080）创建游戏服务器
     */
    public GameServer() {
        this(DEFAULT_PORT);
    }

    // ======================== 生命周期管理 ========================

    /**
     * 启动服务器
     *
     * 启动过程：
     * 1. 配置 Jetty —— 注册 WebSocket 端点
     * 2. 启动 HTTP 服务器 —— 开始监听端口
     * 3. 初始化游戏通信组件 —— 连接 GameWebSocketEndpoint 和 GameEngineImpl
     *
     * @throws Exception 如果端口被占用或配置错误
     */
    public void start() throws Exception {
        // ---- 第一步：配置 WebSocket ----
        configureWebSocket();

        // ---- 第二步：启动 HTTP 服务器 ----
        server.start();
        logger.info("[GameServer] ========================================");
        logger.info("[GameServer]  Monopoly Deal 游戏服务器已启动!");
        logger.info("[GameServer]  WebSocket 地址: ws://localhost:" + port + "/game");
        logger.info("[GameServer]  HTTP 地址:    http://localhost:" + port);
        logger.info("[GameServer] ========================================");

        // ---- 第三步：初始化通信组件 ----
        // 把 GameEngineImpl 注入到 WebSocket 端点中，
        // 这样所有连接都能通过 MessageRouter 访问游戏引擎
        GameWebSocketEndpoint.initialize(gameEngine);
    }

    /**
     * 停止服务器
     *
     * 优雅关闭：先停止接收新连接，再等待现有请求处理完成。
     */
    public void stop() throws Exception {
        logger.info("[GameServer] Stopping server...");
        server.stop();
        logger.info("[GameServer] Server stopped.");
    }

    /**
     * 阻塞等待服务器停止
     *
     * 调用 server.join() 后，当前线程（main 线程）会一直阻塞，
     * 直到服务器被 stop() 或者 JVM 退出。
     *
     * 这样 main() 方法不会执行完就退出，服务器持续运行。
     */
    public void join() throws InterruptedException {
        server.join();
    }

    // ======================== 配置 ========================

    /**
     * 配置 Jetty 的 WebSocket 支持
     *
     * 这里用的是 JSR 356 标准 API（javax.websocket）+ Jetty 的实现。
     *
     * JavaxWebSocketServletContainerInitializer：
     * Jetty 提供的工具类，用于在嵌入式模式下配置 javax.websocket 端点。
     *
     * 配置流程：
     * 1. 创建 ServletContextHandler —— 相当于一个最小的 Web 应用上下文
     * 2. 通过 JavaxWebSocketServletContainerInitializer 注册 @ServerEndpoint 类
     * 3. 把 ServletContextHandler 挂到 Jetty Server 上
     *
     * 这样 Jetty 在启动时会扫描 @ServerEndpoint 注解，
     * 发现 GameWebSocketEndpoint 并将其注册到 /game 路径。
     */
    private void configureWebSocket() {
        // 创建 Servlet 上下文处理器，挂在根路径 "/"
        ServletContextHandler contextHandler = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        server.setHandler(contextHandler);

        // 初始化 javax.websocket 支持
        // configure() 方法会：
        // 1. 扫描 classpath 上的 @ServerEndpoint 注解
        // 2. 为每个找到的端点创建 WebSocket 路由
        // 3. 连接 Jetty 的 WebSocket 实现与 JSR 356 标准 API
        JavaxWebSocketServletContainerInitializer.configure(contextHandler,
                (servletContext, container) -> {
                    // 设置默认超时时间（毫秒）
                    // 30 分钟无活动后自动断开（防止僵尸连接）
                    container.setDefaultMaxSessionIdleTimeout(30 * 60 * 1000);

                    // 添加 @ServerEndpoint 注解的端点类
                    // 由于 GameWebSocketEndpoint 有 @ServerEndpoint("/game") 注解，
                    // 显式注册可以确保它被正确加载
                    container.addEndpoint(GameWebSocketEndpoint.class);

                    logger.info("[GameServer] WebSocket endpoint registered: " +
                            GameWebSocketEndpoint.class.getSimpleName());
                });

        logger.info("[GameServer] WebSocket configured on port " + port);
    }

    // ======================== 查询方法 ========================

    /**
     * 获取游戏引擎实例（调试/测试用）
     */
    public GameEngineImpl getGameEngine() {
        return gameEngine;
    }

    /**
     * 获取监听端口
     */
    public int getPort() {
        return port;
    }
}
