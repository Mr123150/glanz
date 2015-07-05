package ru.mr123150.conn;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by victorsnesarevsky on 13.06.15.
 */
public class User {
    protected int id;
    protected InetAddress address;

    protected double h;
    protected double s;
    protected double b;

    public float x;
    public float y;

    public User() throws IOException{
        this.id=0;
        this.address=InetAddress.getLocalHost();
    }

    public User(int id) throws IOException{
        this.id=id;
        this.address=InetAddress.getLocalHost();
    }

    public int id(){
        return id;
    }

    public InetAddress address(){return address;}

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
