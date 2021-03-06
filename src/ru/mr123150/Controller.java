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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import ru.mr123150.conn.Connection;
import ru.mr123150.conn.User;
import ru.mr123150.gui.ScrollList;
import ru.mr123150.gui.TextNode;
import ru.mr123150.gui.UserNode;
import ru.mr123150.tool.Brush;
import ru.mr123150.tool.Eraser;
import ru.mr123150.tool.Picker;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Vector;

public class Controller implements Initializable{
    @FXML Canvas canvas;
    @FXML Canvas hcolor;
    @FXML Canvas color;
    @FXML Canvas curColor;

    @FXML BorderPane rootPane;
    @FXML VBox topBox;
    @FXML VBox leftBox;
    @FXML VBox rightBox;
    @FXML HBox bottomBox;

    @FXML ScrollPane canvasPane;

    @FXML Button undoBtn;
    @FXML Button redoBtn;
    @FXML MenuBar menuBar;
    @FXML MenuItem UndoMI;
    @FXML MenuItem RedoMI;

    FileChooser fileChooser;

    @FXML TextField brushSizeText;

    @FXML TextField hColorText;
    @FXML TextField sColorText;
    @FXML TextField bColorText;

    @FXML ScrollList userScroll;

    @FXML Label statusLabel;

    @FXML ScrollList chatScroll;
    @FXML TextField chatText;
    @FXML Button chatSend;

    GraphicsContext gc;
    GraphicsContext hc;
    GraphicsContext cc;
    GraphicsContext curC;

    Connection conn=null;
    Connection hconn=null;

    Vector<WritableImage> undo=new Vector<WritableImage>();
    Vector<WritableImage> redo=new Vector<WritableImage>();

    double h,s,b;

    User user;

    Dialog<ButtonType> spinner = new Dialog<>();

    @Override
    public void initialize(URL url, ResourceBundle rb){

        hc=hcolor.getGraphicsContext2D();
        cc=color.getGraphicsContext2D();

        curC=curColor.getGraphicsContext2D();
        curC.fillRect(0,0,curColor.getWidth(),curColor.getHeight());

        gc=canvas.getGraphicsContext2D();
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.lineTo(canvas.getWidth(),0);
        gc.lineTo(canvas.getWidth(), canvas.getHeight());
        gc.lineTo(0, canvas.getHeight());
        gc.lineTo(0, 0);
        gc.stroke();

        try {
            user = new User(gc);
            user.setTool(new Brush());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        setColor(0,0,0);
        brushSizeText.setText(me().size()+"");

        undo.add(canvas.snapshot(null,null));
        if(undo.size()<=1){
            undoBtn.setDisable(true);
            UndoMI.setDisable(true);
        }
        if(redo.isEmpty()){
            redoBtn.setDisable(true);
            RedoMI.setDisable(true);
        }
        userScroll.init(this);
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            try{
                if(me().tool().returnable()) setColor(me().dot(event.getX(),event.getY()));
                else me().dot(event.getX(),event.getY());
            }
            catch(NullPointerException e){}
            send("DRAW;CLICK;" + event.getX() + ";" + event.getY());
            if(me().tool().action()) {
                undo.add(canvas.snapshot(null, null));
                if (undo.size() > 1) {
                    undoBtn.setDisable(false);
                    UndoMI.setDisable(false);
                }
            }
        });

