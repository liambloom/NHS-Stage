module dev.liambloom.nhs.inductionStage {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires org.apache.commons.csv;

    opens dev.liambloom.nhs.inductionStage.gui;// to javafx.fxml;
    opens views;
}