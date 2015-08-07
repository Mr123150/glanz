package ru.mr123150.tool;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Created by victorsnesarevsky on 05.08.15.
 */
public abstract class Tool {

    protected GraphicsContext gc;
    protected String title="NULL";
    public Tool(){}

    public Tool(GraphicsContext gc){
        this.gc=gc;
    }

    public void setContext(GraphicsContext gc){this.gc=gc;}

    public abstract void lineTo(double x, double y);

    public abstract void dot(double x, double y);

    public String title(){return title;}
}
