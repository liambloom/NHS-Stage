package dev.liambloom.nhs.inductionStage.gui;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;

import java.io.IOException;

public interface OrderControls {
    public static OrderControls blank() {
        return new OrderControls() {
            public void next(ActionEvent event) { }
            public void prev(ActionEvent event) { }
            public ObservableStringValue instructions() {
                return new SimpleStringProperty("If you can see this, something is broken");
            }

            public ObservableStringValue nextText() {
                return new SimpleStringProperty("");
            }

            @Override
            public ObservableBooleanValue nextDisable() {
                return new SimpleBooleanProperty(false);
            }
        };
    }

    void next(ActionEvent event) throws Exception;
    void prev(ActionEvent event) throws Exception;
    ObservableStringValue instructions();
    ObservableStringValue nextText();
    ObservableBooleanValue nextDisable();
}
