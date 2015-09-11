package ru.mr123150.tool;

import javafx.scene.Cursor;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Created by victorsnesarevsky on 11.08.15.
 */
public class Picker extends Tool {
    public Picker(){
        title="PICKER";
        returnable=true;
        action=false;
        cursor=Cursor.CROSSHAIR;
    }

    public Color lineTo(double fromX, double fromY, double x, double y){
        return gc.getCanvas().snapshot(null,null).getPixelReader().getColor((int) x, (int) y);
    }

    public Color dot(double x,double y){
        return gc.getCanvas().snapshot(null,null).getPixelReader().getColor((int) x, (int) y);
    }
}
