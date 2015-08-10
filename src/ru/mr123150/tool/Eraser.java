package ru.mr123150.tool;

import javafx.scene.paint.Color;

/**
 * Created by victorsnesarevsky on 10.08.15.
 */
public class Eraser extends Tool {
    public Eraser(){this.title="ERASER";}

    public void lineTo(double x, double y){
        gc.setStroke(Color.WHITE);
        gc.lineTo(x,y);
        gc.stroke();
    }

    public void dot(double x, double y){
        gc.setFill(Color.WHITE);
        gc.fillOval(x-gc.getLineWidth()/2,y-gc.getLineWidth()/2,gc.getLineWidth(),gc.getLineWidth());
    }
}
