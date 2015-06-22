package ru.mr123150;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import ru.mr123150.conn.Connection;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    @FXML Canvas canvas;
    @FXML Canvas hcolor;
    @FXML Canvas color;
    @FXML BorderPane rootPane;
    @FXML VBox rightBox;
    GraphicsContext gc;
    GraphicsContext hc;
    GraphicsContext cc;

    Connection conn=null;
    Connection hconn=null;

    double width=0;
    double height=0;

    double h,s,b;

    int users=0;

    @Override
    public void initialize(URL url, ResourceBundle rb){
        hc=hcolor.getGraphicsContext2D();
        cc=color.getGraphicsContext2D();

        setHue(0);
        setColor(0,0);

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
            gc.fillOval(event.getX(), event.getY(), 2 * gc.getLineWidth(), 2 * gc.getLineWidth());
            send("DRAW;CLICK;"+event.getX()+";"+event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            send("DRAW;PRESS;"+event.getX()+";"+event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
            send("DRAW;DRAG;"+event.getX()+";"+event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            //gc.closePath();
            send("DRAW;RELEASE");
        });

        hcolor.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->{
            setHue(event.getX());
        });

        hcolor.addEventHandler(MouseEvent.MOUSE_DRAGGED, event ->{
            setHue(event.getX());
        });

        color.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->{
            setColor(event.getX(),event.getY());
        });

        color.addEventHandler(MouseEvent.MOUSE_DRAGGED, event ->{
            setColor(event.getX(),event.getY());
        });
    }

    public void setSize(double width, double height){
        this.width=width;
        this.height=height;
        resizeCanvas();
    }

    public void resizeCanvas(){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        System.out.println(rightBox.getWidth());
        canvas.setWidth(width-360);
        canvas.setHeight(height-50); //TODO
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
        this.h=h/hcolor.getWidth()*360;
        for(int i=0;i<hcolor.getWidth();++i){
            hc.setStroke(Color.hsb((double)i/hcolor.getWidth()*360, 1.0, 1.0, 1.0));
            hc.strokeLine(i, 0, i, hcolor.getHeight());
        }
        hc.setStroke(Color.BLACK);
        hc.strokeOval(h-hcolor.getHeight()/2,0,hcolor.getHeight(),hcolor.getHeight());
        redrawColor();
    }

    public void setColor(double s, double b){
        this.s=s/color.getWidth();
        this.b=1-b/color.getHeight();
        redrawColor();
    }

    public void redrawColor(){
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
            send("CONNECT;REQUEST");
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
        if(conn!=null&&(!conn.isHost()||users>0)) {
            try {
                //if (!conn.isHost()) str += (";" + conn.getAddress());
                conn.send(str);
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    public void receive(String str){
        System.out.println(str);
        String arr[]=str.split(";");
        switch(arr[0]){
            case "CONNECT":
                if(conn.isHost()||arr[arr.length-1].equals(conn.getAddress())){
                    switch(arr[1]) {
                        case "REQUEST":
                            ++users;
                            try {
                                conn.send("CONNECT;TEST");
                                if (true) {
                                    conn.send("CONNECT;ACCEPT;" + arr[2]);
                                    conn.send("SYNC;SIZE;" + canvas.getWidth() + ";" + canvas.getHeight());
                                    conn.send("SYNC;LAYERS;1"); //Stub for multi-layers
                                    conn.send("SYNC;DATA;0;data"); //Stub for data sync
                                } else {
                                    conn.send("CONNECT;REJECT;" + arr[2]);
                                    --users;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                }
                break;
            case "DISCONNECT":
                if(conn.isHost()||arr[arr.length-1].equals(conn.getAddress())) {
                    if (conn.isHost()) --users;
                    else conn = null;
                    break;
                }
            case "DRAW":
                if(conn.isHost()||!arr[arr.length-1].equals(conn.getAddress())) {
                    switch (arr[1]) {
                        case "CLICK":
                            gc.fillOval(Double.parseDouble(arr[2]), Double.parseDouble(arr[3]), 2 * gc.getLineWidth(), 2 * gc.getLineWidth());
                            if (conn.isHost()) send(str);
                            break;
                        case "PRESS":
                            gc.beginPath();
                            gc.moveTo(Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
                            if (conn.isHost()) send(str);
                            break;
                        case "DRAG":
                            gc.lineTo(Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
                            gc.stroke();
                            if (conn.isHost()) send(str);
                            break;
                        case "RELEASE":
                            //gc.closePath();
                            if (conn.isHost()) send(str);
                            break;
                        default:
                            break;
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
