package ru.mr123150.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import ru.mr123150.Controller;

import java.io.IOException;

public class ListNode extends Pane {

    @FXML Label idLabel;
    ScrollList parent=null;
    Controller root=null;

    public ListNode(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("list_node.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public ListNode(ScrollList parent){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("list_node.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            this.parent=parent;
            this.root=parent.root();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public int id(){
        return Integer.parseInt(idLabel.getText());
    }

    public void showId(boolean show){
        idLabel.setVisible(show);
    }

    public void setParent(ScrollList parent){
        this.parent=parent;
        this.root=parent.root();
        setWidth(parent.getWidth());
    }
}
