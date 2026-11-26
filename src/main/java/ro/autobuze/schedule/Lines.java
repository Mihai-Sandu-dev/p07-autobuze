package ro.autobuze.schedule;

import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Pagina de linie (D-060). Pentru fiecare sens (linie + capăt, D-051): traseul pe care merg cele mai multe
 * curse (la egalitate, cel cu mai multe stații, apoi cel mai mic {@code trip_id}), stațiile lui, shape-ul
 * (cel mai des {@code shape_id} al acestor curse, altfel linia dintre stații) și orarul: ora din prima stație
 * a fiecărei curse, în coloanele următoarei zile lucrătoare, sâmbete și duminici, după calendar (cu excepții).
 */
@Repository
public class Lines {

    private static final String ROUTES = """
            select route_id, route_long_name, route_color, route_text_color
            from routes where feed_version_id = :fv and route_short_name = :line order by route_id""";

    private static final String TRIPS = """
            select trip_id, coalesce(trip_headsign, '') as headsign, direction_id, shape_id, service_id
            from trips where feed_version_id = :fv and route_id in (:routes)""";

    private static final String STOP_TIMES = """
            select trip_id, stop_id, departure_sec
            from stop_times where feed_version_id = :fv and trip_id in (:trips)
            order by trip_id, stop_sequence""";

    private static final String STOPS = """
            select stop_id, stop_name, stop_lat, stop_lon from stops where feed_version_id = :fv and stop_id in (:ids)""";

    private static final String SHAPE = """
            select shape_pt_lat, shape_pt_lon from shapes
            where feed_version_id = :fv and shape_id = :shape order by shape_pt_sequence""";

    private static final Collator RO = Collator.getInstance(Locale.forLanguageTag("ro"));

    private record Trip(String id, String headsign, Integer direction, String shapeId, String service,
            List<String> pattern, int firstDeparture) {
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final ServiceCalendar calendar;

    public Lines(NamedParameterJdbcTemplate jdbc, ServiceCalendar calendar) {
        this.jdbc = jdbc;
        this.calendar = calendar;
    }

    /** Pagina liniei cu numărul {@code line}; gol dacă linia nu e în versiunea {@code feedVersionId}. */
    public Optional<LinePage> page(long feedVersionId, String line, LocalDate today) {
        MapSqlParameterSource p = new MapSqlParameterSource("fv", feedVersionId).addValue("line", line);
        List<Map<String, Object>> routes = jdbc.queryForList(ROUTES, p);
        if (routes.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> route = routes.get(0);
        List<Trip> trips = trips(p, routes.stream().map(r -> (String) r.get("route_id")).toList());

        List<LocalDate> dates = List.of(calendar.nextOf(DayType.LUCRATOARE, today),
                calendar.nextOf(DayType.SAMBATA, today), calendar.nextOf(DayType.DUMINICA_SARBATOARE, today));
        List<Set<String>> services = dates.stream().map(d -> calendar.activeServices(feedVersionId, d)).toList();
        List<LinePage.Day> days = List.of(new LinePage.Day("Lucrătoare", dates.get(0), true),
                new LinePage.Day("Sâmbătă", dates.get(1), false), new LinePage.Day("Duminică", dates.get(2), false));

        Map<String, List<Trip>> byHeadsign = trips.stream().collect(Collectors.groupingBy(Trip::headsign));
        Map<String, LinePage.Stop> stops = stops(p, trips);
        List<LinePage.Direction> directions = new ArrayList<>();
        for (List<Trip> group : byHeadsign.values()) {
            directions.add(direction(p, group, stops, days, services));
        }
        Map<String, Integer> directionOf = byHeadsign.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                e -> e.getValue().stream().map(Trip::direction).filter(d -> d != null).min(Integer::compare).orElse(0)));
        directions.sort(Comparator.<LinePage.Direction>comparingInt(d -> directionOf.get(d.headsign()))
                .thenComparing(LinePage.Direction::headsign, RO));

        return Optional.of(new LinePage(line, (String) route.get("route_long_name"),
                LineColors.of((String) route.get("route_color"), (String) route.get("route_text_color")), directions));
    }

