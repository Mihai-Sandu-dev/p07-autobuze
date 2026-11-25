package ro.autobuze.schedule;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Calendarul feed-ului (twist 1): ce servicii circulă într-o zi, după calendar.txt și
 * calendar_dates.txt. Sărbătorile legale din DB nu schimbă serviciile; dau doar tipul de zi (D-015).
 */
@Repository
public class ServiceCalendar {

    private final JdbcTemplate jdbc;

    public ServiceCalendar(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Versiunea de feed activă (D-019), dacă s-a importat vreuna. */
    public Optional<Long> activeFeedVersion() {
        return jdbc.queryForList("select id from feed_versions where active", Long.class).stream().findFirst();
    }

    /**
     * Serviciile care circulă la {@code date}: cele din calendar valabile la data aceea și în ziua
     * aceea a săptămânii, plus cele adăugate în calendar_dates (tip 1), minus cele scoase (tip 2).
     */
    public Set<String> activeServices(long feedVersionId, LocalDate date) {
        // Numele coloanei vine din enum-ul DayOfWeek (monday ... sunday), nu din afară.
        String weekday = date.getDayOfWeek().name().toLowerCase();
        // UNION și EXCEPT se aplică de la stânga la dreapta: (calendar ∪ adăugate) − scoase
        String sql = "select service_id from calendar"
                + " where feed_version_id = ? and ? between start_date and end_date and " + weekday
                + " union select service_id from calendar_dates"
                + " where feed_version_id = ? and date = ? and exception_type = 1"
                + " except select service_id from calendar_dates"
                + " where feed_version_id = ? and date = ? and exception_type = 2"
                + " order by service_id";
        return new LinkedHashSet<>(jdbc.queryForList(sql, String.class,
                feedVersionId, date, feedVersionId, date, feedVersionId, date));
    }

    /** Tipul zilei (D-014), cu sărbătorile legale din {@code public_holidays} (D-015). */
    public DayType dayType(LocalDate date) {
        return DayType.of(date, holiday(date));
    }

    /**
     * Eticheta zilei din capul paginii de stație, de ex. „Miercuri, 25 noiembrie · zi lucrătoare”;
     * o sărbătoare legală apare ca „sărbătoare legală”, nu ca duminică.
     */
    public String dayLabel(LocalDate date) {
        String day = date.format(DAY);
        String kind = holiday(date) ? "sărbătoare legală" : switch (DayType.of(date, false)) {
            case LUCRATOARE -> "zi lucrătoare";
            case SAMBATA -> "sâmbătă";
            case DUMINICA_SARBATOARE -> "duminică";
        };
        return Character.toUpperCase(day.charAt(0)) + day.substring(1) + " · " + kind;
    }

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.of("ro"));

    private boolean holiday(LocalDate date) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "select exists (select 1 from public_holidays where date = ?)", Boolean.class, date));
    }
}
