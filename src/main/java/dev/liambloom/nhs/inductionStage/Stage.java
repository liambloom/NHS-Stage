package dev.liambloom.nhs.inductionStage;

import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Stream;

public class Stage {
    private final Member[][] stageLeft;
    private final Member[][] stageRight;
    private final Member[] incumbentOfficers;

    private final int rowCount;

    private final String[] leftTable = { "Mayor", "Superintendent", "Principal", "Advisor" };

    private final int memberCount;

    private static final SeatingGroup[] stageLeftGroups = { SeatingGroup.OfficersElect, SeatingGroup.ReturningSeniors,
            SeatingGroup.ReturningJuniors, SeatingGroup.NewJuniors, SeatingGroup.NewSophomores };
    private static final SeatingGroup[] stageRightGroups = { SeatingGroup.AwardWinners, SeatingGroup.NewSeniors };

    public Stage(Member[] members, int rowCount) {
        this.memberCount = members.length;
        this.rowCount = rowCount;

        // Create a map to sets of members, where each set is a seating group
        Map<SeatingGroup, NavigableSet<Member>> seatingGroups = new HashMap<>();

        // Adds the sets to the map
        for (SeatingGroup group : SeatingGroup.values()) {
            seatingGroups.put(group, new TreeSet<>(group.comparator().reversed()));
        }

        // Puts the members in the sets
        for (Member member : members) {
            seatingGroups.get(getSeatingGroup(member)).add(member);
        }

        StageSideBuilder leftSideBuilder = new StageSideBuilder(rowCount, seatingGroups, stageLeftGroups, false);
        stageLeft = leftSideBuilder.build();

        StageSideBuilder rightSideBuilder = new StageSideBuilder(rowCount, seatingGroups, stageRightGroups, true);
        stageRight = rightSideBuilder.build();

        incumbentOfficers = seatingGroups.get(SeatingGroup.IncumbentOfficers).descendingSet().toArray(new Member[0]);
    }

    public List<Member> getLineup() {
        List<Member> r = new ArrayList<>(memberCount);

        for (int i = 0; i < incumbentOfficers.length; i++) {
            r.add(incumbentOfficers[i]);
        }

        Stream.of(stageLeft, stageRight)
                .flatMap(Stream::of)
                .flatMap(Stream::of)
                .filter(Objects::nonNull)
                .forEachOrdered(r::add);

        return r;
    }

    public void saveLayout(CSVPrinter writer) throws IOException {
        int recordLength = Math.max(stageLeft.length, 4) + Math.max(stageRight.length, 4) + 1;

        Object[] raised = new Object[recordLength];
        System.arraycopy(leftTable, 0, raised, stageLeft.length - leftTable.length, leftTable.length);
        System.arraycopy(incumbentOfficers, 0, raised, stageLeft.length + 1, incumbentOfficers.length);
        writer.printRecord(raised);

        writer.printRecord(new Object[recordLength]);

        for (int i = 0; i < rowCount; i++) {
            Member[] row = new Member[recordLength];
            for (int j = 0; j < stageLeft.length; j++) {
                row[j] = stageLeft[j][i];
            }
            for (int j = 0; j < stageRight.length; j++) {
                row[j + stageLeft.length + 1] = stageRight[j][i];
            }
            writer.printRecord((Object[]) row);
        }

        writer.close();
    }

    public SeatingGroup getSeatingGroup (Member member) {
        if (member.officerPosition().isPresent()) {
            if (member.isOfficerElect()) {
                return SeatingGroup.OfficersElect;
            }
            else {
                return SeatingGroup.IncumbentOfficers;
            }
        }
        else if (member.award().isPresent()) {
            return SeatingGroup.AwardWinners;
        }
        else if (member.isReturning()) {
            if (member.grade().equals(Grade.Senior)) {
                return SeatingGroup.ReturningSeniors;
            }
            else {
                return SeatingGroup.ReturningJuniors;
            }
        }
        else {
            if (member.grade().equals(Grade.Senior)) {
                return SeatingGroup.NewSeniors;
            }
            else if (member.grade().equals(Grade.Junior)) {
                return SeatingGroup.NewJuniors;
            }
            else {
                return SeatingGroup.NewSophomores;
            }
        }
    }
}
