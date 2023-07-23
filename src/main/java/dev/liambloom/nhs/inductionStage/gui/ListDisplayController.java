package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.Member;
import dev.liambloom.nhs.inductionStage.Stage;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ListDisplayController {
    private Stage stage;

    @FXML
    private ComboBox<String> lineupDropdown;

    @FXML
    private VBox lineupBox;

    @FXML
    private Rectangle lineupBorder;

    @FXML
    private Pane lineupBottomContent;

    @FXML
    private Pane lineupBorderPane;

    public void initialize() {
        lineupBorderPane.setPrefHeight(1E15);

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
        lineupDropdown.setItems(FXCollections.observableArrayList(
                Arrays.stream(LineupFormatOptions.values())
                        .map(Object::toString)
                        .toArray(String[]::new)));
//
        lineupDropdown.valueProperty().addListener((observable, oldVal, newVal) -> {
            lineupBottomContent.getChildren().clear();
            switch (LineupFormatOptions.fromString(newVal)) {
                case CommaSeperated -> {
                    Label text = new Label(lineupFencepost(", "));
                    text.setWrapText(true);

                    lineupBottomContent.getChildren().add(text);
                }
                case SeparateLines -> {
                    for (Member member : stage.getHallwayLineup()) {
                        lineupBottomContent.getChildren().add(new Label(member.toString()));
                    }
                }
                case BulletedList -> {
                    for (Member member : stage.getHallwayLineup()) {
                        lineupBottomContent.getChildren().add(new Label("\u2022 " + member));
                    }
                }
                case NumberedList -> {
                    int i = 1;
                    for (Member member : stage.getHallwayLineup()) {
                        lineupBottomContent.getChildren().add(new Label(i++ + ".\t" + member));
                    }
                }
            }
        });

    }

    public void initData(Stage stage) {
        this.stage = stage;

        lineupDropdown.setValue(lineupDropdown.getItems().get(0));
    }

    private String stringFencePost(Iterator<?> iter, String post) {
        StringBuilder builder = new StringBuilder();
        if (iter.hasNext()) {
            builder.append(iter.next());
        }
        while (iter.hasNext()) {
            builder.append(post)
                    .append(iter.next());
        }
        return builder.toString();
    }

    private String lineupFencepost(String post) {
        return stringFencePost(stage.getHallwayLineup().iterator(), post);
    }

    @FXML
    public void copyLineup(ActionEvent event) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        List<Member> lineup = stage.getHallwayLineup();

        switch (LineupFormatOptions.fromString(lineupDropdown.getValue())) {
            case CommaSeperated -> {
                content.putString(lineupFencepost(", "));
            }
            case SeparateLines -> {
                content.putString(lineupFencepost(System.lineSeparator()));
                content.putHtml(lineupFencepost("<br>"));
            }
            case BulletedList -> {
                content.putString(lineup.stream().map(m -> "- " + m).collect(Collectors.joining(System.lineSeparator())));
                content.putHtml("<ul>" + lineup.stream().map(m -> "<li>" + m + "</li>").collect(Collectors.joining()) + "</ul>");
            }
            case NumberedList -> {
                StringBuilder builder = new StringBuilder();
                int i = 1;
                for (Member member : lineup) {
                    builder.append(i++).append(".\t").append(member).append(System.lineSeparator());
                }
                content.putString(builder.toString());

                content.putHtml("<ol>" + lineup.stream().map(m -> "<li>" + m + "</li>").collect(Collectors.joining()) + "</ol>");
            }
        }

        clipboard.setContent(content);
    }
}
