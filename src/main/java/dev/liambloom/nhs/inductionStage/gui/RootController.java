package dev.liambloom.nhs.inductionStage.gui;

import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

public class RootController {
    public BorderPane rootPane;
    public Text instructions;
    public Button next;

    public void setPage(Parent parent) {
        rootPane.setCenter(parent);
    }

    public void next(ActionEvent event) {

    }

    public void prev(ActionEvent event) {

    }
}
