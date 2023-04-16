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

public class StartPage {
    @FXML
    public void openFileChooser(MouseEvent event) throws IOException {
        Scene scene = ((Node) event.getSource()).getScene();
        Stage stage = (Stage) scene.getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
        File memberList = fileChooser.showOpenDialog(stage);
        if (memberList != null) {
            CSVParser csv = CSVParser.parse(memberList, Charset.defaultCharset(), CSVFormat.DEFAULT);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DataEntry.fxml"));
            BorderPane dataContent = loader.load();
            DataEntry controller = loader.getController();
            controller.initData(csv);
            controller.setBeforeFirst(() -> stage.setScene(scene));
            Scene dataScene = new Scene(dataContent);
            dataScene.getStylesheets().add(getClass().getResource("/css/DataEntry.css").toExternalForm());

            stage.setScene(dataScene);
        }
    }
}
