package dev.liambloom.nhs.inductionStage.gui;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

// Hyperlinks don't wrap in a textflow, and I want them to, so this is a custom implementation that extends
//  text to achieve what I want
public class HyperText extends Text {
    private static StageManager stageManager;

    public HyperText() {
        super();
        initialize();
    }

    public HyperText(double x, double y, String text) {
        super(x, y, text);
        initialize();
    }

    public HyperText(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        onAction.addListener((obs, oldValue, newValue) -> {
            if (oldValue != null) {
                removeEventHandler(ActionEvent.ACTION, oldValue);
            }
            if (newValue != null) {
                addEventHandler(ActionEvent.ACTION, newValue);
            }
        });

        visited.addListener((obs, oldValue, newValue) -> {
            pseudoClassStateChanged(PseudoClass.getPseudoClass("visited"), newValue);
        });

        armed.addListener((obs, oldValue, newValue) -> {
            pseudoClassStateChanged(PseudoClass.getPseudoClass("armed"), newValue);
        });

        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (isPrimaryButtonOnly(event)) {
                armedInner.set(true);
                event.setDragDetect(true);
            }
            else {
                event.setDragDetect(false);
            }
        });

        addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (armed.get()) {
                fireEvent(new ActionEvent(this, this));
                armedInner.set(false);
            }
        });

        addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            armedInner.set(getBoundsInLocal().contains(event.getX(), event.getY()));
        });

        addEventHandler(ActionEvent.ACTION, event -> {
            visited.set(true);
            if (getHref() != null) {
                stageManager.getHostServices().showDocument(getHref());
            }
        });
    }

    private boolean isPrimaryButtonOnly(MouseEvent event) {
        return event.isPrimaryButtonDown() && !event.isMiddleButtonDown() && !event.isSecondaryButtonDown()
                && !event.isControlDown() && !event.isAltDown() && !event.isShiftDown() && !event.isMetaDown()
                && !event.isShortcutDown();
    }

    static void setStageManager(StageManager stageManager) {
        HyperText.stageManager = stageManager;
    }

    public final BooleanProperty visited = new SimpleBooleanProperty();

    public BooleanProperty visitedProperty() {
        return visited;
    }

    public boolean isVisited() {
        return visited.get();
    }

    public void setVisited(boolean value) {
        visited.set(value);
    }

    private final ReadOnlyBooleanWrapper armedInner = new ReadOnlyBooleanWrapper();
    public final ReadOnlyBooleanProperty armed = armedInner.getReadOnlyProperty();

    public ReadOnlyBooleanProperty armedProperty() {
        return armed;
    }

    public boolean isArmed() {
        return armed.get();
    }

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>();

    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    public EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    public void setOnAction(EventHandler<ActionEvent> value) {
        onAction.set(value);
    }

    public final StringProperty href = new SimpleStringProperty();

    public StringProperty hrefProperty() {
        return href;
    }

    public String getHref() {
        return href.get();
    }

    public void setHref(String value) {
        href.set(value);
    }
}
