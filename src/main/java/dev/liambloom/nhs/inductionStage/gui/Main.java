package dev.liambloom.nhs.inductionStage.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Pane content = FXMLLoader.load(Main.class.getResource("/views/Start.fxml"));

        Scene startScene = new Scene(content);
        startScene.getStylesheets().addAll(Stream.of("main")
                .map(s -> "/css/" + s + ".css")
                .map(Main.class/*.getClassLoader()*/::getResource)
                .map(URL::toExternalForm)
                .collect(Collectors.toList()));

//        content.maxWidthProperty().bind(stage.widthProperty());
//        content.minWidthProperty().bind(stage.widthProperty());
//        content.maxHeightProperty().bind(stage.heightProperty());
//        content.minHeightProperty().bind(stage.heightProperty());

        startScene.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
            File memberList = fileChooser.showOpenDialog(stage);
            if (memberList != null) {
                try {
                    CSVParser.parse(memberList, Charset.defaultCharset(), CSVFormat.DEFAULT);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

        });

        stage.setTitle("Stage Builder for NHS");
        stage.setScene(startScene);
        stage.show();
    }
}
