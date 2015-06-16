package ru.mr123150;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader loader=new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        Controller controller=loader.getController();
        controller.resizeCanvas(primaryScreenBounds.getWidth(),primaryScreenBounds.getHeight()-50);

        Scene scene=new Scene(root, 300, 275);
        stage.setTitle("Canvas");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            controller.disconnect();

        });

        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());

        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
