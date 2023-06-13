package dev.liambloom.nhs.inductionStage.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public record Page(Parent node, StageManager.Managed controller) {
    public Page(FXMLLoader loader) throws IOException {
        this(loader.load(), loader.getController());
    }

    public Page(String name) throws IOException {
        this(new FXMLLoader(Page.class.getResource("/views/" + name + ".fxml")));
    }
}
