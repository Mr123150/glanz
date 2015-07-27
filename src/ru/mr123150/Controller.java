package ru.mr123150;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import ru.mr123150.conn.Connection;
import ru.mr123150.conn.User;
import ru.mr123150.gui.ScrollList;
import ru.mr123150.gui.TextNode;
import ru.mr123150.gui.UserNode;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Vector;

public class Controller implements Initializable{
    @FXML Canvas canvas;
    @FXML Canvas hcolor;
    @FXML Canvas color;
    @FXML BorderPane rootPane;
    @FXML HBox topBox;
    @FXML VBox leftBox;
    @FXML VBox rightBox;
    @FXML HBox bottomBox;

    @FXML Button undoBtn;
    @FXML Button redoBtn;

    @FXML ScrollList userScroll;

    @FXML Label statusLabel;

    @FXML ScrollList chatScroll;
    @FXML TextField chatText;
    @FXML Button chatSend;

    GraphicsContext gc;
    GraphicsContext hc;
    GraphicsContext cc;

    Connection conn=null;
    Connection hconn=null;

    Vector<WritableImage> undo=new Vector<WritableImage>();
    Vector<WritableImage> redo=new Vector<WritableImage>();

    double h,s,b;

    Dialog<ButtonType> spinner = new Dialog<>();

