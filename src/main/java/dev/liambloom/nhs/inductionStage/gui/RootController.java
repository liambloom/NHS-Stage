package dev.liambloom.nhs.inductionStage.gui;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.util.Optional;

public class RootController {
    public BorderPane rootPane;
    public Label instructions;
    public Button next;
    public BorderPane bottomBar;
    private OrderControls orderControls;

    public void setPage(StageManager.Page page) {
        rootPane.setCenter(page.node());
        Optional<OrderControls> tempOrderControls = page.controller().orderControls();
        boolean showOrderControls = tempOrderControls.isPresent();
        orderControls = tempOrderControls.orElseGet(OrderControls::blank);

        instructions.textProperty().bind(orderControls.instructions());
        next.textProperty().bind(orderControls.nextText());
        next.disableProperty().bind(orderControls.nextDisable());

        bottomBar.setVisible(showOrderControls);
        bottomBar.setManaged(showOrderControls);
    }

    public void next(ActionEvent event) throws Exception {
        orderControls.next(event);
    }

    public void prev(ActionEvent event) throws Exception {
        orderControls.prev(event);
    }
}
