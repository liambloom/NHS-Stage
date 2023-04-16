package dev.liambloom.nhs.inductionStage.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class DataEntry extends BorderPane {
    @FXML
    private TableView<CSVRecord> dataTable;
    private Runnable beforeFirst = null;
    private int i = 0;

    public void initialize() {
        System.out.println("Initialized");
    }

    protected void initData(CSVParser csv) {
        System.out.println("Data!");
    }

    public void setBeforeFirst(Runnable beforeFirst) {
        this.beforeFirst = beforeFirst;
    }

    @FXML
    private void next(ActionEvent event) {
        System.out.println("Next");
        i++;
    }

    @FXML
    private void prev(ActionEvent event) {
        System.out.println("Prev");
        if (i-- <= 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Going back will return to the start screen. Are you sure you wish to go back?");
            alert.showAndWait()
                    .filter(ButtonType.OK::equals)
                    .ifPresent(b -> beforeFirst.run());
        }
    }
}
