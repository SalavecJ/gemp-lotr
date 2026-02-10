package com.gempukku.lotro.bots.forge.utils;

public class BotLogging {

    private static int logLevel = 0;

    public static void setLogLevel(int level) {
        logLevel = level;
    }

    public static void log (int level, String message) {
        log(level, message, false);
    }

    public static void log(int level, String message, boolean printSeparator) {
        if (level <= logLevel) {
            if (printSeparator)
                System.out.println("----------");
            System.out.println(message);
        }
    }
}
