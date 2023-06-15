package dev.liambloom.nhs.inductionStage.gui;

public enum LineupFormatOptions {
    CommaSeperated, SeparateLines, BulletedList, NumberedList;

    public static LineupFormatOptions fromString(String s) {
        return switch (s) {
            case "Comma Seperated" -> CommaSeperated;
            case "Separate Lines" -> SeparateLines;
            case "Bulleted List" -> BulletedList;
            case "Numbered List" -> NumberedList;
            default -> valueOf(s);
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case CommaSeperated -> "Comma Seperated";
            case SeparateLines -> "Separate Lines";
            case BulletedList -> "Bulleted List";
            case NumberedList -> "Numbered List";
        };
    }
}
