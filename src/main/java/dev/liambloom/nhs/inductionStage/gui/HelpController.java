package dev.liambloom.nhs.inductionStage.gui;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.TextFlow;

public class HelpController extends StageManager.Managed {
    public BorderPane root;
    public ScrollPane content;
    public HBox titleBox;

    public TextFlow foo;
    private final ReadOnlyStringWrapper editableTitle = new ReadOnlyStringWrapper();
    public final ReadOnlyStringProperty title = editableTitle.getReadOnlyProperty();

    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    public void initialize() {
        root.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton().equals(MouseButton.BACK)) {
                getStageManager().toPrevPage();
            }
        });
    }

    public String getTitle() {
        return title.get();
    }

    void toPage(HelpPage helpPage, StageManager.Page page) {
        content.setContent(page.node());
        ((Region) page.node()).setPadding(new Insets(10));
        editableTitle.set(helpPage.name());
    }

    public void toPrev() {
        getStageManager().toPrevPage();
    }
}