    @Override
    public void initialize(URL url, ResourceBundle rb){

        hc=hcolor.getGraphicsContext2D();
        cc=color.getGraphicsContext2D();

        setHue(0);
        setColor(0, 0);

        gc=canvas.getGraphicsContext2D();
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.lineTo(canvas.getWidth(),0);
        gc.lineTo(canvas.getWidth(), canvas.getHeight());
        gc.lineTo(0, canvas.getHeight());
        gc.lineTo(0, 0);
        //gc.closePath();
        gc.stroke();

        undo.add(canvas.snapshot(null,null));
        if(undo.size()<=1)undoBtn.setDisable(true);
        if(redo.isEmpty())redoBtn.setDisable(true);
        userScroll.init(this);
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (conn != null && !conn.users.isEmpty()) gc.setFill(conn.users.get(0).color());
            else gc.setFill(Color.hsb(h, s, b));
            gc.fillOval(event.getX(), event.getY(), gc.getLineWidth(), gc.getLineWidth());
            send("DRAW;CLICK;" + event.getX() + ";" + event.getY());

            undo.add(canvas.snapshot(null, null));
            if (undo.size() > 1) undoBtn.setDisable(false);
        });

        statusLabel.setText("");

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if(conn!=null&&!conn.users.isEmpty()) {
                conn.users.get(0).setCoord(event.getX(),event.getY());
            }
            else {
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY());
            }
            send("DRAW;PRESS;"+event.getX()+";"+event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if(conn!=null&&!conn.users.isEmpty()) {
                gc.beginPath();
                gc.setStroke(conn.users.get(0).color());
                gc.moveTo(conn.users.get(0).x(),conn.users.get(0).y());
                conn.users.get(0).setCoord(event.getX(),event.getY());
            }
            else gc.setStroke(Color.hsb(h,s,b));
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
            if(conn!=null&&!conn.users.isEmpty()) gc.closePath();
            send("DRAW;DRAG;"+event.getX()+";"+event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            //gc.closePath();
            send("DRAW;RELEASE");
        });

        hcolor.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->{
            setHue(event.getX() / hcolor.getWidth() * 360);
        });

        hcolor.addEventHandler(MouseEvent.MOUSE_DRAGGED, event ->{
            setHue(event.getX()/hcolor.getWidth()*360);
        });

        color.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->{
            setColor(event.getX() / color.getWidth(), 1 - event.getY() / color.getHeight());
        });

        color.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            setColor(event.getX() / color.getWidth(), 1 - event.getY() / color.getHeight());
        });

        spinner.setTitle("Please wait");
        spinner.setContentText("Waiting for host response");
        spinner.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
    }

    public void resizeCanvas(){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.setWidth(rootPane.getWidth()-leftBox.getWidth()-rightBox.getWidth());
        canvas.setHeight(rootPane.getHeight() - topBox.getHeight() - bottomBox.getHeight());
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.lineTo(canvas.getWidth(), 0);
        gc.lineTo(canvas.getWidth(), canvas.getHeight());
        gc.lineTo(0, canvas.getHeight());
        gc.lineTo(0, 0);
        gc.stroke();

        undo.clear();
        undo.add(canvas.snapshot(null,null));
        gc.drawImage(undo.get(0),0,0);
    }

    public void setHue(double h){
        hc.clearRect(0,0,hcolor.getWidth(),hcolor.getHeight());
        this.h=h;
        for(int i=0;i<hcolor.getWidth();++i){
            hc.setStroke(Color.hsb((double)i/hcolor.getWidth()*360, 1.0, 1.0, 1.0));
            hc.strokeLine(i, 0, i, hcolor.getHeight());
        }
        hc.setStroke(Color.BLACK);
        hc.strokeOval(h / 360 * hcolor.getWidth() - hcolor.getHeight() / 2, 0, hcolor.getHeight(), hcolor.getHeight());
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
        cc.strokeOval(s * color.getWidth() - hcolor.getHeight() / 2, (1 - b) * color.getHeight() - hcolor.getHeight() / 2, hcolor.getHeight(), hcolor.getHeight());
    }

    @FXML public void undo(){
        undo(true);
    }

    public void undo(boolean send){
        gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        redo.add(undo.lastElement());
        undo.remove(undo.size()-1);
        gc.drawImage(undo.lastElement(),0,0);
        if(undo.size()<=1)undoBtn.setDisable(true);
        if(!redo.isEmpty())redoBtn.setDisable(false);
        if(send)send("CHANGE;UNDO");
    }

    @FXML public void redo(){
        redo(true);
    }

    public void redo(boolean send){
        gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        undo.add(redo.lastElement());
        gc.drawImage(redo.lastElement(),0,0);
        redo.remove(redo.size()-1);
        if(redo.isEmpty())redoBtn.setDisable(true);
        if(undo.size()>1)undoBtn.setDisable(false);
        if(send)send("CHANGE;REDO");
    }

    @FXML public void chat(){
        String msg=chatText.getText();
        msg=msg.replace(";",":");
        TextNode node=new TextNode(0,conn.users.get(0).id()+"",msg);
        node.showId(false);
        chatScroll.add(node);
        send("CHAT;" + msg);
    }

    @FXML public void connect(){
        Dialog<Vector<String>> connectDialog = new Dialog<>();
        connectDialog.setTitle("Connect to remote host");
        ButtonType connectBtnType=new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        connectDialog.getDialogPane().getButtonTypes().addAll(connectBtnType, ButtonType.CANCEL);
        GridPane grid=new GridPane();
        grid.add(new Label("Host address"),0,0);
        TextField connAddress = new TextField();
        grid.add(connAddress,1,0);
        connectDialog.getDialogPane().setContent(grid);
        connectDialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectBtnType) {
                Vector<String> res=new Vector<String>();
                res.add(connAddress.getText());
                return res;
            }
            return null;
        });
        Optional<Vector<String>> result = connectDialog.showAndWait();
        result.ifPresent(data->{
            try{
                conn=new Connection(data.get(0),5050);
                hconn=new Connection(5051,false);
                listen();
                send("CONNECT;REQUEST;" + conn.getAddress());
                spinner.show();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        });

    }

    @FXML public void host(){
        try{
            hconn=new Connection(5050);
            conn=new Connection(5051,true,true);
            System.out.println("//SERVER STARTED");
            statusLabel.setText("Server started");
            listen();
            conn.users.insertElementAt(new User(), 0);
            conn.users.get(0).setColor(h, s, b);
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
        if(conn!=null&&(!conn.isHost()||conn.users.size()>0)) {
            try {
                conn.send(str,true);
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    public void send(String str, boolean signature){
        if(conn!=null&&(!conn.isHost()||conn.users.size()>0)) {
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
        int id=Integer.parseInt(arr[arr.length-1]);
        switch(arr[0]){
            case "CONNECT":
                if(conn.isHost()||arr[arr.length-2].equals(conn.getAddress())){
                    switch(arr[1]) {
                        case "REQUEST":
                            try {
                                int new_id=conn.users.lastElement().id()+1;
                                conn.users.add(new User(new_id,arr[2]));
                                Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Connection request");
                                alert.setContentText("Connection request from IP " + arr[2] + ". Allow?");
                                send("CONNECT;TEST");
                                Optional<ButtonType> result=alert.showAndWait();
                                if (result.get() == ButtonType.OK){
                                    userScroll.add(new UserNode(new_id,arr[2],true));
                                    send("CONNECT;ACCEPT;" + new_id + ";" + arr[2]);
                                    send("SYNC;"+ new_id +";SIZE;" + canvas.getWidth() + ";" + canvas.getHeight() + ";" + arr[2]);
                                    send("SYNC;"+ new_id +";LAYERS;1"); //Stub for multi-layers
                                    send("SYNC;"+ new_id +";DATA;0;data"); //Stub for data sync
                                    for(User user:conn.users){
                                        send("SYNC;"+ new_id +";USER;"+user.id()+";"+user.addressText()+";"+user.colorText());
                                    }
                                    send("CHANGE;USER;ADD;"+new_id+";"+arr[2]);
                                } else {
                                    send("CONNECT;REJECT;" + arr[2]);
                                    conn.users.remove(conn.users.size()-1);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "ACCEPT":
                            try {
                                conn.users.insertElementAt(new User(Integer.parseInt(arr[2])),0);
                                conn.users.insertElementAt(new User(), 1);
                                userScroll.add(new UserNode(0,conn.getAddress(),false));
                                userScroll.add(new UserNode(Integer.parseInt(arr[2]),arr[3],false));
                                conn.users.get(0).setColor(h, s, b);
                                if(spinner.isShowing())spinner.hide();
                                statusLabel.setText("Successfully connected");
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                            break;
                        case "REJECT":
                            if(spinner.isShowing())spinner.hide();
                            conn=null;
                            hconn=null;
                            Alert alert=new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Connection rejected");
                            alert.setContentText("Connection request was rejected by host");
                            alert.show();
                            statusLabel.setText("Connection failed");
                            break;
                        default:
                            break;
                    }
                }
                break;
            case "DISCONNECT":
                if(conn.isHost()||Integer.parseInt(arr[1])==id||Integer.parseInt(arr[1])==0) {
                    if (conn.isHost()) {
                        User user=conn.users.get(conn.getUserById(id));
                        conn.users.remove(conn.getUserById(id));
                        userScroll.remove(new UserNode(user.id(),user.addressText(),true));
                        send("CHANGE;USER;REMOVE;"+user.id());
                    }
                    else {
                        conn=null;
                        hconn=null;
                    }
                    break;
                }
            case "DRAW":
                if(conn.isHost()||id!=conn.users.get(0).id()) {
                    int user_id=conn.getUserById(id);

                    switch (arr[1]) {
                        case "CLICK":
                            if(conn.users.get(user_id)!=null) gc.setFill(conn.users.get(user_id).color());
                            else gc.setFill(Color.BLACK);
                            gc.fillOval(Double.parseDouble(arr[2]), Double.parseDouble(arr[3]), 2 * gc.getLineWidth(), 2 * gc.getLineWidth());
                            undo.add(canvas.snapshot(null,null));
                            if(undo.size()>1)undoBtn.setDisable(false);
                            if (conn.isHost()) send(str,false);
                            break;
                        case "PRESS":
                            conn.users.get(user_id).setCoord(Double.parseDouble(arr[2]),Double.parseDouble(arr[3]));
                            if (conn.isHost()) send(str,false);
                            break;
                        case "DRAG":
                            if(user_id!=-1) gc.setStroke(conn.users.get(user_id).color());
                            else gc.setStroke(Color.BLACK);
                            gc.beginPath();
                            gc.moveTo(conn.users.get(user_id).x(), conn.users.get(user_id).y());
                            gc.lineTo(Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
                            conn.users.get(user_id).setCoord(Double.parseDouble(arr[2]),Double.parseDouble(arr[3]));
                            gc.stroke();
                            gc.closePath();
                            if (conn.isHost()) send(str,false);
                            break;
                        case "RELEASE":
                            if (conn.isHost()) send(str,false);
                            break;
                        default:
                            break;
                    }
                }
                break;
            case "SYNC":
                if(Integer.parseInt(arr[1])==conn.users.get(0).id()){
                    switch(arr[2]){
                        case "USER":
                            try {
                                int sync_id=Integer.parseInt(arr[3]);
                                if(sync_id!=conn.users.get(0).id()&&sync_id!=0) {
                                    User new_user = new User(sync_id, arr[4]);
                                    new_user.setColor(Double.parseDouble(arr[5]), Double.parseDouble(arr[6]), Double.parseDouble(arr[7]));
                                    conn.users.add(new_user);
                                    userScroll.add(new UserNode(Integer.parseInt(arr[3]), arr[4], false));
                                }
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                }
                break;
            case "CHANGE":
                if(conn.isHost()||id!=conn.users.get(0).id()) {
                    int user_id=conn.getUserById(id);
                    if(user_id!=-1) {
                        switch (arr[1]) {
                            case "USER":
                                switch(arr[2]){
                                    case "ADD":
                                        int new_id=Integer.parseInt(arr[3]);
                                        if(conn.getUserById(new_id)==-1){
                                            try {
                                                conn.users.add(new User(new_id, arr[4]));
                                                userScroll.add(new UserNode(new_id,arr[4],false));
                                            }
                                            catch(Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                        break;
                                    case "REMOVE":

                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case "COLOR":
                                conn.users.get(user_id).setColor(Double.parseDouble(arr[2]),Double.parseDouble(arr[3]),Double.parseDouble(arr[4]));
                                if(conn.isHost())send(str,false);
                                break;
                            case "UNDO":
                                undo(false);
                                if(conn.isHost())send(str,false);
                                break;
                            case "REDO":
                                redo(false);
                                if(conn.isHost())send(str,false);
                                break;
                            default:
                                break;
                        }
                    }
                }
                break;
            case "CHAT":
                if(conn.isHost()||id!=conn.users.get(0).id()) {
                    TextNode node = new TextNode(0, arr[2], arr[1]);
                    node.showId(false);
                    chatScroll.add(node);
                    if (conn.isHost()) send(str, false);
                }
                break;
            default:
                break;
        }
    }

    public void disconnect(){
        if(conn!=null&&conn.users.get(0).id()!=-1)send("DISCONNECT");
    }
}
