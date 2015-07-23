package ru.mr123150.draw;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * Created by victorsnesarevsky on 22.07.15.
 */
public class LayerCanvas extends Pane {

    protected GraphicsContext gc;
    protected Canvas activeCanvas;
    @FXML protected Canvas tmpCanvas;

    public LayerCanvas(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("layer_canvas.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        activeCanvas=tmpCanvas;
        gc=activeCanvas.getGraphicsContext2D();
    }

    public GraphicsContext context(){ return gc; }

    public WritableImage snapshot(){ return activeCanvas.snapshot(null,null); }

    public void resize(double width, double height){
        setWidth(width);
        setHeight(height);
        activeCanvas.setWidth(width);
        activeCanvas.setHeight(height);
    }


}
