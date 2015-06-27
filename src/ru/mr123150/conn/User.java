package ru.mr123150.conn;

import javafx.scene.paint.Color;

/**
 * Created by victorsnesarevsky on 13.06.15.
 */
public class User {
    protected int id;
    protected String address;

    protected double h;
    protected double s;
    protected double b;

    public float x;
    public float y;

    public User(){
        this.address=null;
        this.id=0;
    }

    public User(String address){
        this.id=-1;
        this.address=address;
    }

    public User(int id, String address){
        this.id=id;
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
