package ru.mr123150.draw;

import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * Created by victorsnesarevsky on 22.07.15.
 */
public class LayerCanvas extends Pane {

    public LayerCanvas(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("layer_canvas.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    GraphicsContext gc;
    Canvas activeCanvas;
    
}
