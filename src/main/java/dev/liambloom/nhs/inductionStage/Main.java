package dev.liambloom.nhs.inductionStage;

import dev.liambloom.nhs.inductionStage.gui.StageOrderController;
import javafx.application.Application;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        List<Member> members = DataLoader.loadData(CSVParser.parse(Path.of("members.csv"), Charset.defaultCharset(), CSVFormat.DEFAULT), 3,
                new ColumnNumbers(0, 1, 3, 5),
                Map.of(
                        6, OfficerPosition.Secretary,
                        10, OfficerPosition.President,
                        13, OfficerPosition.Treasurer,
                        14, OfficerPosition.VicePresident
                ),
                Map.of(
                        42, OfficerPosition.President,
                        43, OfficerPosition.VicePresident,
                        48, OfficerPosition.Secretary,
                        51, OfficerPosition.Treasurer
                ),
                Map.of(
                        1, Award.Service,
                        34, Award.Character,
                        12, Award.Scholarship,
                        38, Award.Leadership
                ));

        Stage stage = new Stage(members.toArray(new Member[0]), 6,  new String[]{ "Mayor", "Superintendent",
                "Principal", "Advisor" }, new SeatingGroup[]{ SeatingGroup.OfficersElect, SeatingGroup.NewSeniors,
                SeatingGroup.NewJuniors, SeatingGroup.NewSophomores }, new SeatingGroup[]{ SeatingGroup.AwardWinners,
                SeatingGroup.ReturningSeniors, SeatingGroup.ReturningJuniors });

        Path out = Path.of("stage.csv");
        if (!Files.exists(out)) {
            Files.createFile(out);
        }
        stage.saveLayout(new CSVPrinter(Files.newBufferedWriter(out), CSVFormat.DEFAULT));
    }
}