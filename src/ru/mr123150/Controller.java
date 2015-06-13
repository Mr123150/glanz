package ru.mr123150;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import ru.mr123150.conn.Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    @FXML Canvas canvas;
    @FXML BorderPane rootPane;
    GraphicsContext gc;

    ServerSocket ss = null;
    Socket clientSocket = null;
    DataOutputStream os = null;

    @Override
    public void initialize(URL url, ResourceBundle rb){
        gc= canvas.getGraphicsContext2D();
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.lineTo(canvas.getWidth(),0);
        gc.lineTo(canvas.getWidth(), canvas.getHeight());
        gc.lineTo(0, canvas.getHeight());
        gc.lineTo(0, 0);
        gc.closePath();
        gc.stroke();

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,event->{
            gc.fillOval(event.getX(), event.getY(), 2 * gc.getLineWidth(), 2 * gc.getLineWidth());
            send("CLICK",event.getX(),event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            send("PRESS",event.getX(),event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
            send("DRAG",event.getX(),event.getY());
        });
    }

    public void resizeCanvas(double width, double height){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.setWidth(width);
        canvas.setHeight(height);
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.lineTo(canvas.getWidth(), 0);
        gc.lineTo(canvas.getWidth(), canvas.getHeight());
        gc.lineTo(0, canvas.getHeight());
        gc.lineTo(0, 0);
        gc.closePath();
        gc.stroke();
    }

    @FXML public void connect(){
        try{
            clientSocket=new Socket("192.168.0.115",5050);
            OutputStream out=clientSocket.getOutputStream();
            os=new DataOutputStream(out);
            os.writeUTF("CONNECT");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML public void host(){
        try{
            ss=new ServerSocket(5050);
            Task task=new Task<Void>(){
                @Override protected Void call(){
                    while(true){
                        try{
                            Socket s=ss.accept();
                            DataInputStream in=new DataInputStream(s.getInputStream());
                            String str=in.readUTF();
                            //System.out.println(in.readUTF());
                            Platform.runLater(()->{receive(str);});
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

        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void send(String status,double x, double y){
        if(clientSocket!=null){
            try {
                clientSocket=new Socket("192.168.0.115",5050);
                OutputStream out=clientSocket.getOutputStream();
                os=new DataOutputStream(out);
                os.writeUTF(status + ";" + x + ";" + y);
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
            case "CLICK":
                gc.fillOval(Double.parseDouble(arr[1]), Double.parseDouble(arr[2]), 2 * gc.getLineWidth(), 2 * gc.getLineWidth());
                break;
            case "PRESS":
                gc.beginPath();
                gc.moveTo(Double.parseDouble(arr[1]), Double.parseDouble(arr[2]));
                break;
            case "DRAG":
                gc.lineTo(Double.parseDouble(arr[1]), Double.parseDouble(arr[2]));
                gc.stroke();
                break;
            default:
                break;
        }
    }
}