        statusLabel.setText("");

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            me().setCoord(event.getX(), event.getY());
            send("DRAW;PRESS;"+event.getX()+";"+event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            try {
                if (me().tool().returnable()) setColor(me().lineTo(event.getX(), event.getY()));
                else me().lineTo(event.getX(), event.getY());
            }
            catch(NullPointerException e){}
            send("DRAW;DRAG;"+event.getX()+";"+event.getY());
        });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            send("DRAW;RELEASE");
        });

        hcolor.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->{
            int h=(int)(event.getX() / hcolor.getWidth() * 360);
            hColorText.setText(h+"");
            setColor(h,s,b);
        });

        hcolor.addEventHandler(MouseEvent.MOUSE_DRAGGED, event ->{
            int h=(int)(event.getX() / hcolor.getWidth() * 360);
            hColorText.setText(h+"");
            setColor(h,s,b);
        });

        color.addEventHandler(MouseEvent.MOUSE_PRESSED, event ->{
            int s=(int)(100*event.getX() / color.getWidth());
            int b=(int)(100*(1 - event.getY() / color.getHeight()));
            sColorText.setText(s+"");
            bColorText.setText(b+"");
            setColor(h,(double)s/100,(double)b/100);
        });

        color.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            int s=(int)(100*event.getX() / color.getWidth());
            int b=(int)(100*(1 - event.getY() / color.getHeight()));
            sColorText.setText(s+"");
            bColorText.setText(b+"");
            setColor(h,(double)s/100,(double)b/100);
        });

        hColorText.textProperty().addListener((observable,oldValue,newValue)->{
            parseColor();
        });

        sColorText.textProperty().addListener((observable,oldValue,newValue)->{
            parseColor();
        });

        bColorText.textProperty().addListener((observable,oldValue,newValue)->{
            parseColor();
        });

        brushSizeText.textProperty().addListener((observable,oldValue,newValue)->{
            double size=1;
            try{
                size=Double.parseDouble(newValue);
            }
            catch (NumberFormatException e){

            }
            me().setSize(size);
        });

        spinner.setTitle("Please wait");
        spinner.setContentText("Waiting for host response");
        spinner.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
    }

    public User me(){
        return conn==null?user:conn.users.get(0);
    }

    public void fitPane(){
        canvasPane.setPrefWidth(rootPane.getWidth()-leftBox.getWidth()-rightBox.getWidth());
        canvasPane.setPrefHeight(rootPane.getHeight() - topBox.getHeight() - bottomBox.getHeight());
    }

    public void fitCanvas(){
        resizeCanvas(canvasPane.getWidth()-2,canvasPane.getHeight()-2);
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
        gc.stroke();

        undo.clear();
        undo.add(canvas.snapshot(null,null));
        gc.drawImage(undo.get(0),0,0);
    }
    
    public void setColor(double h, double s, double b){
        if(h<0)h=0;
        if(h>360)h=360;
        if(s<0)s=0;
        if(s>1.0)s=1;
        if(b<0)b=0;
        if(b>1.0)b=1;
        this.h=h;
        this.s=s;
        this.b=b;

        me().setColor(h, s, b);
        send("CHANGE;COLOR;"+h+";"+s+";"+b);
        redrawColor();
    }

    public void setColor(Color color){
        setColor(color.getHue(), color.getSaturation(),color.getBrightness());
        hColorText.setText((int)color.getHue()+"");
        sColorText.setText((int)(100*color.getSaturation())+"");
        bColorText.setText((int)(100*color.getBrightness())+"");
    }

    public void redrawColor(){
        hc.clearRect(0,0,hcolor.getWidth(),hcolor.getHeight());
        for(int i=0;i<hcolor.getWidth();++i){
            hc.setStroke(Color.hsb((double)i/hcolor.getWidth()*360, 1.0, 1.0, 1.0));
            hc.strokeLine(i, 0, i, hcolor.getHeight());
        }
        hc.setStroke(Color.BLACK);
        hc.strokeOval(h / 360 * hcolor.getWidth() - hcolor.getHeight() / 2, 0, hcolor.getHeight(), hcolor.getHeight());
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

    public void parseColor(){
        int h,s,b;
        try{h=Integer.parseInt(hColorText.getText());}
        catch (Exception e){h=0;}
        try{s=Integer.parseInt(sColorText.getText());}
        catch (Exception e){s=0;}
        try{b=Integer.parseInt(bColorText.getText());}
        catch (Exception e){b=0;}
        if(h>360)h=360;
        if(h<0)h=0;
        if(s>100)s=100;
        if(s<0)s=0;
        if(b>100)b=100;
        if(b<0)b=0;
        setColor(h,(double)s/100,(double)b/100);
        curC.setFill(Color.hsb(h,(double)s/100,(double)b/100));
        curC.fillRect(0,0,curColor.getWidth(),curColor.getHeight());

    }

    @FXML public void undo(){
        undo(true);
    }

    public void undo(boolean send){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        redo.add(undo.lastElement());
        undo.remove(undo.size()-1);
        gc.drawImage(undo.lastElement(),0,0);
        if (undo.size() <= 1) {
            undoBtn.setDisable(true);
            UndoMI.setDisable(true);
        }
        if (!redo.isEmpty()) {
            redoBtn.setDisable(false);
            RedoMI.setDisable(false);
        }
        if(send)send("CHANGE;UNDO");
    }

    @FXML public void redo(){
        redo(true);
    }

    public void redo(boolean send){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        undo.add(redo.lastElement());
        gc.drawImage(redo.lastElement(),0,0);
        redo.remove(redo.size()-1);
        if (undo.size() > 1) {
            undoBtn.setDisable(false);
            UndoMI.setDisable(false);
        }
        if (redo.isEmpty()) {
            redoBtn.setDisable(true);
            RedoMI.setDisable(true);
        }
        if(send)send("CHANGE;REDO");
    }

    @FXML public void toolBrush(){
        me().setTool(new Brush());
        send("CHANGE;TOOL;BRUSH");
    }

    @FXML public void toolEraser(){
        me().setTool(new Eraser());
        send("CHANGE;TOOL;ERASER");
    }

    @FXML public void toolPicker(){
        me().setTool(new Picker());
        send("CHANGE;TOOL;PICKER");
    }

    @FXML public void chat(){
        String msg=chatText.getText();
        msg=msg.replace(";",":");
        TextNode node=new TextNode(0,me().id()+"",msg);
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
                User tmp_user=me();
                conn=new Connection(data.get(0),5050);
                hconn=new Connection(5051,false);
                listen();
                send("CONNECT;REQUEST;" + conn.getAddress() + ";" + tmp_user.toolText() + ";" + tmp_user.x() + ";" + tmp_user.y() + ";" + tmp_user.colorText());
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
            statusLabel.setText("Server started at "+conn.getAddress());
            listen();
            conn.users.insertElementAt(new User(gc), 0);
            me().setColor(h, s, b);
            me().setTool(new Brush());
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
                                User new_user=new User(new_id,arr[2],gc);
                                conn.users.add(new_user);
                                Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Connection request");
                                alert.setContentText("Connection request from IP " + arr[2] + ". Allow?");
                                send("CONNECT;TEST");
                                Optional<ButtonType> result=alert.showAndWait();
                                if (result.get() == ButtonType.OK){
                                    switch (arr[3]){
                                        case "BRUSH":
                                            new_user.setTool(new Brush());
                                            break;
                                        case "ERASER":

                                            break;
                                        default:
                                            break;
                                    }
                                    new_user.setCoord(Double.parseDouble(arr[4]),Double.parseDouble(arr[5]));
                                    new_user.setColor(Double.parseDouble(arr[6]),Double.parseDouble(arr[7]),Double.parseDouble(arr[8]));
                                    userScroll.add(new UserNode(new_id,arr[2],true));
                                    send("CONNECT;ACCEPT;" + new_id + ";" + arr[2]);
                                    send("SYNC;"+ new_id +";SIZE;" + canvas.getWidth() + ";" + canvas.getHeight() + ";" + arr[2]);
                                    send("SYNC;"+ new_id +";DATA;0;data"); //Stub for data sync
                                    for(User user:conn.users){
                                        send("SYNC;"+ new_id +";USER;"+user.id()+";"+user.addressText()+";"+user.toolText()+";"+user.colorText()+";"+user.x()+";"+user.y());
                                    }
                                    send("CHANGE;USER;ADD;"+new_id+";"+arr[2]+";"+arr[3]+";"+arr[4]+";"+arr[5]+";"+arr[6]+";"+arr[7]+";"+arr[8]);
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
                                conn.users.insertElementAt(new User(Integer.parseInt(arr[2]),gc), 0);
                                conn.users.insertElementAt(new User(gc), 1);
                                userScroll.add(new UserNode(0, conn.getAddress(), false));
                                userScroll.add(new UserNode(Integer.parseInt(arr[2]), arr[3], false));
                                me().setColor(h, s, b);
                                me().setTool(new Brush());
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
                        send("CHANGE;USER;REMOVE;" + user.id());
                    }
                    else {
                        conn=null;
                        hconn=null;
                    }
                    break;
                }
            case "DRAW":
                if(conn.isHost()||id!=me().id()) {
                    int user_id=conn.getUserById(id);

                    switch (arr[1]) {
                        case "CLICK":
                            if(conn.users.get(user_id).tool().action())conn.users.get(user_id).dot(Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
                            undo.add(canvas.snapshot(null,null));
                            if (undo.size() > 1) {
                                undoBtn.setDisable(false);
                                UndoMI.setDisable(false);
                            }
                            if (conn.isHost()) send(str,false);
                            break;
                        case "PRESS":
                            conn.users.get(user_id).setCoord(Double.parseDouble(arr[2]),Double.parseDouble(arr[3]));
                            if (conn.isHost()) send(str,false);
                            break;
                        case "DRAG":
                            conn.users.get(user_id).lineTo(Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
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
                if(Integer.parseInt(arr[1])==me().id()){
                    switch(arr[2]){
                        case "SIZE":
                            resizeCanvas(Double.parseDouble(arr[3]),Double.parseDouble(arr[4]));
                            break;
                        case "USER":
                            try {
                                int sync_id=Integer.parseInt(arr[3]);
                                if(sync_id!=me().id()&&sync_id!=0) {
                                    User new_user = new User(sync_id, arr[4]);
                                    new_user.setContext(gc);
                                    new_user.setColor(Double.parseDouble(arr[6]), Double.parseDouble(arr[7]), Double.parseDouble(arr[8]));
                                    new_user.setCoord(Double.parseDouble(arr[9]),Double.parseDouble(arr[10]));
                                    switch (arr[5]){
                                        case "BRUSH":
                                            new_user.setTool(new Brush());
                                            break;
                                        case "ERASER":
                                            new_user.setTool(new Eraser());
                                            break;
                                        case "PICKER":
                                            new_user.setTool(new Picker());
                                            break;
                                        default:
                                            break;
                                    }
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
                if(conn.isHost()||id!=me().id()) {
                    int user_id=conn.getUserById(id);
                    if(user_id!=-1) {
                        switch (arr[1]) {
                            case "USER":
                                switch(arr[2]){
                                    case "ADD":
                                        int new_id=Integer.parseInt(arr[3]);
                                        if(conn.getUserById(new_id)==-1){
                                            int sync_id=Integer.parseInt(arr[3]);
                                            if(sync_id!=me().id()&&sync_id!=0) {
                                                try {
                                                    User new_user = new User(sync_id, arr[4]);
                                                    new_user.setContext(gc);
                                                    new_user.setColor(Double.parseDouble(arr[6]), Double.parseDouble(arr[7]), Double.parseDouble(arr[8]));
                                                    new_user.setCoord(Double.parseDouble(arr[9]), Double.parseDouble(arr[10]));
                                                    switch (arr[5]) {
                                                        case "BRUSH":
                                                            new_user.setTool(new Brush());
                                                            break;
                                                        case "ERASER":
                                                            new_user.setTool(new Eraser());
                                                            break;
                                                        case "PICKER":
                                                            new_user.setTool(new Picker());
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                    conn.users.add(new_user);
                                                    userScroll.add(new UserNode(new_id, arr[4], false));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
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
                            case "TOOL":
                                switch (arr[2]){
                                    case "BRUSH":
                                        conn.users.get(user_id).setTool(new Brush());
                                        break;
                                    case "ERASER":
                                        conn.users.get(user_id).setTool(new Eraser());
                                        break;
                                    case "PICKER":
                                        conn.users.get(user_id).setTool(new Picker());
                                        break;
                                    default:
                                        break;
                                }
                                if(conn.isHost())send(str,false);
                            default:
                                break;
                        }
                    }
                }
                break;
            case "CHAT":
                if(conn.isHost()||id!=me().id()) {
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
        if(conn!=null&&me().id()!=-1)send("DISCONNECT");
    }
}
