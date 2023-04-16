package dev.liambloom.nhs.inductionStage.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main extends Application {
    Stage primaryStage;
    Scene startScene;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> handleError(throwable, stage));

        Pane startContent = FXMLLoader.load(getClass().getResource("/views/Start.fxml"));
        startScene = new Scene(startContent);
        startScene.getStylesheets().add(getClass().getResource("/css/Start.css").toExternalForm());

        stage.setTitle("Stage Builder for NHS");
        stage.setScene(startScene);
        stage.show();
    }

    private void handleError(Throwable throwable, Stage stage) {
        System.err.println("Error");
        if (Platform.isFxApplicationThread()) {
            while (throwable.getCause() != null && (throwable.getStackTrace()[0].getModuleName().startsWith("javafx")
                    || throwable instanceof InvocationTargetException)) {
                throwable = throwable.getCause();
            }

            Alert alert = new Alert(Alert.AlertType.ERROR);

            if (throwable instanceof Exception) {
                alert.setHeaderText("Exception: " + throwable.getMessage());
                alert.setContentText("You will now be returned to the starting page of this application.");
                alert.showAndWait();
                primaryStage.setScene(startScene);
            }
            else {
                alert.setHeaderText("Error: " + throwable.getMessage());
                alert.setContentText("The application will now close.");
                alert.showAndWait();
                System.exit(1);
            }
        }
        else {
            throwable.printStackTrace();
            System.exit(1);
        }
    }
}
