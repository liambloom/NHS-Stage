package dev.liambloom.nhs.inductionStage.gui;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

public class DataSelector {
    public enum SelectionType {
        Rows,
        Row,
        Column,
    }

    private final SelectionType selectionType;

    @FXML
    private final ReadOnlyStringProperty instruction;

    public DataSelector(SelectionType selectionType, String description) {
        this(selectionType, false, description);
    }

    public DataSelector(SelectionType selectionType, boolean negative, String description) {
        this.selectionType = selectionType;

        StringBuilder builder = new StringBuilder();
        builder.append("Please select the ")
                .append(selectionType.toString().toLowerCase())
                .append(" that ");
        if (negative) {
            builder.append("do");
            if (!selectionType.equals(SelectionType.Rows)) {
                builder.append("es");
            }
             builder.append(" NOT ");
        }
        builder.append("contain");
        if (!selectionType.equals(SelectionType.Rows)) {
            builder.append('s');
        }
        builder.append(' ')
                .append(description)
                .append('.');

        this.instruction = new SimpleStringProperty(builder.toString());
    }

    public SelectionType getSelectionType() {
        return selectionType;
    }

    public String getInstruction() {
        return instruction.get();
    }

    public ReadOnlyStringProperty getInstructionProperty() {
        return instruction;
    }
}