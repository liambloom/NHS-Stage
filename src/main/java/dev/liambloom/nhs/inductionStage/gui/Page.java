package dev.liambloom.nhs.inductionStage.gui;

import javafx.beans.binding.ObjectBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.io.IOException;

public record Page(Parent node, StageManager.Managed controller) {
    public Page(FXMLLoader loader) throws IOException {
        this(loader.load(), loader.getController());
    }
}
