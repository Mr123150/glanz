package ru.mr123150.gui;

/**
 * Created by victorsnesarevsky on 29.06.15.
 */

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import ru.mr123150.Controller;

import java.io.IOException;

/**
 * Created by victorsnesarevsky on 29.06.15.
 */
public class UserNode extends ListNode{

    @FXML Label addressLabel;
    @FXML Button disconnectBtn;

    public UserNode(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("user_node.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public UserNode(int id, String address, boolean isHost){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("user_node.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        idLabel.setText(id+"");
        addressLabel.setText(address);

        disconnectBtn.setVisible(isHost);
    }

    @FXML public void disconnect(){
        if(parent!=null) {
            root.send("DISCONNECT;" + idLabel.getText(), false);
            parent.remove(this);
        }
    }
}

