package ru.mr123150;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader loader=new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();

        Controller controller=loader.getController();

        Scene scene=new Scene(root, 1024, 768);
        stage.setTitle("Canvas");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            controller.disconnect();
        });

        controller.canvas.setOnMouseEntered(event -> {
            scene.setCursor(controller.me().tool().cursor());
        });

        controller.canvas.setOnMouseExited(event -> {
            scene.setCursor(Cursor.DEFAULT);
        });

        scene.widthProperty().addListener((ObservableValue<? extends Number> ov, Number oldVal, Number newVal) -> {
            controller.fitPane();
        });

        scene.heightProperty().addListener((ObservableValue<? extends Number> ov, Number oldVal, Number newVal) -> {
            controller.fitPane();
        });

        stage.setMaximized(true);
        stage.show();

        controller.fitPane();
        controller.fitCanvas();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
