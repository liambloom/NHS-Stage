package dev.liambloom.nhs.inductionStage.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Objects;

public class RootController extends StageManager.UnorderedManaged {
    public BorderPane borderPane;
    public Button next;

    public void initialize() {
//        System.out.println(Bindings.createStringBinding(() -> null).getClass());
//        System.out.println(Bindings.selectString(new SimpleObjectProperty<>(), "").getClass());
//        System.out.println(Bindings.when(new SimpleBooleanProperty()).then("").otherwise("").getClass());
//        System.out.println(Bindings.when(new SimpleBooleanProperty()).then(new SimpleStringProperty()).otherwise(new SimpleStringProperty()).getClass());
//        System.out.println(Bindings.selectString(nodeControllerProperty(), "instructions").getClass());
//        System.out.println(instructionsProperty().getClass());
//        System.out.println(completedProperty().getClass());
//        System.out.println(requiredProperty().getClass());

        borderPane.centerProperty().bind(node);
        next.disableProperty().bind(Bindings.and(requiredProperty(), Bindings.not(completedProperty())));
//        completedProperty().addListener((obs, oldVal, newVal) -> {});
//        Bindings.and(requiredProperty(), Bindings.not(completedProperty())).addListener((obs, oldVal, newVal) -> {
//            System.out.println("Disabled: " + oldVal + " -> " + newVal);
//        });
//        showBottomBar
        showBottomBar.addListener((obs, oldVal, newVal) -> {

            System.out.println("Show bottom bar is now: " + newVal);
        });
        System.out.println("Show bottom bar is now: " + showBottomBar.get());


        page.addListener((obs, oldVal, newVal) -> {

            System.out.println("Page is now: " + newVal);
            System.out.println(requiredProperty().getClass());
            System.out.println(completedProperty().getClass());
//            System.out.println((ReadOnlyProperty) Bindings.selectBoolean(nodeControllerProperty(), "required").getClass());
        });
        System.out.println("Page is now: " + page.get());


    }

    public ObjectProperty<Page> page = new SimpleObjectProperty<>(new Page(null, new StageManager.UnorderedManaged() { }));

    public ObjectProperty<Page> pageProperty() {
        return page;
    }

    public Page getPage() {
        return page.get();
    }

    public void setPage(Page page) {
        System.out.println("Setting page to: " + page);
        this.page.set(page);
    }

    public ObjectBinding<Node> node = Bindings.createObjectBinding(() -> page.get().node(), page);

    public ObjectBinding<Node> nodeProperty() {
        return node;
    }

    public Node getNode() {
        return node.get();
    }

    public ObjectBinding<StageManager.Managed> nodeController = Bindings.createObjectBinding(() -> page.get().controller(), page);

    public ObjectBinding<StageManager.Managed> nodeControllerProperty() {
        return nodeController;
    }

    public StageManager.Managed getNodeController() {
        return nodeController.get();
    }

    public BooleanBinding showBottomBar = Bindings.selectBoolean(nodeController, "ordered");;

    public BooleanBinding showBottomBarProperty() {
        return showBottomBar;
    }

    public boolean getShowBottomBar() {
        return showBottomBar.get();
    }

    @Override
    public void next(ActionEvent event) throws Exception {
        if (getNodeController() instanceof StageManager.OrderedManaged controller) {
            controller.next(event);
        }
    }

    @Override
    public void prev(ActionEvent event) throws Exception {
        if (getNodeController() instanceof StageManager.OrderedManaged controller) {
            controller.prev(event);
        }
    }

    public ObservableBooleanValue required = Bindings.when(Bindings.selectBoolean(nodeControllerProperty(), "ordered"))
                .then(Bindings.selectBoolean(nodeControllerProperty(), "required"))
//            .then(Bindings.createBooleanBinding(() -> getNodeController().isRequired(), nodeControllerProperty()))
            .otherwise(false);

    @Override
    public boolean isRequired() {
        return required.get();
    }

    @Override
    public ObservableBooleanValue requiredProperty() {
        return required;
    }

    public ObservableBooleanValue completed = Objects.requireNonNull(Bindings.when(Bindings.selectBoolean(nodeControllerProperty(), "ordered"))
//            .then(Bindings.selectBoolean(nodeControllerProperty(), "completed"))
                .then(Bindings.createBooleanBinding(() -> getNodeController().isCompleted(), nodeControllerProperty()))
            .otherwise(false), "wtf");

    @Override
    public boolean isCompleted() {
        return completed.get();
    }

    @Override
    public ObservableBooleanValue completedProperty() {
        return completed;
    }

    public StringBinding nextText =
            Bindings.when(Bindings.and(Bindings.not(requiredProperty()), Bindings.not(Objects.requireNonNull(completedProperty(), "foo null error"))))
                    .then("Skip")
                    .otherwise("Next");

    public StringBinding nextTextProperty() {
        return nextText;
    }

    public String getNextText() {
        return nextText.get();
    }

    public StringBinding instructions = Bindings.when(Bindings.selectBoolean(nodeControllerProperty(), "ordered"))
//            .then(Bindings.selectString(nodeControllerProperty(), "instructions"))
                .then(Bindings.createStringBinding(() -> getNodeController().getInstructions(),  nodeControllerProperty()))
            .otherwise("");

    @Override
    public String getInstructions() {
        return instructions.get();
    }

    @Override
    public StringBinding instructionsProperty() {
        return instructions;
    }
}
