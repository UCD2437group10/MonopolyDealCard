plugins {
    id("java")
}

group = "edu.group10"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // ======================== 测试框架 ========================
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // ======================== JSON 序列化：Jackson ========================
    // Jackson 是 Java 生态中最主流的 JSON 库
    // jackson-databind：核心库，提供 ObjectMapper（Java对象 ↔ JSON字符串 互相转换）
    // jackson-datatype-jsr310：让 Java 8 时间类型（LocalDateTime、Instant）也能正确序列化
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")

    // ======================== WebSocket 服务器：Jetty ========================
    // 为什么选 Jetty？
    // 1. 轻量级，可嵌入 main() 启动，不需要安装 Tomcat
    // 2. 对 JSR 356（javax.websocket）注解方式支持完善
    // 3. 无需引入 Spring Boot，保持项目小而精
    //
    // Jetty 10 使用 javax 命名空间（Java EE），Jetty 11 改用 jakarta 命名空间（Jakarta EE）
    // 我们选择 Jetty 10 + javax.websocket，因为文档更多、社区更熟
    implementation("org.eclipse.jetty:jetty-server:10.0.24")
    implementation("org.eclipse.jetty:jetty-servlet:10.0.24")
    // websocket-javax-server = Jetty 对 JSR 356 (javax.websocket) 的实现
    implementation("org.eclipse.jetty.websocket:websocket-javax-server:10.0.24")
}

tasks.test {
    useJUnitPlatform()
}