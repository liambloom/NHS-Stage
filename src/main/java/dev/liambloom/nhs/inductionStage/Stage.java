package dev.liambloom.nhs.inductionStage;

import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class Stage {
    private final Member[][] stageLeft;
    private final Member[][] stageRight;
    private final Member[] incumbentOfficers;

    private final int rowCount;

    private final String[] vipTable = { "Mayor", "Superintendent", "Principal", "Advisor" };

    private final int memberCount;

    public Stage(Member[] members, int rowCount, SeatingGroup[] stageLeftGroups, SeatingGroup[] stageRightGroups) {
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

        r.addAll(Arrays.asList(incumbentOfficers));

        Stream.of(stageLeft, stageRight)
                .flatMap(Stream::of)
                .flatMap(Stream::of)
                .filter(Objects::nonNull)
                .forEachOrdered(r::add);

        return r;
    }

    public String[][] getLayout() {
        int rowLength = Math.max(stageLeft.length, 4) + Math.max(stageRight.length, 4) + 1;

        String[][] layout = new String[rowCount + 2][rowLength];

        System.arraycopy(vipTable, 0, layout[0], stageLeft.length - vipTable.length, vipTable.length);
        System.arraycopy(Arrays.stream(incumbentOfficers).map(Member::toString).toArray(String[]::new), 0, layout[0],
                stageLeft.length + 1, incumbentOfficers.length);

        for (int i = 0; i < rowCount; i++) {
            String[] row = layout[i + 2];
            for (int j = 0; j < stageLeft.length; j++) {
                row[j] = Objects.toString(stageLeft[j][i], null);
            }
            for (int j = 0; j < stageRight.length; j++) {
                row[j + stageLeft.length + 1] = Objects.toString(stageRight[j][i], null);
            }
        }

        return layout;
    }

    public void saveLayout(CSVPrinter writer) throws IOException {
        for (String[] row : getLayout()) {
            writer.printRecord((Object[]) row);
        }
        writer.close();
    }

    private SeatingGroup getSeatingGroup (Member member) {
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
