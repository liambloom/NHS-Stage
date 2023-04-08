package dev.liambloom.nhs.inductionStage;

import java.util.Comparator;
import java.util.Optional;

public record Member(String firstName, String lastName, Grade grade, boolean isReturning,
                     Optional<OfficerPosition> officerPosition, boolean isOfficerElect, Optional<Award> award) {
    public static final Comparator<Member> alphabeticalComparator = Comparator
            .comparing(Member::lastName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Member::firstName, String.CASE_INSENSITIVE_ORDER);
    public static final Comparator<Member> officerPositionComparator = Comparator
            .comparing(m -> m.officerPosition().orElseThrow(), Comparator.naturalOrder());
    public static final Comparator<Member> awardComparator = Comparator
            .comparing(m -> m.award().orElseThrow(), Comparator.naturalOrder());

    @Override
    public String toString() {
        return firstName() + " " + lastName();
    }
}
