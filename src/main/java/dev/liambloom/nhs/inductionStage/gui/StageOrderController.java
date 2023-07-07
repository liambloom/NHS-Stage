package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.Member;
import dev.liambloom.nhs.inductionStage.SeatingGroup;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StageOrderController extends StageManager.Managed {
    private List<Member> members;

    @FXML
    private ListView<SeatingGroup> stageLeft;

    @FXML
    private ListView<SeatingGroup> stageRight;

    @FXML
    private VBox vipTable;

    public static List<SeatingGroup> stageLeftDefault = List.of(SeatingGroup.OfficersElect, SeatingGroup.NewSeniors,
            SeatingGroup.NewJuniors, SeatingGroup.NewSophomores);
    public static List<SeatingGroup> stageRightDefault = List.of(SeatingGroup.AwardWinners, SeatingGroup.ReturningSeniors,
            SeatingGroup.ReturningJuniors);

    public void initialize() {
        stageLeft.getItems().addAll(stageLeftDefault);
        stageRight.getItems().addAll(stageRightDefault);

        //noinspection unchecked
        for (ListView<SeatingGroup> stageSide : new ListView[]{stageLeft, stageRight}) {
            stageSide.setCellFactory(view -> new StageOrderCell());
            stageSide.setPrefHeight(23 * 7 + 7);

            stageSide.addEventHandler(DragEvent.DRAG_OVER, event -> {
                if (event.getGestureSource() instanceof StageOrderCell && stageSide.getItems().isEmpty()) {
                    stageSide.setBorder(new Border(new BorderStroke(Color.web("#0096C9"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                            new BorderWidths(5, 0, 0, 0))));

                    event.acceptTransferModes(TransferMode.MOVE);
                }
            });

            stageSide.addEventHandler(DragEvent.DRAG_EXITED, event -> stageSide.setBorder(Border.EMPTY));

            stageSide.addEventHandler(DragEvent.DRAG_DROPPED, event -> {
                if (event.getGestureSource() instanceof StageOrderCell) {
                    stageSide.setBorder(Border.EMPTY);

                    moveListElement(event, stageSide, stageSide.getItems().size()
                            - (((StageOrderCell) event.getGestureSource()).getListView() == stageSide ? 1 : 0));
                }
            });
        }
    }

    static void moveListElement(DragEvent event, ListView<SeatingGroup> target, int index) {
        SeatingGroup moved = SeatingGroup.valueOf(event.getDragboard().getString());
        StageOrderCell source = (StageOrderCell) event.getGestureSource();

        source.getListView().getItems().remove(moved);

        target.getItems().add(index, moved);

        source.getListView().getSelectionModel().clearSelection();
        target.getSelectionModel().select(index);
        target.requestFocus();
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    @Override
    public Optional<OrderControls> orderControls() {
        return Optional.of(new OrderControls() {
            @Override
            public void next(ActionEvent event) throws IOException {
                getStageManager().toResults(members,
                        vipTable.getChildren()
                                .stream()
                                .filter(TextField.class::isInstance)
                                .map(TextField.class::cast)
                                .map(TextField::getText)
                                .toList(),
                        stageLeft.getItems(), stageRight.getItems());
            }

            @Override
            public void prev(ActionEvent event) {
                getStageManager().toPrevPage();
            }

            @Override
            public ObservableStringValue instructions() {
                return new SimpleStringProperty("Drag and drop the groups to the section and order you want them in");
            }

            @Override
            public ObservableStringValue nextText() {
                return new SimpleStringProperty("Next");
            }

            @Override
            public ObservableBooleanValue nextDisable() {
                return new SimpleBooleanProperty(false);
            }
        });
    }
}
