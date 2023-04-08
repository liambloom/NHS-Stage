package dev.liambloom.nhs.inductionStage;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataLoader {
    public static List<Member> loadData(CSVParser parser, ColumnNumbers columnNumbers, Map<Integer, OfficerPosition> incumbent,
                                    Map<Integer, OfficerPosition> elect, Map<Integer, Award> awards) {
        List<Member> members = new ArrayList<>();
        int i = 0;
        for (CSVRecord r : parser) {
            AtomicBoolean isOfficerElect = new AtomicBoolean(false);
            int finalI = i;
            Optional<OfficerPosition> officerPosition = Optional.ofNullable(incumbent.get(i))
                    .or(() -> {
                        Optional<OfficerPosition> pos = Optional.ofNullable(elect.get(finalI));
                        isOfficerElect.set(pos.isPresent());
                        return pos;
                    });
            Optional<Award> award = Optional.ofNullable(awards.get(i));
            members.add(new Member(r.get(columnNumbers.firstName()), r.get(columnNumbers.lastName()),
                    Grade.parse(r.get(columnNumbers.grade())), booleanParser(r.get(columnNumbers.isReturning())),
                    officerPosition, isOfficerElect.get(), award));
            i++;
        }

        return members;
    }

    private static boolean booleanParser(String s) {
        return switch (s.toLowerCase()) {
            case "true", "t", "yes", "y" -> true;
            case "false", "f", "no", "n" -> false;
            default -> throw new IllegalArgumentException("\"" + s + "\" is not a valid boolean");
        };
    }
}
