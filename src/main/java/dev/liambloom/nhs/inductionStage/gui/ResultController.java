package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.Member;
import dev.liambloom.nhs.inductionStage.Stage;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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

public class ResultController extends StageManager.Managed {
    private Stage stage;
    public GridPane seatingChart;
    public ComboBox<String> lineupDropdown;
    public VBox lineupBox;
    public Rectangle lineupBorder;
    public Pane lineupBottomContent;
    public Pane lineupBoxContainer;
    public Pane lineupBorderPane;

    public void initialize() {
        lineupBox.prefHeightProperty().bind(lineupBoxContainer.heightProperty().subtract(20));
//        lineupRoot.prefHeightProperty().bind(((Region) lineupRoot.getParent()).heightProperty());

        lineupBoxContainer.parentProperty().addListener((obs, oldV, newV) -> {
            System.out.println(newV);
        });

        lineupBorder.widthProperty().bind(lineupBox.widthProperty());
        lineupBorder.heightProperty().bind(lineupBox.heightProperty());

        Rectangle lineupClip = new Rectangle();
        lineupClip.widthProperty().bind(lineupBorder.widthProperty());
        lineupClip.heightProperty().bind(lineupBorder.heightProperty());
        lineupClip.setArcHeight(10);
        lineupClip.setArcWidth(10);
        lineupBox.setClip(lineupClip);
//
//
        lineupDropdown.setItems(FXCollections.observableArrayList("Comma Seperated", "Separate Lines", "Bulleted List", "Numbered List"));
//
        lineupDropdown.valueProperty().addListener((observable, oldVal, newVal) -> {
            lineupBottomContent.getChildren().clear();
            switch (newVal) {
                case "Comma Seperated" -> {
                    StringBuilder builder = new StringBuilder();
                    Iterator<Member> iter = stage.getLineup().iterator();
                    if (iter.hasNext()) {
                        builder.append(iter.next());
                    }
                    while (iter.hasNext()) {
                        builder.append(", ")
                                .append(iter.next());
                    }

                    Label text = new Label(builder.toString());
                    text.setWrapText(true);

                    lineupBottomContent.getChildren().add(text);
                }
                case "Separate Lines" -> {
                    for (Member member : stage.getLineup()) {
                        lineupBottomContent.getChildren().add(new Label(member.toString()));
                    }
                }
            }
        });

    }

    public void initData(List<Member> members) {
        stage = new Stage(members.toArray(new Member[0]), 6);

        lineupBorderPane.setPrefHeight(1E15);
        lineupDropdown.setValue(lineupDropdown.getItems().get(0));

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

    public void copyLineup(ActionEvent event) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        String lineup = stage.getLineup().toString();
        content.putString(lineup.substring(1, lineup.length() - 1));
        clipboard.setContent(content);
    }

    @Override
    public Optional<OrderControls> orderControls() {
        return Optional.of(new OrderControls() {
            @Override
            public void next(ActionEvent event) throws Exception {

            }

            @Override
            public void prev(ActionEvent event) throws Exception {
                getStageManager().toLastDataEntry();
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

    public void printSizes(ActionEvent event) {
//        System.out.println(seatingChart.getBoundsInLocal());
//        System.out.println(seatingChart.getPrefHeight());
//        System.out.println(seatingChart.getMinHeight());
    }
}
