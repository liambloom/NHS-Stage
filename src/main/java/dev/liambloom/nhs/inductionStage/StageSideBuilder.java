package dev.liambloom.nhs.inductionStage;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class StageSideBuilder {
    private final int rowCount;
    private final int colCount;
    private final int memberCount;
    private final boolean rightToLeft;
    private final Map<SeatingGroup, ? extends SortedSet<Member>> seatingGroups;
    private final SeatingGroup[] groupOrder;
    private final Member[][] stage;
    private boolean done = false;

    public StageSideBuilder(int rowCount, Map<SeatingGroup, ? extends SortedSet<Member>> seatingGroups, SeatingGroup[] groupOrder,
                            boolean rightToLeft) {
        this.rowCount = rowCount;
        this.seatingGroups = seatingGroups;
        this.groupOrder = groupOrder;
        this.rightToLeft = rightToLeft;

        // Counts up the number of members on the left side of the stage to make the stage left 2D array
        int memberCount = 0;
        for (SeatingGroup group : groupOrder) {
            memberCount += seatingGroups.get(group).size();
        }
        this.memberCount = memberCount;
        this.colCount = (int) Math.ceil(memberCount * 1.0 / rowCount);

        stage = new Member[colCount][rowCount];
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColCount() {
        return colCount;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public Member[][] build() {
        if (done) {
            throw new IllegalStateException("Cannot call StageSideBuilder.build() twice on the same instance");
        }
        else {
            done = true;
        }

        Stream.Builder<SeatingGroup> iterBuilder = Stream.builder();

        for (int i = groupOrder.length - 1; i >= 0; i--) {
            iterBuilder.add(groupOrder[i]);
        }

        Iterator<Member> iter = iterBuilder.build()
                .map(seatingGroups::get)
                .flatMap(Set::stream)
                .iterator();

        int i = rightToLeft ? colCount - 1 : 0;
        int j = rowCount - (memberCount - 1) % rowCount - 1;

        while (iter.hasNext()) {

            stage[i][j++] = iter.next();
            System.out.printf("stage[%d][%d] = %s%n", i, j - 1, stage[i][j - 1]);
            if (j >= rowCount) {
                j = 0;
                if (rightToLeft) {
                    i--;
                }
                else {
                    i++;
                }
            }
        }

        return stage;
    }
}
