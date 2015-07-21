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

    protected double x;
    protected double y;

    public User() throws IOException{
        this.id=0;
        this.address=InetAddress.getLocalHost();
    }

    public User(int id) throws IOException{
        this.id=id;
        this.address=InetAddress.getLocalHost();
    }

    public User(int id,String address) throws IOException{
        this.id=id;
        this.address=InetAddress.getByName(address);
    }

    public int id(){
        return id;
    }

    public InetAddress address(){return address;}

    public String addressText(){return address.getHostAddress();}

    public void setId(int id){
        this.id=id;
    }

    public Color color(){
        return Color.hsb(h,s,b);
    }

    public String colorText(){return h+";"+s+";"+b;}

    public double x(){return x;}

    public double y(){return y;}

    public void setColor(double h, double s, double b){
        this.h=h;
        this.s=s;
        this.b=b;
    }

    public void setCoord(double x, double y){
        this.x=x;
        this.y=y;
    }
}
