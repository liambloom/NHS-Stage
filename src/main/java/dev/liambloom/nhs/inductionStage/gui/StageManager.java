package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
            try {
                throwable.printStackTrace();

                if (Platform.isFxApplicationThread()) {
                    while (throwable.getCause() != null
                            && (Optional.ofNullable(throwable.getStackTrace()[0].getModuleName())
                                    .map(n -> n.startsWith("javafx"))
                                    .orElse(false)
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
                    } else {
                        alert.setHeaderText("Error: " + message);
                        alert.setContentText("The application will now close.");
                        alert.showAndWait();
                        System.exit(1);
                    }
                } else {
                    System.exit(1);
                }
            }
            catch (Throwable e) {
                System.exit(1);
            }
        });

        FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("/views/Root.fxml"));
        Pane rootContent = rootLoader.load();
        rootContent.getStylesheets().add(getClass().getResource("/css/Root.css").toExternalForm());
        rootController = rootLoader.getController();

        stage.setScene(new Scene(rootContent, 600, 400));

        List<String> params = getParameters().getRaw();
        if (params.isEmpty() || !params.get(0).equals("debugStart")) {
            toStart();
        }
        else {
            gotoDebugStart(params.get(1));
        }

        stage.setTitle("Stage Builder for NHS");
        stage.show();
    }

    public void gotoDebugStart(String page) throws IOException {
        Path memberList = Path.of("members.csv");

        switch (page) {
            case "Start" -> toStart();
            case "DataEntry" -> toDataEntry(CSVParser.parse(memberList, Charset.defaultCharset(), CSVFormat.DEFAULT).getRecords());
            case "Results" -> {
                List<Member> members = DataLoader.loadData(CSVParser.parse(memberList, Charset.defaultCharset(), CSVFormat.DEFAULT), 3,
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

                toResults(members);
            }
        }
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
            startContent = new Page("Start");
//            startScene = newScene(startContent);
            startContent.node().getStylesheets().add(getClass().getResource("/css/Start.css").toExternalForm());

            Managed controller = startContent.controller();
            controller.stageManager = this;
        }

        dataEntry = null;
        resultContent = null;
//        stage.setScene(startScene);
        rootController.setPage(startContent);
    }

    public void toDataEntry(List<CSVRecord> csv) throws IOException {
        dataEntry = new Page("DataEntry");
//        dataEntry = newScene(dataContent);
        dataEntry.node().getStylesheets().add(getClass().getResource("/css/DataEntry.css").toExternalForm());

        DataEntryController controller = (DataEntryController) dataEntry.controller();
        controller.initData(csv);
        ((Managed) controller).stageManager = this;

        toLastDataEntry();
    }

    public void toLastDataEntry() {
        resultContent = null;
        rootController.setPage(dataEntry);
    }

    public void toResults(List<Member> members) throws IOException {

        resultContent = new Page("Results");
        resultContent.node().getStylesheets().add(getClass().getResource("/css/Results.css").toExternalForm());

        ResultController controller = (ResultController) resultContent.controller();
        controller.initData(members);
        ((Managed) controller).stageManager = this;

        rootController.setPage(resultContent);
    }

    public void help(HelpPage page) {

    }

    public static abstract class Managed {
        private StageManager stageManager;

        protected StageManager getStageManager() {
            return stageManager;
        }

        protected Optional<OrderControls> orderControls() {
            return Optional.empty();
        }
    }
}
