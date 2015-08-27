package ru.mr123150.tool;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Created by victorsnesarevsky on 05.08.15.
 */
public abstract class Tool {

    protected GraphicsContext gc;
    protected String title="NULL";
    protected boolean returnable=false;
    protected Cursor cursor=null;
    public Tool(){}

    public Tool(GraphicsContext gc){
        this.gc=gc;
    }

    public void setContext(GraphicsContext gc){this.gc=gc;}

    public boolean returnable(){return this.returnable;}

    public abstract Color lineTo(double x, double y);

    public abstract Color dot(double x, double y);

    public String title(){return title;}

    public Cursor cursor(){return cursor!=null?cursor:Cursor.DEFAULT;}
}
