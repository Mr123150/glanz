package ru.mr123150;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import ru.mr123150.conn.Connection;
import ru.mr123150.conn.User;

import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    @FXML Canvas canvas;
    @FXML Canvas hcolor;
    @FXML Canvas color;
    @FXML BorderPane rootPane;
    @FXML HBox topBox;
    @FXML VBox leftBox;
    @FXML VBox rightBox;
    @FXML HBox bottomBox;
    GraphicsContext gc;
    GraphicsContext hc;
    GraphicsContext cc;

    Connection conn=null;
    Connection hconn=null;

    double h,s,b;

    int users=0;

    @Override
    public void initialize(URL url, ResourceBundle rb){
        hc=hcolor.getGraphicsContext2D();
        cc=color.getGraphicsContext2D();

        setHue(0);
        setColor(0, 0);

        gc= canvas.getGraphicsContext2D();
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.lineTo(canvas.getWidth(),0);
        gc.lineTo(canvas.getWidth(), canvas.getHeight());
        gc.lineTo(0, canvas.getHeight());
        gc.lineTo(0, 0);
        //gc.closePath();
        gc.stroke();


        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,event->{
            if(conn!=null&&!conn.users.isEmpty()) gc.setStroke(conn.users.get(0).color());
            else gc.setStroke(Color.hsb(h,s,b));
            gc.fillOval(event.getX(), event.getY(), 2 * gc.getLineWidth(), 2 * gc.getLineWidth());
            send("DRAW;CLICK;"+event.getX()+";"+event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            send("DRAW;PRESS;"+event.getX()+";"+event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if(conn!=null&&!conn.users.isEmpty()) gc.setStroke(conn.users.get(0).color());
            else gc.setStroke(Color.hsb(h,s,b));
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
            send("DRAW;DRAG;"+event.getX()+";"+event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            //gc.closePath();
            send("DRAW;RELEASE");
        });

        hcolor.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->{
            setHue(event.getX()/hcolor.getWidth()*360);
        });

        hcolor.addEventHandler(MouseEvent.MOUSE_DRAGGED, event ->{
            setHue(event.getX()/hcolor.getWidth()*360);
        });

        color.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->{
            setColor(event.getX()/color.getWidth(), 1-event.getY()/color.getHeight());
        });

        color.addEventHandler(MouseEvent.MOUSE_DRAGGED, event ->{
            setColor(event.getX()/color.getWidth(), 1-event.getY()/color.getHeight());
        });
    }

    public void resizeCanvas(double width, double height){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.setWidth(width-leftBox.getWidth()-rightBox.getWidth());
        canvas.setHeight(height-topBox.getHeight()-bottomBox.getHeight());
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.lineTo(canvas.getWidth(), 0);
        gc.lineTo(canvas.getWidth(), canvas.getHeight());
        gc.lineTo(0, canvas.getHeight());
        gc.lineTo(0, 0);
        gc.stroke();
        //gc.closePath();
    }

    public void setHue(double h){
        hc.clearRect(0,0,hcolor.getWidth(),hcolor.getHeight());
        this.h=h;
        for(int i=0;i<hcolor.getWidth();++i){
            hc.setStroke(Color.hsb((double)i/hcolor.getWidth()*360, 1.0, 1.0, 1.0));
            hc.strokeLine(i, 0, i, hcolor.getHeight());
        }
        hc.setStroke(Color.BLACK);
        hc.strokeOval(h/360*hcolor.getWidth()-hcolor.getHeight()/2,0,hcolor.getHeight(),hcolor.getHeight());
        redrawColor();
    }

    public void setColor(double s, double b){
        this.s=s;
        this.b=b;
        redrawColor();
    }

    public void redrawColor(){
        if(conn!=null) {
            conn.users.get(0).setColor(h, s, b);
            send("CHANGE;COLOR;"+h+";"+s+";"+b);
        }
        cc.clearRect(0,0,color.getWidth(),color.getHeight());
        for(int i=0;i<color.getWidth();++i){
            for(int j=0;j<color.getHeight();++j){
                cc.setFill(Color.hsb(h,(double)i/color.getWidth(),1-(double)j/color.getHeight()));
                cc.fillOval(i,j,1,1);
            }
        }
        cc.setStroke(Color.BLACK);
        cc.strokeOval(s*color.getWidth()-hcolor.getHeight()/2,(1-b)*color.getHeight()-hcolor.getHeight()/2,hcolor.getHeight(),hcolor.getHeight());
    }

    @FXML public void connect(){
        try{
            conn=new Connection("192.168.0.110",5050);
            hconn=new Connection("192.168.0.110",5051);
            listen();
            send("CONNECT;REQUEST;"+conn.address());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML public void host(){
        try{
            hconn=new Connection(5050);
            conn=new Connection(5051);
            System.out.println("//SERVER STARTED");
            listen();
            conn.users.insertElementAt(new User(), 0);
            conn.users.get(0).setColor(h,s,b);
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void listen(){
        Task task=new Task<Void>(){
            @Override protected Void call(){
                while(true){
                    try{
                        String str=hconn.receive();
                        if (!str.equals("")) Platform.runLater(()->{receive(str);});
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        break;
                    }
                }
                return null;
            }
        };

        Thread th=new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    public void send(String str){
        if(conn!=null&&(!conn.isServer()||conn.users.size()>0)) {
            try {
                conn.send(str,true);
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    public void send(String str, boolean signature){
        if(conn!=null&&(!conn.isServer()||conn.users.size()>0)) {
            try {
                conn.send(str,signature);
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    public void receive(String str){
        System.out.println(str);
        String arr[]=str.split(";");
        int id;
        try {
            id = Integer.parseInt(arr[arr.length - 1]);
        }
        catch(NumberFormatException e){
            id=-1;
        }
        switch(arr[0]){
            case "CONNECT":
                if(conn.isServer()||arr[arr.length-2].equals(conn.address())){
                    switch(arr[1]) {
                        case "REQUEST":
                            int new_id=conn.users.lastElement().id()+1;
                            try {
                                conn.send("CONNECT;TEST", true);
                                if (true) {
                                    conn.users.add(new User(new_id));
                                    send("CONNECT;ACCEPT;" + new_id + ";" + arr[2]);
                                    send("SYNC;SIZE;" + canvas.getWidth() + ";" + canvas.getHeight() + ";" + arr[2]);
                                    send("SYNC;LAYERS;1" + ";" + arr[2]); //Stub for multi-layers
                                    send("SYNC;DATA;0;data" + ";" + arr[2]); //Stub for data sync
                                } else {
                                    conn.send("CONNECT;REJECT;" + arr[2], true);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "ACCEPT":
                            conn.users.insertElementAt(new User(Integer.parseInt(arr[2])),0);
                            conn.users.insertElementAt(new User(), 1);
                            conn.users.get(0).setColor(h, s, b);
                        default:
                            break;
                    }
                }
                break;
            case "DISCONNECT":
                if(conn.isServer()||Integer.parseInt(arr[1])==id||Integer.parseInt(arr[1])==0) {
                    if (conn.isServer()) {
                        conn.users.remove(conn.getUserById(id));
                    }
                    else {
                        conn=null;
                        hconn=null;
                    }
                    break;
                }
            case "DRAW":
                if(conn.isServer()||id!=conn.users.get(0).id()) {
                    int user_id=conn.getUserById(id);
                    if(user_id!=-1){
                        gc.setStroke(conn.users.get(user_id).color());
                    }
                    else{
                        gc.setStroke(Color.BLACK);
                    }
                    switch (arr[1]) {
                        case "CLICK":
                            gc.fillOval(Double.parseDouble(arr[2]), Double.parseDouble(arr[3]), 2 * gc.getLineWidth(), 2 * gc.getLineWidth());
                            if (conn.isServer()) send(str,false);
                            break;
                        case "PRESS":
                            gc.beginPath();
                            gc.moveTo(Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
                            if (conn.isServer()) send(str,false);
                            break;
                        case "DRAG":
                            gc.lineTo(Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
                            gc.stroke();
                            if (conn.isServer()) send(str,false);
                            break;
                        case "RELEASE":
                            //gc.closePath();
                            if (conn.isServer()) send(str,false);
                            break;
                        default:
                            break;
                    }
                }
                break;
            case "CHANGE":
                if(conn.isServer()||id!=conn.users.get(0).id()) {
                    int user_id=conn.getUserById(id);
                    if(user_id!=-1) {
                        switch (arr[1]) {
                            case "COLOR":
                                conn.users.get(user_id).setColor(Double.parseDouble(arr[2]),Double.parseDouble(arr[3]),Double.parseDouble(arr[4]));
                                break;
                            default:
                                break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    public void disconnect(){
        send("DISCONNECT");
    }
}
