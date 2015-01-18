package net.capps.word.util;

import java.util.concurrent.TimeUnit;

/**
 * Created by charlescapps on 1/17/15.
 */
public class DurationUtil {
    public static String getDurationPretty(long durationMs) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;

        StringBuilder sb = new StringBuilder();
        if (minutes > 0) {
            sb.append(minutes + " minutes, ");
        }

        sb.append(seconds + " seconds");
        return sb.toString();
    }

    public static String getDurationPrettyMillis(long durationMs) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;
        long millis = durationMs % 1000;

        StringBuilder sb = new StringBuilder();
        if (minutes > 0) {
            sb.append(minutes + " minutes, ");
        }

        if (seconds > 0) {
            sb.append(seconds + " seconds, ");
        }

        sb.append(millis + " milliseconds");
        return sb.toString();
    }
}
