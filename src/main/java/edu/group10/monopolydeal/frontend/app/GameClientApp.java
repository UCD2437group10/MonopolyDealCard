package edu.group10.monopolydeal.frontend.app;

import edu.group10.monopolydeal.frontend.context.FrontendContext;
import javafx.fxml.FXMLLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * JavaFX client entry point.
 */
public class GameClientApp extends Application {

    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Monopoly Deal");
        Parent root = loadRoot();
        root.getStyleClass().add("main-root");
        Scene scene = new Scene(root, 1100, 760);
        String css = getClass().getResource("/css/main-theme.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }

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
