package ru.mr123150.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class ListNode extends HBox{

    @FXML Label idLabel;

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

    public int id(){
        return Integer.parseInt(idLabel.getText());
    }
}
