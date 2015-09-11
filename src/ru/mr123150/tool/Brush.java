package ru.mr123150.tool;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Created by victorsnesarevsky on 06.08.15.
 */
public class Brush extends Tool {
    public Brush(){
        this.title="BRUSH";
    }

    public Color lineTo(double fromX, double fromY, double x, double y){
        gc.lineTo(x,y);
        gc.stroke();
        return null;
    }

    public Color dot(double x, double y){
        gc.fillOval(x-gc.getLineWidth()/2,y-gc.getLineWidth()/2,gc.getLineWidth(),gc.getLineWidth());
        return null;
    }
}