    private List<Trip> trips(MapSqlParameterSource p, List<String> routeIds) {
        List<Map<String, Object>> rows = jdbc.queryForList(TRIPS, new MapSqlParameterSource(p.getValues())
                .addValue("routes", routeIds));
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<String, List<String>> patterns = new HashMap<>();
        Map<String, Integer> first = new HashMap<>();
        jdbc.query(STOP_TIMES, new MapSqlParameterSource(p.getValues())
                .addValue("trips", rows.stream().map(r -> r.get("trip_id")).toList()), rs -> {
                    String trip = rs.getString(1);
                    patterns.computeIfAbsent(trip, k -> new ArrayList<>()).add(rs.getString(2));
                    first.putIfAbsent(trip, rs.getInt(3));
                });
        return rows.stream().filter(r -> patterns.containsKey(r.get("trip_id"))).map(r -> new Trip(
                (String) r.get("trip_id"), (String) r.get("headsign"), (Integer) toInt(r.get("direction_id")),
                (String) r.get("shape_id"), (String) r.get("service_id"),
                patterns.get(r.get("trip_id")), first.get(r.get("trip_id")))).toList();
    }

    private static Integer toInt(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    private Map<String, LinePage.Stop> stops(MapSqlParameterSource p, List<Trip> trips) {
        Set<String> ids = trips.stream().flatMap(t -> t.pattern().stream()).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return jdbc.query(STOPS, new MapSqlParameterSource(p.getValues()).addValue("ids", ids),
                (rs, n) -> new LinePage.Stop(rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4)))
                .stream().collect(Collectors.toMap(LinePage.Stop::stopId, Function.identity()));
    }

    private LinePage.Direction direction(MapSqlParameterSource p, List<Trip> group, Map<String, LinePage.Stop> stops,
            List<LinePage.Day> days, List<Set<String>> services) {
        Map<List<String>, List<Trip>> byPattern = group.stream().collect(Collectors.groupingBy(Trip::pattern));
        List<String> main = byPattern.entrySet().stream()
                .min(Comparator.<Map.Entry<List<String>, List<Trip>>>comparingInt(e -> -e.getValue().size())
                        .thenComparingInt(e -> -e.getKey().size())
                        .thenComparing(e -> e.getValue().stream().map(Trip::id).min(String::compareTo).orElseThrow()))
                .orElseThrow().getKey();
        List<LinePage.Stop> route = main.stream().map(stops::get).toList();

        // Ore pe ziua de serviciu: 24:35 rămâne „24” la sortare și se scrie „00” (D-043, D-060)
        TreeMap<Integer, List<List<LinePage.Minute>>> hours = new TreeMap<>();
        for (int i = 0; i < days.size(); i++) {
            int day = i;
            group.stream().filter(t -> services.get(day).contains(t.service()))
                    .sorted(Comparator.comparingInt(Trip::firstDeparture))
                    .forEach(t -> {
                        List<List<LinePage.Minute>> row = hours.computeIfAbsent(t.firstDeparture() / 3600, h -> {
                            List<List<LinePage.Minute>> empty = new ArrayList<>();
                            days.forEach(d -> empty.add(new ArrayList<>()));
                            return empty;
                        });
                        row.get(day).add(new LinePage.Minute(String.format("%02d", t.firstDeparture() / 60 % 60),
                                !t.pattern().equals(main)));
                    });
        }
        List<LinePage.Hour> rows = hours.entrySet().stream()
                .map(e -> new LinePage.Hour(String.format("%02d", e.getKey() % 24), e.getValue())).toList();

        return new LinePage.Direction(group.get(0).headsign(), route, shape(p, byPattern.get(main), route),
                new LinePage.Timetable(days, rows), byPattern.size() > 1);
    }

    /** Cel mai des shape_id al curselor traseului principal (la egalitate, cel mai mic); altfel stațiile. */
    private List<double[]> shape(MapSqlParameterSource p, List<Trip> mainTrips, List<LinePage.Stop> route) {
        Optional<String> shape = mainTrips.stream().map(Trip::shapeId).filter(s -> s != null && !s.isBlank())
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .min(Comparator.<Map.Entry<String, Long>>comparingLong(e -> -e.getValue()).thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey);
        if (shape.isPresent()) {
            List<double[]> points = jdbc.query(SHAPE, new MapSqlParameterSource(p.getValues())
                    .addValue("shape", shape.get()), (rs, n) -> new double[] {rs.getDouble(1), rs.getDouble(2)});
            if (!points.isEmpty()) {
                return points;
            }
        }
        return route.stream().map(s -> new double[] {s.lat(), s.lon()}).toList();
    }
}
