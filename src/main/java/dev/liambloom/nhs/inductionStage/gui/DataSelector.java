package dev.liambloom.nhs.inductionStage.gui;

import javafx.beans.property.*;
import javafx.fxml.FXML;

import java.util.Optional;

public class DataSelector {
    public enum SelectionType {
        TopRows,
        Row,
        Column;

        @Override
        public String toString() {
            if (this.equals(SelectionType.TopRows)) {
                return "Rows";
            }
            else {
                return super.toString();
            }
        }
    }

    @FXML
    private final ReadOnlyObjectProperty<SelectionType> selectionType;

    @FXML
    private final ReadOnlyStringProperty instruction;

    @FXML
    private final ReadOnlyBooleanProperty required;

    @FXML
    private final ObjectProperty<Integer> selection = new SimpleObjectProperty<>(null);

    public DataSelector(SelectionType selectionType, String description) {
        this(selectionType, false, description);
    }

    public DataSelector(SelectionType selectionType, boolean negative, String description) {
        this.selectionType = new SimpleObjectProperty<>(selectionType);
        this.required = new SimpleBooleanProperty(selectionType.equals(SelectionType.Column));

        StringBuilder builder = new StringBuilder();
        builder.append("Please select the ")
                .append(selectionType.toString().toLowerCase())
                .append(" that ");
        if (negative) {
            builder.append("do");
            if (!selectionType.equals(SelectionType.TopRows)) {
                builder.append("es");
            }
             builder.append(" NOT ");
        }
        builder.append("contain");
        if (!selectionType.equals(SelectionType.TopRows)) {
            builder.append('s');
        }
        builder.append(' ')
                .append(description);

        if (!getRequired()) {
            builder.append(" (optional)");
        }

        builder.append('.');

        this.instruction = new SimpleStringProperty(builder.toString());
    }

    public SelectionType getSelectionType() {
        return selectionType.get();
    }

    public ReadOnlyObjectProperty<SelectionType> getSelectionTypeProperty() {
        return selectionType;
    }

    public String getInstruction() {
        return instruction.get();
    }

    public ReadOnlyStringProperty getInstructionProperty() {
        return instruction;
    }

    public boolean getRequired() {
        return required.get();
    }

    public ReadOnlyBooleanProperty getRequiredProperty() {
        return required;
    }

    public Integer getSelection() {
        return selection.get();
    }

    public void setSelection(Integer selection) {
        this.selection.set(selection);
    }

    public ObjectProperty<Integer> selectionProperty() {
        return selection;
    }
}
