package ro.autobuze.schedule;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Plecările dintr-o stație într-un interval (twist 2). O oră din feed se adună la miezul nopții
 * zilei de serviciu (D-043, D-047): 24:40:00 în ziua de serviciu 25.11 pleacă pe 26.11 la 00:40.
 * De aceea intervalul se caută în toate zilele de serviciu care îl ating, începând cu ziua de
 * dinaintea lui {@code from}.
 */
@Repository
public class Departures {

    private static final String SQL = """
            select st.stop_id, st.stop_sequence, st.departure_sec, st.trip_id,
                   r.route_short_name, t.direction_id, t.trip_headsign
            from stop_times st
            join trips t on t.feed_version_id = st.feed_version_id and t.trip_id = st.trip_id
            join routes r on r.feed_version_id = t.feed_version_id and r.route_id = t.route_id
            where st.feed_version_id = :fv
              and st.stop_id in (:stops)
              and st.departure_sec >= :from and st.departure_sec < :until
              and t.service_id in (:services)""";

    private final NamedParameterJdbcTemplate jdbc;
    private final ServiceCalendar calendar;

    public Departures(NamedParameterJdbcTemplate jdbc, ServiceCalendar calendar) {
        this.jdbc = jdbc;
        this.calendar = calendar;
    }

    /**
     * Plecările din {@code stopIds} cu ora reală în {@code [from, until)}, în ordinea orei.
     * Presupune ore din feed sub 48:00:00, deci cel mult o zi de serviciu înapoi (Tursib: maxim 24:21:10).
     */
    public List<Departure> between(long feedVersionId, Collection<String> stopIds, LocalDateTime from,
            LocalDateTime until) {
        List<Departure> result = new ArrayList<>();
        if (stopIds.isEmpty() || !from.isBefore(until)) {
            return result;
        }
        for (LocalDate day = from.toLocalDate().minusDays(1); !day.isAfter(until.toLocalDate()); day = day.plusDays(1)) {
            Set<String> services = calendar.activeServices(feedVersionId, day);
            if (services.isEmpty()) {
                continue;
            }
            LocalDate serviceDate = day;
            LocalDateTime midnight = serviceDate.atStartOfDay();
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("fv", feedVersionId)
                    .addValue("stops", stopIds)
                    .addValue("services", services)
                    // fereastra, în secunde de la miezul nopții zilei de serviciu
                    .addValue("from", Duration.between(midnight, from).toSeconds())
                    .addValue("until", Duration.between(midnight, until).toSeconds());
            result.addAll(jdbc.query(SQL, params, (rs, n) -> new Departure(
                    serviceDate,
                    midnight.plusSeconds(rs.getInt("departure_sec")),
                    rs.getString("stop_id"),
                    rs.getInt("stop_sequence"),
                    rs.getString("trip_id"),
                    rs.getString("route_short_name"),
                    rs.getObject("direction_id", Integer.class),
                    rs.getString("trip_headsign"))));
        }
        result.sort(Comparator.comparing(Departure::time).thenComparing(Departure::stopId));
        return result;
    }
}
