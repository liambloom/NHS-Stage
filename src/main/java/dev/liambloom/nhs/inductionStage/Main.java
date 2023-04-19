package dev.liambloom.nhs.inductionStage;

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
                Map.of(),
                Map.of(
                        1, Award.Service,
                        34, Award.Character,
                        12, Award.Scholarship,
                        38, Award.Leadership
                ));

        for (Member member : members)
            System.out.printf("%-20s | %-20s | %-10s | %b | %-13s | %b | %-10s%n", member.firstName(), member.lastName(),
                    member.grade(), member.isReturning(), member.officerPosition().map(Object::toString).orElse(""), member.isOfficerElect(),
                    member.award().map(Object::toString).orElse(""));

        Stage stage = new Stage(members.toArray(new Member[0]), 6);
        System.out.println(stage.getLineup());
        Path out = Path.of("stage.csv");
        if (!Files.exists(out)) {
            Files.createFile(out);
        }
        stage.saveLayout(new CSVPrinter(Files.newBufferedWriter(out), CSVFormat.DEFAULT));
    }
}