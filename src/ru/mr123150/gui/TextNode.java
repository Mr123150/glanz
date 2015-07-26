package ru.mr123150.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

import java.io.IOException;

/**
 * Created by victorsnesarevsky on 26.07.15.
 */
public class TextNode extends ListNode {

    @FXML Label senderLabel;
    @FXML Label textLabel;

    public TextNode(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("text_node.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public TextNode(int id, String sender, String text){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("text_node.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        idLabel.setText(id + "");
        senderLabel.setText(sender);
        textLabel.setText(text);
    }

}
