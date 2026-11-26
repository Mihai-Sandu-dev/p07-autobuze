package ro.autobuze.schedule;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Stația din aplicație a unui rând din stops.txt (D-031). Feed-ul Tursib nu are
 * {@code parent_station} (Q-019), deci stația = rândurile cu același nume aflate la cel mult 150 m
 * de rândul cerut. Poziția stației e media rândurilor ei (C-002).
 */
@Repository
public class Stations {

    private static final String SQL = """
            select s.stop_id, x.stop_name, s.stop_lat, s.stop_lon
            from stops x
            join stops s on s.feed_version_id = x.feed_version_id
                        and s.stop_name = x.stop_name
                        and ST_DWithin(s.geog, x.geog, 150)
            where x.feed_version_id = ? and x.stop_id = ?
            order by s.stop_id""";

    private record Row(String stopId, String name, double lat, double lon) {
    }

    private final JdbcTemplate jdbc;

    public Stations(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Stația lui {@code stopId} în versiunea {@code feedVersionId}; gol dacă rândul nu există. */
    public Optional<StationBoard.Station> of(long feedVersionId, String stopId) {
        List<Row> rows = jdbc.query(SQL, (rs, n) -> new Row(rs.getString(1), rs.getString(2), rs.getDouble(3),
                rs.getDouble(4)), feedVersionId, stopId);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new StationBoard.Station(rows.get(0).name(), rows.stream().map(Row::stopId).toList(),
                rows.stream().mapToDouble(Row::lat).average().orElseThrow(),
                rows.stream().mapToDouble(Row::lon).average().orElseThrow()));
    }
}
