package ru.mr123150.draw;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
        for(Node node:getChildren()) {
            Canvas canvas=(Canvas)node;
            canvas.setWidth(width);
            canvas.setHeight(height);
        }
    }

    public void add(){
        Canvas newCanvas = new Canvas();
        newCanvas.setWidth(getWidth());
        newCanvas.setHeight(getHeight());
        int id=Integer.parseInt(getChildren().get(getChildren().size()-1).getId())+1;
        newCanvas.setId(id+"");
        getChildren().add(newCanvas);
    }

    public void add(String id){
        Canvas newCanvas = new Canvas();
        newCanvas.setWidth(getWidth());
        newCanvas.setHeight(getHeight());
        newCanvas.setId(id);
        getChildren().add(newCanvas);
    }

    public Canvas find(String id){
        for(Node node:getChildren()){
            if(node.getId().equals(id))
                return (Canvas)node;
        }
        return null;
    }

    public void select(String id){
        Canvas canvas=find(id);
        if(canvas!=null) activeCanvas=canvas;
    }


}
