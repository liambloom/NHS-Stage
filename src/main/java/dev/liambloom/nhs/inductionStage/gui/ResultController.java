package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.Member;
import dev.liambloom.nhs.inductionStage.SeatingGroup;
import dev.liambloom.nhs.inductionStage.Stage;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ResultController extends StageManager.Managed {
    private Stage stage;

    @FXML
    private GridPane seatingChart;

    @SuppressWarnings("unused") // https://youtrack.jetbrains.com/issue/IDEA-169099
    @FXML
    private ListDisplayController hallLineController;

    @SuppressWarnings("unused")
    @FXML
    private ListDisplayController newMemberLineupController;

    @SuppressWarnings("unused")
    @FXML
    private ListDisplayController seniorLineupController;

    public void initData(List<Member> members, List<String> vipTable, List<SeatingGroup> stageLeft, List<SeatingGroup> stageRight) {
        stage = new Stage(members.toArray(new Member[0]), 6, vipTable.toArray(new String[0]),
                stageLeft.toArray(new SeatingGroup[0]), stageRight.toArray(new SeatingGroup[0]));

        hallLineController.initData(stage.getHallwayLineup());
        newMemberLineupController.initData(stage.getNewMemberCallupOrder());
        seniorLineupController.initData(stage.getSeniorCallupOrder());


        String[][] chartContent = stage.getLayout();
        for (int i = 0; i < chartContent.length; i++) {
            for (int j = 0; j < chartContent[i].length; j++) {
                Label text = new Label(chartContent[i][j]);
//                text.setWrapText(true);
                text.setMaxWidth(Double.MAX_VALUE);
                text.setMaxHeight(Double.MAX_VALUE);
                text.getStyleClass().add("chartCell");
                GridPane.setFillWidth(text, true);
                GridPane.setFillHeight(text, true);

                seatingChart.add(text, j, i);
            }
        }

        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setMinWidth(20);
        columnConstraints.setMaxWidth(Double.POSITIVE_INFINITY);
        columnConstraints.setHgrow(Priority.SOMETIMES);
        for (int i = 0; i < seatingChart.getColumnCount(); i++) {
            seatingChart.getColumnConstraints().add(columnConstraints);
        }

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setMinHeight(Double.NEGATIVE_INFINITY);
        rowConstraints.setMaxHeight(Double.POSITIVE_INFINITY);
        rowConstraints.setVgrow(Priority.ALWAYS);
        rowConstraints.setValignment(VPos.TOP);
        for (int i = 0; i < seatingChart.getRowCount(); i++) {
            seatingChart.getRowConstraints().add(rowConstraints);
        }
    }

    @FXML
    public void download(ActionEvent event) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save As");
        File outFile = chooser.showSaveDialog(getStageManager().getStage());

        if (outFile == null) {
            return;
        }

        Path out = outFile.toPath();

        if (!Files.exists(out)) {
            Files.createFile(out);
        }
        stage.saveLayout(new CSVPrinter(Files.newBufferedWriter(out), CSVFormat.DEFAULT));
    }

    public void toCsvHelp(ActionEvent event) throws IOException {
        getStageManager().help(HelpPage.CSV);
    }

    @Override
    public Optional<OrderControls> orderControls() {
        return Optional.of(new OrderControls() {
            @Override
            public void next(ActionEvent event) throws Exception {

            }

            @Override
            public void prev(ActionEvent event) throws Exception {
                getStageManager().toPrevPage();
            }

            @Override
            public ObservableStringValue instructions() {
                return new SimpleStringProperty("");
            }

            @Override
            public ObservableStringValue nextText() {
                return new SimpleStringProperty("Next");
            }

            @Override
            public ObservableBooleanValue nextDisable() {
                return new SimpleBooleanProperty(true);
            }
        });
    }
}
