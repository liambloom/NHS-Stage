package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.SeatingGroup;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class StageOrderCell extends ListCell<SeatingGroup> {
    private static final DataFormat cellFormat = new DataFormat(StageOrderCell.class.toString());

    public StageOrderCell() {
        addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
            if (getItem() == null) {
                return;
            }

            setOpacity(0.3);

            ClipboardContent content = new ClipboardContent();
            content.putString(getItem().toString());

            startDragAndDrop(TransferMode.MOVE).setContent(content);
        });

        addEventHandler(DragEvent.DRAG_DONE, event -> {
            if (event.isDropCompleted()) {
                this.getListView().getItems().remove(this.getIndex());
            }
            else {
                this.setOpacity(1);
            }
        });

        addEventHandler(DragEvent.DRAG_OVER, event -> {
            if (event.getGestureSource() != this &&
                    event.getGestureSource() instanceof StageOrderCell) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        addEventHandler(DragEvent.DRAG_ENTERED_TARGET, event -> {
            System.out.println(event);
            if (event.getGestureSource() != this &&
                    event.getGestureSource() instanceof StageOrderCell) {
                this.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        });

        addEventHandler(DragEvent.DRAG_EXITED_TARGET, event -> {
            if (event.getGestureSource() != this &&
                    event.getGestureSource() instanceof StageOrderCell) {
                this.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        });

        addEventHandler(DragEvent.DRAG_OVER, event -> {
            if (event.getGestureSource() != this &&
                    event.getGestureSource() instanceof StageOrderCell) {
                this.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        });
    }

    protected void updateItem(SeatingGroup item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.toString());
        }
    }
}
