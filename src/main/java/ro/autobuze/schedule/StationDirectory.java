package ro.autobuze.schedule;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Stațiile pentru hartă și „aproape de mine” (C-003). O stație = rândurile din stops.txt cu același
 * nume la cel mult 150 m (D-031, aceeași regulă ca {@link Stations}); grupul pornește de la cel mai
 * mic {@code stop_id} încă negrupat, iar poziția stației e media rândurilor. Pe feed-ul Tursib nu
 * există același nume la 150–400 m (DATA.md), deci grupurile nu depind de ordine.
 */
@Repository
public class StationDirectory {

    static final int NEAR_LIMIT = 3;
    static final int NEAR_RADIUS_M = 1000;

    /** Fiecare rând cu vecinii lui (inclusiv el însuși): același nume, la cel mult 150 m. */
    private static final String NEIGHBOURS = """
            select x.stop_id, x.stop_name, s.stop_id, s.stop_lat, s.stop_lon
            from stops x
            join stops s on s.feed_version_id = x.feed_version_id
                        and s.stop_name = x.stop_name
                        and ST_DWithin(s.geog, x.geog, 150)
            where x.feed_version_id = ?
            order by x.stop_id, s.stop_id""";

    /** Rândurile la cel mult 1 km de un punct, de la cel mai apropiat (D-034). */
    private static final String NEAR = """
            select s.stop_id, ST_Distance(s.geog, p.geog) as distance
            from stops s, (select ST_SetSRID(ST_MakePoint(?::float8, ?::float8), 4326)::geography as geog) p
            where s.feed_version_id = ? and ST_DWithin(s.geog, p.geog, ?)
            order by distance, s.stop_id""";

    private static final Comparator<MapStation> BY_NAME = Comparator
            .comparing(MapStation::name, Collator.getInstance(Locale.forLanguageTag("ro")))
            .thenComparing(MapStation::stopId);

    /** O stație pe hartă; {@code stopId} e cel mai mic din {@code stopIds}. */
    public record MapStation(String stopId, String name, double lat, double lon, List<String> stopIds) {
    }

    /** O stație lângă un punct, cu distanța în metri până la cel mai apropiat rând al ei. */
    public record NearStation(String stopId, String name, double lat, double lon, int distanceM) {
    }

    private record Row(String stopId, double lat, double lon) {
    }

    private final JdbcTemplate jdbc;

    public StationDirectory(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Toate stațiile versiunii {@code feedVersionId}, ordonate după nume (D-054). */
    public List<MapStation> all(long feedVersionId) {
        Map<String, String> names = new LinkedHashMap<>();
        Map<String, List<Row>> neighbours = new LinkedHashMap<>();
        jdbc.query(NEIGHBOURS, rs -> {
            names.put(rs.getString(1), rs.getString(2));
            neighbours.computeIfAbsent(rs.getString(1), k -> new ArrayList<>())
                    .add(new Row(rs.getString(3), rs.getDouble(4), rs.getDouble(5)));
        }, feedVersionId);

        List<String> ids = new ArrayList<>(names.keySet());
        ids.sort(Comparator.naturalOrder());
        Set<String> grouped = new HashSet<>();
        List<MapStation> stations = new ArrayList<>();
        for (String id : ids) {
            if (grouped.contains(id)) {
                continue;
            }
            List<Row> members = neighbours.get(id).stream().filter(r -> !grouped.contains(r.stopId()))
                    .sorted(Comparator.comparing(Row::stopId)).toList();
            members.forEach(r -> grouped.add(r.stopId()));
            stations.add(new MapStation(id, names.get(id),
                    members.stream().mapToDouble(Row::lat).average().orElseThrow(),
                    members.stream().mapToDouble(Row::lon).average().orElseThrow(),
                    members.stream().map(Row::stopId).toList()));
        }
        stations.sort(BY_NAME);
        return stations;
    }

    /** Cele mai apropiate 3 stații la cel mult 1 km de punct (D-034), de la cea mai apropiată. */
    public List<NearStation> near(long feedVersionId, double lat, double lon) {
        Map<String, MapStation> stationOf = new HashMap<>();
        for (MapStation s : all(feedVersionId)) {
            s.stopIds().forEach(id -> stationOf.put(id, s));
        }
        Set<String> seen = new HashSet<>();
        List<NearStation> near = new ArrayList<>();
        jdbc.query(NEAR, rs -> {
            MapStation s = stationOf.get(rs.getString(1));
            if (near.size() < NEAR_LIMIT && s != null && seen.add(s.stopId())) {
                near.add(new NearStation(s.stopId(), s.name(), s.lat(), s.lon(),
                        (int) Math.round(rs.getDouble(2))));
            }
        }, lon, lat, feedVersionId, NEAR_RADIUS_M);
        return near;
    }
}
