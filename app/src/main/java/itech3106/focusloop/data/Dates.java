package itech3106.focusloop.data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * One place for every date and time conversion in A2.
 *
 * <p>A "day" is a local epoch day (days since 1970-01-01 in the device time zone).
 * Storing the epoch day instead of a formatted string means the history can be grouped,
 * sorted and totalled correctly after midnight, a time-zone change or a locale change.
 */
public final class Dates {

    private static final DateTimeFormatter DAY_LABEL =
            DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_LABEL =
            DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter LONG_DATE =
            DateTimeFormatter.ofPattern("EEEE · d MMMM", Locale.ENGLISH);

    private Dates() {
    }

    public static ZoneId zone() {
        return ZoneId.systemDefault();
    }

    public static int today() {
        return (int) LocalDate.now(zone()).toEpochDay();
    }

    public static int epochDay(long millis) {
        return (int) Instant.ofEpochMilli(millis).atZone(zone()).toLocalDate().toEpochDay();
    }

    public static long startOfDay(int epochDay) {
        return LocalDate.ofEpochDay(epochDay).atStartOfDay(zone()).toInstant().toEpochMilli();
    }

    public static long at(int epochDay, int hour, int minute) {
        LocalDateTime time = LocalDate.ofEpochDay(epochDay).atTime(hour, minute);
        return time.atZone(zone()).toInstant().toEpochMilli();
    }

    /** "22 NOV" for a session group header. */
    public static String dayLabel(long millis) {
        return DAY_LABEL.format(Instant.ofEpochMilli(millis).atZone(zone()))
                .toUpperCase(Locale.ENGLISH);
    }

    public static String dayLabel(int epochDay) {
        return DAY_LABEL.format(LocalDate.ofEpochDay(epochDay))
                .toUpperCase(Locale.ENGLISH);
    }

    /** "09:12" for a session row. */
    public static String timeLabel(long millis) {
        return TIME_LABEL.format(Instant.ofEpochMilli(millis).atZone(zone()));
    }

    /** "Sunday · 22 November" for the review and main headers. */
    public static String longDate(int epochDay) {
        return LONG_DATE.format(LocalDate.ofEpochDay(epochDay));
    }

    public static boolean isToday(int epochDay) {
        return epochDay == today();
    }
}
