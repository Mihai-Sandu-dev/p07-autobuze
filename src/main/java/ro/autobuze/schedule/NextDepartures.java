package ro.autobuze.schedule;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Următoarele plecări dintr-o stație (punctul 8). Fereastra ține de acum până la sfârșitul zilei de
 * mâine, după calendarul fiecărei zile (D-018); cursele de după miezul nopții ale zilei de serviciu
 * anterioare intră prin {@link Departures}. Se scot cursele care se termină în stație (D-048), iar
 * grupurile sunt pe linie + capăt (D-051), cu primele 3 plecări.
 */
@Service
public class NextDepartures {

    static final int PER_GROUP = 3;
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    /** Ultimul stop_sequence al fiecărei curse din listă: acolo cursa se termină (D-048). */
    private static final String LAST_STOPS = """
            select trip_id, max(stop_sequence) as last_sequence
            from stop_times
            where feed_version_id = :fv and trip_id in (:trips)
            group by trip_id""";

    private static final String COLORS = """
            select route_short_name, route_color, route_text_color
            from routes
            where feed_version_id = :fv""";

    private final ServiceCalendar calendar;
    private final Stations stations;
    private final Departures departures;
    private final NamedParameterJdbcTemplate jdbc;

    public NextDepartures(ServiceCalendar calendar, Stations stations, Departures departures,
            NamedParameterJdbcTemplate jdbc) {
        this.calendar = calendar;
        this.stations = stations;
        this.departures = departures;
        this.jdbc = jdbc;
    }

    /**
     * Lista stației lui {@code stopId} la ora {@code now} (ora de pe ceas, D-047). Gol dacă rândul nu
     * există în versiunea de feed activă; fără versiune activă, o listă goală cu {@code feedImported} fals.
     */
    public Optional<StationBoard> at(String stopId, LocalDateTime now) {
        String dayLabel = calendar.dayLabel(now.toLocalDate());
        Optional<Long> active = calendar.activeFeedVersion();
        if (active.isEmpty()) {
            return Optional.of(new StationBoard(null, now, dayLabel, false, List.of()));
        }
        long fv = active.get();
        Optional<StationBoard.Station> station = stations.of(fv, stopId);
        if (station.isEmpty()) {
            return Optional.empty();
        }
        LocalDateTime until = now.toLocalDate().plusDays(2).atStartOfDay();
        List<Departure> found = withoutTripEnds(fv, departures.between(fv, station.get().stopIds(), now, until));
        return Optional.of(new StationBoard(station.get(), now, dayLabel, true, groups(found, colors(fv), now)));
    }

    private List<Departure> withoutTripEnds(long fv, List<Departure> found) {
        if (found.isEmpty()) {
            return found;
        }
        Set<String> trips = found.stream().map(Departure::tripId).collect(Collectors.toSet());
        Map<String, Integer> last = new HashMap<>();
        jdbc.query(LAST_STOPS, new MapSqlParameterSource().addValue("fv", fv).addValue("trips", trips),
                rs -> {
                    last.put(rs.getString("trip_id"), rs.getInt("last_sequence"));
                });
        return found.stream().filter(d -> !Integer.valueOf(d.stopSequence()).equals(last.get(d.tripId()))).toList();
    }

    private Map<String, LineColors> colors(long fv) {
        Map<String, LineColors> colors = new HashMap<>();
        jdbc.query(COLORS, new MapSqlParameterSource("fv", fv), rs -> {
            colors.putIfAbsent(rs.getString("route_short_name"),
                    LineColors.of(rs.getString("route_color"), rs.getString("route_text_color")));
        });
        return colors;
    }

    /**
     * Grupurile pe linie + capăt, cu primele 3 plecări, ordonate după prima plecare.
     * {@code departures} vine în ordinea orei ({@link Departures#between}).
     */
    static List<StationBoard.Group> groups(List<Departure> departures, Map<String, LineColors> colors,
            LocalDateTime now) {
        Map<List<String>, List<Departure>> byGroup = new LinkedHashMap<>();
        for (Departure d : departures) {
            List<String> key = List.of(Objects.toString(d.routeShortName(), ""), Objects.toString(d.headsign(), ""));
            List<Departure> group = byGroup.computeIfAbsent(key, k -> new ArrayList<>());
            if (group.size() < PER_GROUP) {
                group.add(d);
            }
        }
        LocalDate tomorrow = now.toLocalDate().plusDays(1);
        List<StationBoard.Group> result = new ArrayList<>();
        byGroup.forEach((key, group) -> {
            LineColors c = colors.getOrDefault(key.get(0), LineColors.of(null, null));
            result.add(new StationBoard.Group(key.get(0), key.get(1), c.background(), c.text(),
                    group.stream().map(d -> new StationBoard.Entry(d.time().format(HH_MM), d.time(),
                            d.time().toLocalDate().equals(tomorrow),
                            (int) Duration.between(now, d.time()).toMinutes())).toList()));
        });
        // LinkedHashMap păstrează ordinea primei plecări a fiecărui grup
        return result;
    }
}
