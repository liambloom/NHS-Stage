package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.Member;
import dev.liambloom.nhs.inductionStage.SeatingGroup;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StageOrderController extends StageManager.Managed {
    private List<Member> members;
    public ListView<SeatingGroup> stageLeft;
    public ListView<SeatingGroup> stageRight;

    public static List<SeatingGroup> stageLeftDefault = List.of(SeatingGroup.OfficersElect, SeatingGroup.NewSeniors,
            SeatingGroup.NewJuniors, SeatingGroup.NewSophomores);
    public static List<SeatingGroup> stageRightDefault = List.of(SeatingGroup.AwardWinners, SeatingGroup.ReturningSeniors,
            SeatingGroup.ReturningJuniors);

    public void initialize() {
        stageLeft.getItems().addAll(stageLeftDefault);
        stageLeft.setCellFactory(view -> new StageOrderCell());

        stageRight.getItems().addAll(stageRightDefault);
        stageRight.setCellFactory(view -> new StageOrderCell());

//        stageRight.getFixedCellSize()
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    @Override
    public Optional<OrderControls> orderControls() {
        return Optional.of(new OrderControls() {
            @Override
            public void next(ActionEvent event) throws IOException {
                getStageManager().toResults(members, stageLeft.getItems(), stageRight.getItems());
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
