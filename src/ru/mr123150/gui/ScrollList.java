package ru.mr123150.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import ru.mr123150.Controller;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by victorsnesarevsky on 29.06.15.
 */
public class ScrollList extends ScrollPane {

    protected VBox listBox;
    protected Vector<ListNode> list;
    protected Controller parent;

    public ScrollList(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("scroll_list.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        list=new Vector<>();
        listBox=new VBox();
        setContent(listBox);
    }

    public void init(Controller parent){this.parent=parent;}

    public void add(ListNode node){
        list.add(node);
        listBox.getChildren().add(node);
        //refresh();
    }

    public void remove(ListNode node){
        list.remove(node);
        listBox.getChildren().remove(node);
        refresh();
    }

    public void refresh(){
        listBox.getChildren().removeAll();
        listBox.getChildren().addAll(list);
    }

    public int findById(int id){
        int i=0;
        for(ListNode node:list){
            if(node.id()==id) return i;
            ++i;
        }
        return -1;
    }
}
