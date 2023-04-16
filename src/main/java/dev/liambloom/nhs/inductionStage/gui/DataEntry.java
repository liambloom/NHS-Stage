package dev.liambloom.nhs.inductionStage.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.UncheckedIOException;

public class DataEntry extends StageManager.Managed {
    @FXML
    private TableView<CSVRecord> dataTable;
    private int i = 0;

    public void initialize() {
        System.out.println("Initialized");
    }

    protected void initData(CSVParser csv) {
        System.out.println("Data!");
    }

    @FXML
    private void next(ActionEvent event) {
        System.out.println("Next");
        i++;
    }

    @FXML
    private void prev(ActionEvent event) throws IOException {
        System.out.println("Prev");
        if (i-- <= 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Going back will return to the start screen. Are you sure you wish to go back?");
            if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
                getStageManager().toStart();
            }
        }
    }
}
