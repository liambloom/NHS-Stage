package dev.liambloom.nhs.inductionStage.gui;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import dev.liambloom.nhs.inductionStage.gui.DataSelector.SelectionType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class DataEntry extends StageManager.Managed {
    @FXML
    private TableView<CSVRecord> dataTable;

    @FXML
    private Text instructions;

    @FXML
    private IntegerProperty i = new SimpleIntegerProperty(0);

    @FXML
    private ObservableList<DataSelector> selectors = FXCollections.observableArrayList(
            new DataSelector(SelectionType.Rows, true, "member data"),
            new DataSelector(SelectionType.Column, "members' first names"),
            new DataSelector(SelectionType.Column, "members' last names"),
            new DataSelector(SelectionType.Column, "members' year/grade (as a number OR a word)"),
            new DataSelector(SelectionType.Column, "whether a member is returning"),
            new DataSelector(SelectionType.Row, "the incumbent (current) President"),
            new DataSelector(SelectionType.Row, "the incumbent (current) Vice President"),
            new DataSelector(SelectionType.Row, "the incumbent (current) Secretary"),
            new DataSelector(SelectionType.Row, "the incumbent (current) Treasurer"),
            new DataSelector(SelectionType.Row, "the President-elect (next year's President)"),
            new DataSelector(SelectionType.Row, "the Vice President-elect (next year's Vice President)"),
            new DataSelector(SelectionType.Row, "the Secretary-elect (next year's Secretary)"),
            new DataSelector(SelectionType.Row, "the Treasurer-elect (next year's Treasurer)"),
            new DataSelector(SelectionType.Row, "the Leadership award winner"),
            new DataSelector(SelectionType.Row, "the Scholarship award winner"),
            new DataSelector(SelectionType.Row, "the Service award winner"),
            new DataSelector(SelectionType.Row, "the Character award winner")
    );

    public void initialize() {
        instructions.textProperty().bind(new StringBinding() {
            {
                bind(i, selectors);
            }

            @Override
            protected String computeValue() {
                return selectors.get(i.get()).getInstruction();
            }
        });
    }

    protected void initData(List<CSVRecord> records) {
        ObservableList<CSVRecord> observableRecords = FXCollections.observableList(records);
        dataTable.setItems(observableRecords);

        int size = records.get(0).size();
        for (int i = 0; i < size; i++) {
            final int finalI = i;
            TableColumn<CSVRecord, String> column = new TableColumn<>();
            column.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().get(finalI)));
            dataTable.getColumns().add(column);
        }
    }

    @FXML
    private void next(ActionEvent event) {
        if (i.get() >= selectors.size() - 1) {
            new Alert(Alert.AlertType.INFORMATION, "I haven't programmed the next part bit yet!").showAndWait();
        }
        else {
            i.set(i.get() + 1);
        }
    }

    @FXML
    private void prev(ActionEvent event) throws IOException {
        if (i.get() <= 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Going back will return to the start screen. " +
                    "Are you sure you wish to go back?");
            if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
                getStageManager().toStart();
            }
        }
        else {
            i.set(i.get() - 1);
        }
    }
}
