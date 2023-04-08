package dev.liambloom.nhs.inductionStage;

import java.util.Comparator;

public enum SeatingGroup {
    IncumbentOfficers(Member.officerPositionComparator),
    NewSophomores(Member.alphabeticalComparator),
    NewJuniors(Member.alphabeticalComparator),
    NewSeniors(Member.alphabeticalComparator),
    OfficersElect(Member.officerPositionComparator),
    AwardWinners(Member.awardComparator),
    ReturningSeniors(Member.alphabeticalComparator),
    ReturningJuniors(Member.alphabeticalComparator);


    private final Comparator<Member> comparator;

    SeatingGroup(Comparator<Member> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Member> comparator() {
        return this.comparator;
    }
}
