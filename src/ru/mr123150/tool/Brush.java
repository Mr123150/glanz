package ru.mr123150.tool;

import javafx.scene.canvas.GraphicsContext;

/**
 * Created by victorsnesarevsky on 06.08.15.
 */
public class Brush extends Tool {
    public Brush(){
        this.title="BRUSH";
    }

    public void lineTo(double x, double y){
        gc.lineTo(x,y);
        gc.stroke();
    }

    public void dot(double x, double y){
        gc.fillOval(x-gc.getLineWidth(),y-gc.getLineWidth(),2*gc.getLineWidth(),2*gc.getLineWidth());
    }
}
