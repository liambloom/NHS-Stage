package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.*;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

public class StageManager extends Application {
    private Stage stage;
    private RootController rootController;
    private Page startContent;
    private Page helpContent;
    private Stack<Page> prevPages = new Stack<>();
    private Page currentPage;

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

        HyperText.setStageManager(this);

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

        switch (page) {
            case "Start" -> toStart();
            case "DataEntry" -> toDataEntry(CSVParser.parse(memberList, Charset.defaultCharset(), CSVFormat.DEFAULT).getRecords());
            case "StageOrder" -> toStageOrder(members);
            case "Results" -> toResults(members, List.of("Mayor", "Superintendent", "Principal", "Advisor"),
                    StageOrderController.stageLeftDefault, StageOrderController.stageRightDefault);
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
            startContent.addStyles("Start");
        }

        prevPages.clear();
        toPage(startContent);
    }

    public void toDataEntry(List<CSVRecord> csv) throws IOException {
        Page dataEntry = new Page("DataEntry");
        dataEntry.addStyles("DataEntry");

        DataEntryController controller = dataEntry.controller();
        controller.initData(csv);

        toPage(dataEntry);
    }

    public void toStageOrder(List<Member> members) throws IOException {
        Page stageOrder = new Page("StageOrder");
        stageOrder.addStyles("StageOrder");
        stageOrder.node.getStylesheets().add(getClass().getResource("/css/StageOrder.css").toExternalForm());

        StageOrderController controller = stageOrder.controller();
        controller.setMembers(members);

        toPage(stageOrder);
    }

    public void toResults(List<Member> members, List<String> vipTable, List<SeatingGroup> stageLeft, List<SeatingGroup> stageRight) throws IOException {
        Page resultContent = new Page("Results");
        resultContent.addStyles("Results");

        ResultController controller = resultContent.controller();
        controller.initData(members, vipTable, stageLeft, stageRight);

        toPage(resultContent);
    }

    public void help(HelpPage page) throws IOException {
        Page helpPage = new Page("Help");
        helpPage.addStyles("Help");

        Page content = new Page("help/" + page.name());

        HelpController controller = helpPage.controller();
        controller.toPage(page, content);

        toPage(helpPage);
    }

    private void toPage(Page page) {
        if (currentPage != null) {
            prevPages.add(currentPage);
        }
        currentPage = page;
        rootController.setPage(page);
    }

    public void toPrevPage() {
        currentPage = prevPages.pop();
        rootController.setPage(currentPage);
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

    public class Page {
        public final Parent node;
        public final Managed controller;

        public Page(Parent node, Managed controller) {
            this.node = node;
            if (controller != null) {
                controller.stageManager = StageManager.this;
            }
            this.controller = controller;
        }

        public Page(FXMLLoader loader) throws IOException {
            this(loader.load(), loader.getController());
        }

        public Page(String name) throws IOException {
            this(new FXMLLoader(Page.class.getResource("/views/" + name + ".fxml")));
        }

        public Parent node() {
            return node;
        }

        public <T extends Managed> T controller() {
            return (T) controller;
        }

        public void addStyles(String name) {
            node.getStylesheets().add(getClass().getResource("/css/" + name + ".css").toExternalForm());
        }
    }
}
