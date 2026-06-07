package edu.group10.monopolydeal.frontend.app;

import edu.group10.monopolydeal.frontend.context.FrontendContext;
import javafx.fxml.FXMLLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Launches the JavaFX desktop client.
 */
public class GameClientApp extends Application {

    /** Starts the JavaFX application lifecycle. */
    public static void launchApp(String[] args) {
        launch(args);
    }

    /** Builds and shows the main application window. */
    @Override
    public void start(Stage stage) {
        stage.setTitle("Monopoly Deal");
        Parent root = loadRoot();
        root.getStyleClass().add("main-root");
        Scene scene = new Scene(root, 1280, 920);
        String css = getClass().getResource("/css/main-theme.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.setMinWidth(1180);
        stage.setMinHeight(860);
        stage.show();
    }

    /** Loads the main FXML root after the shared client is prepared. */
    private Parent loadRoot() {
        if (FrontendContext.gameClient() == null) {
            throw new IllegalStateException("game client not initialized");
        }
        try {
            return FXMLLoader.load(getClass().getResource("/fxml/main-view.fxml"));
        } catch (IOException exception) {
            throw new IllegalStateException("failed to load fxml", exception);
        }
    }
}
