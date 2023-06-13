package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.Member;
import dev.liambloom.nhs.inductionStage.Stage;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ResultController extends StageManager.Managed {
    private Stage stage;

    public void initData(List<Member> members) {
        stage = new Stage(members.toArray(new Member[0]), 6);
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

    public void reset(ActionEvent event) throws IOException {
        getStageManager().toStart();
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
