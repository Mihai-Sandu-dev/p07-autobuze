package ro.autobuze.gtfs;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conversia valorilor din feed spre formatul din DB. */
final class GtfsValues {

    // GTFS: H:MM:SS sau HH:MM:SS; ora poate trece de 24 la cursele de după miezul nopții
    private static final Pattern TIME = Pattern.compile("(\\d{1,2}):([0-5]\\d):([0-5]\\d)");
    private static final DateTimeFormatter GTFS_DATE =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

    private GtfsValues() {
    }

    /** {@code HH:MM:SS} → secunde de la începutul zilei de serviciu (D-043); gol → null. */
    static Integer seconds(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        Matcher m = TIME.matcher(time.strip());
        if (!m.matches()) {
            throw new IllegalArgumentException("oră greșită: " + time);
        }
        return Integer.parseInt(m.group(1)) * 3600 + Integer.parseInt(m.group(2)) * 60
                + Integer.parseInt(m.group(3));
    }

    /** {@code YYYYMMDD} → {@code YYYY-MM-DD}; gol → null. */
    static String date(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date.strip(), GTFS_DATE).toString();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("dată greșită: " + date);
        }
    }
}
