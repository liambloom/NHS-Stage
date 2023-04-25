package dev.liambloom.nhs.inductionStage.gui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.When;
import javafx.beans.property.*;
import javafx.beans.property.adapter.JavaBeanStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.apache.commons.csv.CSVRecord;

import dev.liambloom.nhs.inductionStage.gui.DataSelector.SelectionType;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataEntry extends StageManager.Managed {
    @FXML
    private TableView<CSVRecord> dataTable;

    @FXML
    private Text instructions;

    @FXML
    private IntegerProperty i = new SimpleIntegerProperty(0);

    @FXML
    private final ObservableList<DataSelector> selectors = FXCollections.observableArrayList(
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

    private final ObjectBinding<DataSelector> currentSelector = Bindings.valueAt(selectors, i);

    public void initialize() {
        instructions.textProperty().bind(Bindings.selectString(currentSelector, "getInstruction"));

        i.addListener((observable, oldValue, newValue) ->
                setSelectionType(selectors.get(newValue.intValue()).getSelectionType()));

        ObjectBinding<SelectionType> selectionType = Bindings.select(currentSelector, "getSelectionType");

        dataTable.getSelectionModel().selectionModeProperty().bind(
                new When(selectionType.isEqualTo(DataSelector.SelectionType.Row))
                        .then(SelectionMode.SINGLE)
                        .otherwise(SelectionMode.MULTIPLE));

        dataTable.getSelectionModel().cellSelectionEnabledProperty().bind(selectionType.isEqualTo(SelectionType.Column));

//        dataTable.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
//            Node node = event.getPickResult().getIntersectedNode();
//
//            while (node != null /*&& /* && node != dataTable && !(node instanceof TableRow)*/) {
//                System.out.println("node: " + node);
//                node = node.getParent();
//            }
//        });

        dataTable.addEventFilter(MouseEvent.MOUSE_PRESSED, (event) -> {
//            if(currentSelector.get().getSelectionType().equals(SelectionType.Column)
//                    && (event.isShortcutDown() || event.isShiftDown())) {
//                event.consume();
//            }
        });

        setSelectionType(selectors.get(i.get()).getSelectionType());
    }

    private AtomicBoolean columnListenerIsSet = new AtomicBoolean(false);
    private AtomicBoolean rowsHandlerIsSet = new AtomicBoolean(false);

    private void setSelectionType(DataSelector.SelectionType selector) {
        switch (selector) {
            case Row -> {
//                dataTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//                dataTable.getSelectionModel().setCellSelectionEnabled(false);
                dataTable.getFocusModel().focusedCellProperty().removeListener(columnSelector);
                dataTable.removeEventFilter(MouseEvent.MOUSE_PRESSED, rowsSelector);
                columnListenerIsSet.setRelease(false);
                rowsHandlerIsSet.setRelease(false);
            }
            case Rows -> {
//                dataTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//                dataTable.getSelectionModel().setCellSelectionEnabled(false);
                dataTable.getFocusModel().focusedCellProperty().removeListener(columnSelector);
                columnListenerIsSet.setRelease(false);

                if (!rowsHandlerIsSet.getAndSet(true)) {
//                    dataTable.addEventFilter(MouseEvent.MOUSE_PRESSED, rowsSelector);
                }
            }
            case Column -> {
//                dataTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//                dataTable.getSelectionModel().setCellSelectionEnabled(true);
                dataTable.removeEventFilter(MouseEvent.MOUSE_PRESSED, rowsSelector);
                rowsHandlerIsSet.setRelease(false);
                if (!columnListenerIsSet.getAndSet(true)) {
                    dataTable.getFocusModel().focusedCellProperty().addListener(columnSelector);
                }
            }
        }
    }

    private ChangeListener<TablePosition> columnSelector = (obs, oldVal, newVal) -> {
        System.out.println("column " + newVal + " @ " + System.nanoTime());
        if(newVal.getTableColumn() != null){
        Platform.runLater(() -> {
            System.out.println("later, " + newVal);
//        dataTable.getSelectionModel().clearSelection();
            dataTable.getSelectionModel()
                    .selectRange(0, newVal.getTableColumn(), dataTable.getItems().size(), newVal.getTableColumn());});
            System.out.println("Selected TableColumn: "+ newVal.getTableColumn().getText());
//            System.out.println("Selected column index: "+ newVal.getColumn());
        }
    };

    private EventHandler<MouseEvent> rowsSelector = evt -> {
        // https://stackoverflow.com/a/39366485/11326662
        Node node = evt.getPickResult().getIntersectedNode();

        while (node != null && node != dataTable && !(node instanceof TableRow)) {
            node = node.getParent();
        }

        if (node instanceof TableRow) {
            evt.consume();

            TableRow<CSVRecord> row = (TableRow<CSVRecord>) node;
            TableView<CSVRecord> tv = row.getTableView();

            // focus the tableview
            tv.requestFocus();

            if (!row.isEmpty()) {
                // handle selection for non-empty nodes
                int index = row.getIndex();
                if (row.isSelected()) {
                    tv.getSelectionModel().clearSelection(index);
                } else {
                    tv.getSelectionModel().select(index);
                }
            }
        }
    };

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
