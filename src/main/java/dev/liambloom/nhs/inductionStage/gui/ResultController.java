package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.Member;
import dev.liambloom.nhs.inductionStage.Stage;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ResultController extends StageManager.Managed {
    private Stage stage;
    public GridPane seatingChart;

    public VBox foo;

    public void initData(List<Member> members) {
        stage = new Stage(members.toArray(new Member[0]), 6);

        String[][] chartContent = stage.getLayout();
        for (int i = 0; i < chartContent.length; i++) {
            for (int j = 0; j < chartContent[i].length; j++) {
                Pane pane = new Pane();
                pane.getStyleClass().add("chartCell");
                pane.getStyleClass().add("chartCell");
                pane.minWidth(20);

                Label text = new Label(chartContent[i][j]);
                text.setWrapText(true);
//                text.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> pane.getBoundsInLocal().getWidth(), pane.boundsInLocalProperty()));
//                text.maxWidthProperty().bind(pane.minWidthProperty());
//                System.out.println(pane.getMinWidth());
//
//                pane.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> text.getBoundsInLocal().getHeight(), text.boundsInLocalProperty()));
//                text.setTextOrigin(VPos.TOP);
//                text.wrappingWidthProperty().bind(Bindings.createDoubleBinding(() -> {
//                    System.out.println("foo");
//                    double r = text.getBoundsInLocal().getHeight();
//                    System.out.println(r);
//                    return r;
//                }));

                pane.getChildren().add(text);

//                GridPane.setVgrow(pane, Priority.ALWAYS);
//                GridPane.setHgrow(pane, Priority.SOMETIMES);
                GridPane.setConstraints(pane, j, i, 1, 1, HPos.LEFT, VPos.TOP, Priority.SOMETIMES, Priority.ALWAYS);
                seatingChart.getChildren().add(pane);
            }
        }

        ColumnConstraints columnConstraints = new ColumnConstraints();
        // do stuff
        for (int i = 0; i < seatingChart.getColumnCount(); i++) {
            seatingChart.getColumnConstraints().add(columnConstraints);
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
}
