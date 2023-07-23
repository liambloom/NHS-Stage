package dev.liambloom.nhs.inductionStage;

import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class Stage {
    private final Member[][] stageLeft;
    private final Member[][] stageRight;
    private final Member[] incumbentOfficers;

    private final int rowCount;

    private final String[] vipTable;

    private final int memberCount;

    public Stage(Member[] members, int rowCount, String[] vipTable, SeatingGroup[] stageLeftGroups, SeatingGroup[] stageRightGroups) {
        this.memberCount = members.length;
        this.rowCount = rowCount;
        this.vipTable = vipTable;

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

    public List<Member> getHallwayLineup() {
        List<Member> r = new ArrayList<>(memberCount);

        r.addAll(Arrays.asList(incumbentOfficers));

        Stream.of(stageLeft, stageRight)
                .flatMap(Stream::of)
                .flatMap(Stream::of)
                .filter(Objects::nonNull)
                .forEachOrdered(r::add);

        return r;
    }

    public List<Member> getNewMemberCallupOrder() {
        List<Member> r = new ArrayList<>(memberCount);

        Stream.of(stageLeft, stageRight)
                .flatMap(Stream::of)
                .flatMap(members -> {
                    Stream.Builder<Member> builder = Stream.builder();
                    for (int i = members.length - 1; i >= 0; i--) {
                        builder.add(members[i]);
                    }
                    return builder.build();
                })
                .filter(Objects::nonNull)
                .forEachOrdered(r::add);

        return r;
    }

    public List<Member> getSeniorCallupOrder() {
        List<Member> r = new ArrayList<>(memberCount);

        r.addAll(Arrays.asList(incumbentOfficers).subList(0, incumbentOfficers.length - 1));

        Stream.Builder<Member[]> stageRightReverseBuilder = Stream.builder();
        for (int i = stageRight.length - 1; i >= 0; i--) {
            stageRightReverseBuilder.add(stageRight[i]);
        }

        Stream.concat(Stream.of(stageLeft), stageRightReverseBuilder.build())
                .flatMap(members -> {
                    Stream.Builder<Member> builder = Stream.builder();
                    for (int i = members.length - 1; i >= 0; i--) {
                        builder.add(members[i]);
                    }
                    return builder.build();
                })
                .filter(Objects::nonNull)
                .forEachOrdered(r::add);

        r.add(incumbentOfficers[incumbentOfficers.length - 1]);

        return r;
    }

    public String[][] getLayout() {
        int leftColWidth = Math.max(stageLeft.length, vipTable.length);
        int rowLength = leftColWidth + Math.max(stageRight.length, incumbentOfficers.length) + 1;

        String[][] layout = new String[rowCount + 2][rowLength];

        System.arraycopy(vipTable, 0, layout[0], Math.max(stageLeft.length - vipTable.length, 0), vipTable.length);
        System.arraycopy(Arrays.stream(incumbentOfficers).map(Member::toString).toArray(String[]::new), 0, layout[0],
                leftColWidth + 1, incumbentOfficers.length);

        for (int i = 0; i < rowCount; i++) {
            String[] row = layout[i + 2];
            for (int j = 0; j < stageLeft.length; j++) {
                row[j] = Objects.toString(stageLeft[j][i], null);
            }
            for (int j = 0; j < stageRight.length; j++) {
                row[j + leftColWidth + 1] = Objects.toString(stageRight[j][i], null);
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
