package dev.liambloom.nhs.inductionStage.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.*;
import java.nio.charset.Charset;

public class StartPage extends StageManager.Managed {
    @FXML
    public void openFileChooser(MouseEvent event) throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
        File memberList = fileChooser.showOpenDialog(getStageManager().getStage());
        if (memberList != null) {
            CSVParser csv = CSVParser.parse(memberList, Charset.defaultCharset(), CSVFormat.DEFAULT);

            getStageManager().toDataEntry(csv);
        }
    }
}
