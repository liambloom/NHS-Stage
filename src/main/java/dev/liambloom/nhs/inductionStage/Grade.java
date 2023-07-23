package dev.liambloom.nhs.inductionStage;

public enum Grade {
    Sophomore,
    Junior,
    Senior;

    private static final String parseError = "Grade cannot be \"%s\". Acceptable values for" +
            "grade are: sophomore, junior, senior, 10, 11, 12";

    public static Grade parse(String s) {
        if (s.isEmpty()) {
            throw new IllegalArgumentException(String.format(parseError, s));
        }

        try {
            return Grade.valueOf(Character.toUpperCase(s.charAt(0)) + s.toLowerCase().substring(1));
        }
        catch (IllegalArgumentException e) {
            try {
                int n = Integer.parseInt(s);
                return switch (n) {
                    case 10 -> Sophomore;
                    case 11 -> Junior;
                    case 12 -> Senior;
                    default -> throw new IllegalArgumentException(String.format(parseError, n));
                };
            }
            catch (NumberFormatException e2) {
                throw new IllegalArgumentException(String.format(parseError, s));
            }
        }
    }
}
