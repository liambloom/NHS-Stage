package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.Member;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

public class StageManager extends Application {
    private Stage stage;
    private Scene startScene;
    private Scene helpScene;
    private Scene dataEntry;
    private Scene resultScene;

    public Stage getStage() {
        return this.stage;
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();

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
                    try {
                        toStart();
                    } catch (IOException e) {
                        System.exit(1);
                    }
                }
                else {
                    alert.setHeaderText("Error: " + throwable.getMessage());
                    alert.setContentText("The application will now close.");
                    alert.showAndWait();
                    System.exit(1);
                }
            }
            else {
                System.exit(1);
            }
        });

        toStart();
        //toDataEntry(CSVParser.parse(Path.of("members.csv"), Charset.defaultCharset(), CSVFormat.DEFAULT));

        stage.setTitle("Stage Builder for NHS");
        stage.setMaximized(true);
        stage.show();
    }

    private Scene newScene(Parent content) {
        Scene prev = stage.getScene();
        if (prev == null) {
            return new Scene(content);
        }
        else {
            return new Scene(content, prev.getWidth(), prev.getHeight());
        }
    }

    public void toStart() throws IOException {
        if (startScene == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Start.fxml"));

            Pane startContent = loader.load();
            startScene = newScene(startContent);
            startScene.getStylesheets().add(getClass().getResource("/css/Start.css").toExternalForm());

            Managed controller = loader.getController();
            controller.stageManager = this;
        }

        dataEntry = null;
        resultScene = null;
        stage.setScene(startScene);
    }

    public void toDataEntry(List<CSVRecord> csv) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DataEntry.fxml"));

        BorderPane dataContent = loader.load();
        dataEntry = newScene(dataContent);
        dataEntry.getStylesheets().add(getClass().getResource("/css/DataEntry.css").toExternalForm());

        DataEntry controller = loader.getController();
        controller.initData(csv);
        ((Managed) controller).stageManager = this;

        resultScene = null;

        stage.setScene(dataEntry);
    }

    public void toResults(List<Member> members) {

    }

    public void help(HelpPage page) {

    }

    public static abstract class Managed {
        private StageManager stageManager;

        protected StageManager getStageManager() {
            return stageManager;
        }
    }
}
