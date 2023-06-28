package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.apache.commons.csv.CSVRecord;

import dev.liambloom.nhs.inductionStage.gui.DataSelector.SelectionType;

import java.io.IOException;
import java.util.*;

public class DataEntryController extends StageManager.Managed {
    @FXML
    private TableView<CSVRecord> dataTable;

    private List<CSVRecord> records;

//    @FXML
//    private Button next;

//    @FXML
//    private Text instructions;
//
    @FXML
    private IntegerProperty i = new SimpleIntegerProperty(0);

    @FXML
    private final ObservableList<DataSelector> selectors = FXCollections.observableArrayList(
            new DataSelector(SelectionType.TopRows, "headers"),
            new DataSelector(SelectionType.Column, "members' first names"),
            new DataSelector(SelectionType.Column, "members' last names"),
            new DataSelector(SelectionType.Column, "members' year/grade (as a number OR a word)"),
            new DataSelector(SelectionType.Column, "whether a member is returning (were they a member last year?)"),
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

    private boolean selectionUpdateFreeze = false;

    private boolean columnFocusFreezer = false;

    public void initialize() {
        ObjectBinding<SelectionType> selectionType = Bindings.select(currentSelector, "getSelectionType");

        dataTable.getSelectionModel().selectionModeProperty().bind(
                new When(selectionType.isEqualTo(DataSelector.SelectionType.Row))
                        .then(SelectionMode.SINGLE)
                        .otherwise(SelectionMode.MULTIPLE));

        dataTable.getSelectionModel().cellSelectionEnabledProperty().bind(selectionType.isEqualTo(SelectionType.Column));

        dataTable.addEventFilter(MouseEvent.MOUSE_PRESSED, (event) -> {
            if(currentSelector.get().getSelectionType().equals(SelectionType.Column)
                    && (event.isShortcutDown() || event.isShiftDown())) {
                event.consume();
            }
        });

        // Column focus
        dataTable.getFocusModel().focusedCellProperty().addListener((obs, oldVal, newVal) -> {
            if (!currentSelector.get().getSelectionType().equals(SelectionType.Column) || columnFocusFreezer) {
                return;
            }

            if (newVal.getTableColumn() != null){
                columnFocusFreezer = true;
                selectCol(newVal.getColumn());
                columnFocusFreezer = false;
            }
        });

        // TopRows focus
        dataTable.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!currentSelector.get().getSelectionType().equals(SelectionType.TopRows)) {
                return;
            }

            // https://stackoverflow.com/a/39366485/11326662
            Node node = event.getPickResult().getIntersectedNode();

            while (node != null && node != dataTable && !(node instanceof TableRow)) {
                node = node.getParent();
            }

            if (node instanceof TableRow) {
                event.consume();

                TableRow<CSVRecord> row = (TableRow<CSVRecord>) node;
                TableView<CSVRecord> table = row.getTableView();

                // focus the tableview
                table.requestFocus();

                table.getSelectionModel().clearSelection();
                table.getSelectionModel().selectRange(0, row.getIndex() + 1);
            }
        });

        dataTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (selectionUpdateFreeze) {
                return;
            }

            ObservableList<TablePosition> selectedCells = dataTable.getSelectionModel().getSelectedCells();

            if (selectedCells.isEmpty()) {
                return;
            }

            int selection = switch (currentSelector.get().getSelectionType()) {
                case TopRows -> selectedCells.stream().map(TablePosition::getRow).sorted().skip(selectedCells.size() - 1).findFirst().orElseThrow();
                case Row -> selectedCells.get(0).getRow();
                case Column -> selectedCells.get(0).getColumn();
            };

            currentSelector.get().setSelection(selection);
        });
    }

    protected void initData(List<CSVRecord> records) {
        this.records = records;
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


    private void selectCol(int i) {
        TableColumn<CSVRecord, ?> col = dataTable.getColumns().get(i);
        dataTable.getSelectionModel().clearSelection();
        dataTable.getSelectionModel().selectRange(0, col, dataTable.getItems().size() - 1, col);
    }

    private void iChanged() {
        DataSelector selector = currentSelector.get();
        if (selector.getSelection() == null)
            return;

        selectionUpdateFreeze = true;

        switch (selector.getSelectionType()) {
            case Row -> {
                dataTable.getSelectionModel().select(selector.getSelection());
            }
            case Column -> {
                selectCol(selector.getSelection());
            }
            case TopRows -> {
                dataTable.getSelectionModel().selectRange(0, selector.getSelection() + 1);
            }
        }

        selectionUpdateFreeze = false;
    }

    private <T> Map<Integer, T> mapSetup(Iterator<Integer> iter, T[] values, int d) {
        Map<Integer, T> map = new HashMap<>();

        for (T t : values) {
            Integer n = iter.next();
            if (n != null) {
                map.put(n + d, t);
            }
        }

        return map;
    }

    @Override
    public Optional<OrderControls> orderControls() {
        return Optional.of(new OrderControls() {
            @Override
            public void next(ActionEvent event) throws IOException {
                if (i.get() >= selectors.size() - 1) {
                    Iterator<Integer> iter = selectors.stream().map(DataSelector::getSelection).iterator();
                    int headerRows = iter.next() + 1;
                    ColumnNumbers colNumbers = new ColumnNumbers(iter.next(), iter.next(), iter.next(), iter.next());

                    Map<Integer, OfficerPosition> incumbents = mapSetup(iter, OfficerPosition.values(), -headerRows);
                    Map<Integer, OfficerPosition> elect = mapSetup(iter, OfficerPosition.values(), -headerRows);
                    Map<Integer, Award> awards = mapSetup(iter, Award.values(), -headerRows);

                    List<Member> members = DataLoader.loadData(records, headerRows, colNumbers, incumbents, elect, awards);
                    getStageManager().toStageOrder(members);
                }
                else {
                    dataTable.getSelectionModel().clearSelection();
                    i.set(i.get() + 1);
                    iChanged();
                }
            }

            @Override
            public void prev(ActionEvent event) throws IOException {
                if (i.get() <= 0) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Going back will return to the start screen. " +
                            "Are you sure you wish to go back?");
                    if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
                        getStageManager().toStart();
                    }
                }
                else {
                    dataTable.getSelectionModel().clearSelection();
                    i.set(i.get() - 1);
                    iChanged();
                }
            }

            @Override
            public StringBinding instructions() {
                return Bindings.select(currentSelector, "getInstruction").asString();
            }

            @Override
            public StringBinding nextText() {
                return new When(Bindings.selectBoolean(currentSelector, "getRequired").not()
                        .and(Bindings.select(currentSelector, "selection").isNull()))
                        .then("Skip")
                        .otherwise("Next");
            }

            @Override
            public BooleanBinding nextDisable() {
                return Bindings.selectBoolean(currentSelector, "getRequired")
                        .and(Bindings.select(currentSelector, "selection").isNull());
            }
        });
    }
}

