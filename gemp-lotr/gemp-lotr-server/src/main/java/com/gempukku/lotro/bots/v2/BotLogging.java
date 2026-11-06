package com.gempukku.lotro.bots.v2;

public class BotLogging {

    public enum LogLevel {
        NONE(0),
        ERROR(1),
        INFO(2),
        DEBUG(3),
        REASONING(4);

        private final int level;

        LogLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    private static int logLevel = 0;

    public static void setLogLevel(LogLevel level) {
        logLevel = level.getLevel();
    }

    public static void log (LogLevel level, String message) {
        log(level, message, false);
    }

    public static void log(LogLevel level, String message, boolean printSeparator) {
        log(level.getLevel(), message, printSeparator);
    }

    private static void log(int level, String message, boolean printSeparator) {
        if (level <= logLevel) {
            if (printSeparator)
                System.out.println("----------");
            System.out.println(message);
        }
    }
}
