package ru.mr123150;

import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

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
        });

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
        });

        try{
            ss=new ServerSocket(5050);
            boolean true_true=true;
            Task task=new Task<Void>(){
                @Override protected Void call(){
                    while(true_true){
                        try{
                            Socket s=ss.accept();
                            DataInputStream in=new DataInputStream(s.getInputStream());
                            System.out.println(in.readUTF());
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

    public void resizeCanvas(double width, double height){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.setWidth(width);
        canvas.setHeight(height);
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.lineTo(canvas.getWidth(), 0);
        gc.lineTo(canvas.getWidth(), canvas.getHeight());
        gc.lineTo(0, canvas.getHeight());
        gc.lineTo(0,0);
        gc.closePath();
        gc.stroke();
    }

    @FXML public void connect(){
        try{
            clientSocket=new Socket("127.0.0.1",5050);
            OutputStream out=clientSocket.getOutputStream();
            DataOutputStream os=new DataOutputStream(out);
            os.writeUTF("test");
            clientSocket.close();
        }
        catch (Exception e){

        }
    }
}
