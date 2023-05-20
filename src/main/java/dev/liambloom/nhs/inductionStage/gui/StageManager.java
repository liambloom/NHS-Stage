package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import java.util.Map;
import java.util.Optional;

public class StageManager extends Application {
    // TODO: Make it so there is only one scene, and the root node changes

    private Stage stage;
    private RootController rootController;
    private Page startContent;
    private Page helpContent;
    private Page dataEntry;
    private Page resultContent;

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

                String message = Optional.ofNullable(throwable.getMessage()).orElse(throwable.getClass().getName());

                if (throwable instanceof Exception) {
                    alert.setHeaderText("Exception: " + message);
                    alert.setContentText("You will now be returned to the starting page of this application.");
                    alert.showAndWait();
                    try {
                        toStart();
                    } catch (IOException e) {
                        System.exit(1);
                    }
                }
                else {
                    alert.setHeaderText("Error: " + message);
                    alert.setContentText("The application will now close.");
                    alert.showAndWait();
                    System.exit(1);
                }
            }
            else {
                System.exit(1);
            }
        });

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Root.fxml"));
        BorderPane root = loader.load();
        root.getStylesheets().add(getClass().getResource("/css/DataEntry.css").toExternalForm());
        stage.setScene(new Scene(root));
        rootController = loader.getController();
        ((Managed) rootController).stageManager = this;

//        toStart();
        toDataEntry(CSVParser.parse(Path.of("members.csv"), Charset.defaultCharset(), CSVFormat.DEFAULT).getRecords());
        List<Member> members = DataLoader.loadData(CSVParser.parse(Path.of("members.csv"), Charset.defaultCharset(), CSVFormat.DEFAULT), 3,
                new ColumnNumbers(0, 1, 4, 6),
                Map.of(
                        4, OfficerPosition.Secretary,
                        6, OfficerPosition.President,
                        9, OfficerPosition.Treasurer,
                        10, OfficerPosition.VicePresident
                ),
                Map.of(
                        42, OfficerPosition.President,
                        43, OfficerPosition.VicePresident,
                        48, OfficerPosition.Secretary,
                        51, OfficerPosition.Treasurer
                ),
                Map.of(
                        1, Award.Service,
                        33, Award.Character,
                        8, Award.Scholarship,
                        38, Award.Leadership
                ));

//        toResults(members);

        stage.setTitle("Stage Builder for NHS");
//        stage.setMaximized(true);
        stage.show();
    }

//    private Scene newScene(Parent content) {
//        Scene prev = stage.getScene();
//        if (prev == null) {
//            return new Scene(content);
//        }
//        else {
//            return new Scene(content, prev.getWidth(), prev.getHeight());
//        }
//    }


    public void toStart() throws IOException {
        if (startContent == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Start.fxml"));

            startContent = new Page(loader);
//            startScene = newScene(startContent);
            startContent.node().getStylesheets().add(getClass().getResource("/css/Start.css").toExternalForm());

            startContent.controller().stageManager = this;
        }

        dataEntry = null;
        resultContent = null;
//        stage.setScene(startScene);
        rootController.setPage(startContent);
    }

    public void toDataEntry(List<CSVRecord> csv) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DataEntry.fxml"));

        dataEntry = new Page(loader);
//        dataEntry = newScene(dataContent);
        dataEntry.node().getStylesheets().add(getClass().getResource("/css/DataEntry.css").toExternalForm());

//        DataEntryController controller = loader.getController();
        ((DataEntryController) dataEntry.controller()).initData(csv);
        dataEntry.controller().stageManager = this;

        toLastDataEntry();
    }

    public void toLastDataEntry() {
        resultContent = null;
        rootController.setPage(dataEntry);
    }

    public void toResults(List<Member> members) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Results.fxml"));

        resultContent = new Page(loader);
        resultContent.node().getStylesheets().add(getClass().getResource("/css/Results.css").toExternalForm());

//        ResultController controller = loader.getController();
        ((ResultController) resultContent.controller()).initData(members);
        resultContent.controller().stageManager = this;

        rootController.setPage(resultContent);
    }

    public void help(HelpPage page) {

    }

    public static abstract class Managed {
        @FXML
        private final ReadOnlyBooleanProperty ordered = new ReadOnlyBooleanWrapper(isOrdered()).getReadOnlyProperty();

        private StageManager stageManager;

        protected StageManager getStageManager() {
            return stageManager;
        }

        public abstract boolean isOrdered();

        public final ReadOnlyBooleanProperty orderedProperty() {
            return ordered;
        }

        public abstract void next(ActionEvent event) throws Exception;

        public abstract void prev(ActionEvent event) throws Exception;

        public abstract ObservableBooleanValue requiredProperty();

        public boolean isRequired() {
            return requiredProperty().get();
        }

        public abstract ObservableBooleanValue completedProperty();

        public boolean isCompleted() {
            return completedProperty().get();
        }

        public abstract ObservableStringValue instructionsProperty();

        public String getInstructions() {
            return instructionsProperty().get();
        }
    }

    public static abstract class UnorderedManaged extends Managed {

        @Override
        public final boolean isOrdered() {
            return false;
        }

        @Override
        public void next(ActionEvent event) throws Exception {
            throw new UnsupportedOperationException("next");
        }

        @Override
        public void prev(ActionEvent event) throws Exception {
            throw new UnsupportedOperationException("prev");
        }

        @Override
        public ObservableBooleanValue requiredProperty() {
//            throw new UnsupportedOperationException("required");
            return new SimpleBooleanProperty(false);
        }

        @Override
        public ObservableBooleanValue completedProperty() {
//            throw new UnsupportedOperationException("completed");
            return new SimpleBooleanProperty(false);
        }

        @Override
        public ObservableStringValue instructionsProperty() {
//            throw new UnsupportedOperationException("instructions");
            return new SimpleStringProperty("");
        }
    }

    public static abstract class OrderedManaged extends Managed {
        @Override
        public final boolean isOrdered() {
            return true;
        }

        @Override
        public ObservableBooleanValue requiredProperty() {
            return new SimpleBooleanProperty(true);
        }
    }
}
