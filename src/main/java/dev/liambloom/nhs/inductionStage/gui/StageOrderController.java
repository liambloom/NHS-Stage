package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.Member;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class StageOrderController extends StageManager.Managed {
    private List<Member> members;

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    @Override
    public Optional<OrderControls> orderControls() {
        return Optional.of(new OrderControls() {
            @Override
            public void next(ActionEvent event) throws IOException {
                getStageManager().toResults(members);
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
