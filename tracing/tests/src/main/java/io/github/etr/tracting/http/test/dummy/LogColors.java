package io.github.etr.tracting.http.test.dummy;

public class LogColors {

    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";

    public static String cyan(String original) {
        return CYAN + original + RESET;
    }

    public static String green(String original) {
        return GREEN + original + RESET;
    }

    public static String yellow(String original) {
        return YELLOW + original + RESET;
    }

    private LogColors() {
    }
}
