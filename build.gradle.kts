plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "edu.group10"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.16")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")

    implementation("org.eclipse.jetty:jetty-server:10.0.24")
    implementation("org.eclipse.jetty:jetty-servlet:10.0.24")
    implementation("org.eclipse.jetty.websocket:websocket-javax-server:10.0.24")
}

application {
    mainClass = "edu.group10.FrontEnd.MainUI"
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass = "edu.group10.monopolydeal.launcher.GameLauncher"
}

tasks.test {
    useJUnitPlatform()
}
