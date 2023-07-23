package dev.liambloom.nhs.inductionStage;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataLoader {
    public static List<Member> loadData(Iterable<CSVRecord> records, int headerRows, ColumnNumbers columnNumbers,
                                        Map<Integer, OfficerPosition> incumbent, Map<Integer, OfficerPosition> elect,
                                        Map<Integer, Award> awards) {
        List<Member> members = new ArrayList<>();
        boolean[] incumbentOfficers = new boolean[OfficerPosition.values().length];
        boolean[] officersElect = new boolean[OfficerPosition.values().length];
        boolean[] awardWinners = new boolean[Award.values().length];
        int i = -headerRows;
        for (CSVRecord r : records) {
            if (i < 0) {
                i++;
                continue;
            }

            AtomicBoolean isOfficerElect = new AtomicBoolean(false);
            int finalI = i;
            Optional<OfficerPosition> officerPosition = Optional.ofNullable(incumbent.get(i))
                    .or(() -> {
                        Optional<OfficerPosition> pos = Optional.ofNullable(elect.get(finalI));
                        isOfficerElect.set(pos.isPresent());
                        return pos;
                    });
            Optional<Award> award = Optional.ofNullable(awards.get(i));

            officerPosition.ifPresent(pos ->
                    (isOfficerElect.get() ? officersElect : incumbentOfficers)[pos.ordinal()] = true);
            award.ifPresent(value -> awardWinners[value.ordinal()] = true);

            members.add(new Member(r.get(columnNumbers.firstName()), r.get(columnNumbers.lastName()),
                    Grade.parse(r.get(columnNumbers.grade()).trim()),
                    booleanParser(r.get(columnNumbers.isReturning()).trim()),
                    officerPosition, isOfficerElect.get(), award));
            i++;
        }

        for (OfficerPosition position : OfficerPosition.values()) {
            if (!incumbentOfficers[position.ordinal()]) {
                members.add(new Member("Incumbent", position.name(), Grade.Senior, false,
                        Optional.of(position), false, Optional.empty()));
            }
            if (!officersElect[position.ordinal()]) {
                members.add(new Member(position.name(), "Elect", Grade.Junior, false,
                        Optional.of(position), true, Optional.empty()));
            }
        }

        // TODO: This doesn't work
        for (Award award : Award.values()) {

            if (!awardWinners[award.ordinal()]) {
                members.add(new Member(award.name(), "Award Winner", Grade.Senior, false,
                        Optional.empty(), false, Optional.of(award)));
            }
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
