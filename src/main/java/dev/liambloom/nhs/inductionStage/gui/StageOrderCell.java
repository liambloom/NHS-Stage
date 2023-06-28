package dev.liambloom.nhs.inductionStage.gui;

import dev.liambloom.nhs.inductionStage.SeatingGroup;
import javafx.application.Platform;
import javafx.scene.control.ListCell;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class StageOrderCell extends ListCell<SeatingGroup> {
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
            this.setOpacity(1);
        });

        addEventHandler(DragEvent.DRAG_OVER, event -> {
            if (shouldSkipCellConsuming(event)) {
                return;
            }

            if (sceneToLocal(0, event.getSceneY()).getY() <= getHeight() / 2) {
                setBorder(topBorder());
            }
            else {
                setBorder(bottomBorder());
            }

            event.acceptTransferModes(TransferMode.MOVE);
        });

        addEventHandler(DragEvent.DRAG_EXITED, event -> {
            if (shouldSkipCellConsuming(event)) {
                return;
            }

            setBorder(Border.EMPTY);
        });

        addEventHandler(DragEvent.DRAG_DROPPED, event -> {
            boolean success = false;

            try {
                if (shouldSkipCellConsuming(event)) {
                    return;
                }

                StageOrderCell source = (StageOrderCell) event.getGestureSource();
                int index = getIndex() + (sceneToLocal(0, event.getSceneY()).getY() > getHeight() / 2 ? 1 : 0)
                        + (source.getListView() == getListView() &&  source.getIndex() < getIndex() ? -1 : 0);

                StageOrderController.moveListElement(event, getListView(), index);

                success = true;
            }
            finally {
                event.setDropCompleted(success);
            }
        });

        if (getListView() == null) {
            listViewProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    initializeListView();
                }
            });
        }
        else {
            initializeListView();
        }
    }

    private void initializeListView() {
        getListView().addEventHandler(DragEvent.DRAG_OVER, event -> {
            if (dragIsReorder(event)) {
                if (getIndex() == getListView().getItems().size()) {
                    setBorder(topBorder());
                }

                event.acceptTransferModes(TransferMode.MOVE);

                event.consume();
            }
        });


        getListView().addEventFilter(DragEvent.DRAG_OVER, this::clearBelowListBorder);
        getListView().addEventHandler(DragEvent.DRAG_EXITED, this::clearBelowListBorder);
        getListView().addEventFilter(DragEvent.DRAG_DROPPED, this::clearBelowListBorder);
    }

    private void clearBelowListBorder(DragEvent event) {
        if (dragIsReorder(event) && getIndex() == getListView().getItems().size()) {
            setBorder(Border.EMPTY);
        }
    }

    /**
     * This takes an event and returns whether the handler should return before doing anything. It should only be used
     * on event listeners on this cell, not on other cells or on the {@code ListView}. It will also consume the event
     * if this cell's item is not null.
     *
     * @param event The event
     * @return True if {@code return} should be called before anything happens.
     */
    private boolean shouldSkipCellConsuming(DragEvent event) {
        if (getItem() == null) {
            return true;
        }

        event.consume();

        return !dragIsReorder(event);
    }

    private boolean dragIsReorder(DragEvent event) {
        return event.getGestureSource() != this && event.getGestureSource() instanceof StageOrderCell;
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

    private static Border topBorder() {
        return new Border(new BorderStroke(Color.web("#0096C9"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(5, 0, 0, 0)));
    }

    private static Border bottomBorder() {
        return new Border(new BorderStroke(Color.web("#0096C9"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0, 0, 5, 0)));
    }
}
