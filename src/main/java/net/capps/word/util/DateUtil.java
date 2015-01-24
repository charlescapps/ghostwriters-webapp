package net.capps.word.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by charlescapps on 1/17/15.
 */
public class DateUtil {
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("M/dd/yy hh:mm:ss z");

    public static String format(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static String format(Timestamp timestamp) {
        Date date = new Date(timestamp.getTime());
        return DATE_FORMAT.format(date);
    }


    public static String getDurationPretty(long durationMs) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;

        StringBuilder sb = new StringBuilder();
        if (minutes > 0) {
            sb.append(minutes).append(" minutes, ");
        }

        sb.append(seconds).append(" seconds");
        return sb.toString();
    }

    public static String getDurationPrettyMillis(long durationMs) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;
        long millis = durationMs % 1000;

        StringBuilder sb = new StringBuilder();
        if (minutes > 0) {
            sb.append(minutes).append(" minutes, ");
        }

        if (seconds > 0) {
            sb.append(seconds).append(" seconds, ");
        }

        sb.append(millis).append(" milliseconds");
        return sb.toString();
    }
}
