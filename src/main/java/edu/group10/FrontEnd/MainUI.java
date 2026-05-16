package edu.group10.FrontEnd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 加载 UIA 画的 FXML 文件
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-view.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Monopoly Deal Card Game");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}