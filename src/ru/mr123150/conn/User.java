package ru.mr123150.conn;

import javafx.scene.paint.Color;

/**
 * Created by victorsnesarevsky on 13.06.15.
 */
public class User {
    protected int id=0;
    protected String address;

    protected float x;
    protected float y;

    protected double h;
    protected double s;
    protected double b;

    public User(){
        this.address=null;
    }

    public User(String address){
        this.address=address;
    }

    public int id(){
        return id;
    }

    public void setId(int id){
        this.id=id;
    }

    public Color color(){
        return Color.hsb(h,s,b);
    }

    public void setColor(double h, double s, double b){
        this.h=h;
        this.s=s;
        this.b=b;
    }
}
