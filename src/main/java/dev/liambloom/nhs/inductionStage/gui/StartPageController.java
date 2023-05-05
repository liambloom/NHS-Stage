package dev.liambloom.nhs.inductionStage.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

public class StartPageController extends StageManager.Managed {
    @FXML
    public void openFileChooser(MouseEvent event) throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
        File memberList = fileChooser.showOpenDialog(getStageManager().getStage());
        if (memberList != null) {
            List<CSVRecord> csv = CSVParser.parse(memberList, Charset.defaultCharset(), CSVFormat.DEFAULT).getRecords();

            Iterator<CSVRecord> iter = csv.iterator();

            if (!iter.hasNext()) {
                new Alert(Alert.AlertType.ERROR, "This file contains no data. Please choose a different file.")
                        .showAndWait();
                return;
            }

            int size = iter.next().size();
            while (iter.hasNext()) {
                if (iter.next().size() != size) {
                    new Alert(Alert.AlertType.ERROR, "The rows in this data are not all the same size. Please " +
                            "choose a different file.").showAndWait();
                    return;
                }
            }

            getStageManager().toDataEntry(csv);
        }
    }
}
