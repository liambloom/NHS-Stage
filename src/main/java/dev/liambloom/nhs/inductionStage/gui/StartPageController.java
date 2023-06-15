package dev.liambloom.nhs.inductionStage.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

public class StartPageController extends StageManager.Managed {
    private boolean processFile(File memberList) throws IOException {
        List<CSVRecord> csv = CSVParser.parse(memberList, Charset.defaultCharset(), CSVFormat.DEFAULT).getRecords();

        Iterator<CSVRecord> iter = csv.iterator();

        if (!iter.hasNext()) {
            new Alert(Alert.AlertType.ERROR, "This file contains no data. Please choose a different file.")
                    .showAndWait();
            return false;
        }

        int size = iter.next().size();
        while (iter.hasNext()) {
            if (iter.next().size() != size) {
                new Alert(Alert.AlertType.ERROR, "The rows in this data are not all the same size. Please " +
                        "choose a different file.").showAndWait();
                return false;
            }
        }

        getStageManager().toDataEntry(csv);
        return true;
    }

    @FXML
    public void openFileChooser(MouseEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
        File memberList = fileChooser.showOpenDialog(getStageManager().getStage());
        if (memberList != null) {
            processFile(memberList);
        }
    }

    @FXML
    public void dragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles() && db.getFiles().size() == 1 && db.getFiles().get(0).getName().endsWith(".csv")) {
            event.acceptTransferModes(TransferMode.COPY);
        }
    }

    @FXML
    public void acceptDragFile(DragEvent event) throws IOException {
        event.setDropCompleted(processFile(event.getDragboard().getFiles().get(0)));
    }
}
