package ro.autobuze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/** Schema din V2: versiuni de feed, tabelele GTFS și datele utilizatorilor (D-043, D-044). */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SchemaIT {

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void emptyTables() {
        jdbc.execute("truncate feed_versions, users, deviation_reports restart identity cascade");
    }

    @Test
    void flywayAppliesTheSchemaWithPostgis() {
        Integer applied = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where version = '2' and success", Integer.class);
        assertThat(applied).isEqualTo(1);
        Integer postgis = jdbc.queryForObject(
                "select count(*) from pg_extension where extname = 'postgis'", Integer.class);
        assertThat(postgis).isEqualTo(1);
    }

    @Test
    void departureAfterMidnightIsStoredInSecondsAndStopGetsAPoint() {
        long fv = feedVersion(true);
        insertTripWithOneStop(fv);
        // 25:10:00 = 90600 s de la începutul zilei de serviciu (D-043)
        Integer departure = jdbc.queryForObject(
                "select departure_sec from stop_times where feed_version_id = ? and trip_id = 'T1'",
                Integer.class, fv);
        assertThat(departure).isEqualTo(90600);
        String point = jdbc.queryForObject(
                "select ST_AsText(geog) from stops where feed_version_id = ? and stop_id = 'S1'", String.class, fv);
        assertThat(point).isEqualTo("POINT(24.15 45.79)");
    }

    @Test
    void onlyOneFeedVersionIsActive() {
        feedVersion(true);
        feedVersion(false);
        assertThatThrownBy(() -> feedVersion(true)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingAFeedVersionKeepsFavoritesAndReports() {
        long fv = feedVersion(true);
        insertTripWithOneStop(fv);
        long user = user("ana@example.ro");
        jdbc.update("insert into favorites (user_id, stop_key) values (?, 'S1')", user);
        jdbc.update("""
                insert into deviation_reports (source, user_id, route_short_name, stop_key, direction_id,
                    service_date, feed_time_sec, actual_time_sec, deviation_min)
                values ('utilizator', ?, '24B', 'S1', 0, date '2026-11-24', 90600, 90840, 4)""", user);

        jdbc.update("delete from feed_versions where id = ?", fv);

        for (String table : new String[] {"agency", "routes", "stops", "calendar", "calendar_dates",
                "shapes", "trips", "stop_times"}) {
            assertThat(jdbc.queryForObject("select count(*) from " + table, Integer.class))
                    .as(table).isZero();
        }
        assertThat(jdbc.queryForObject("select count(*) from favorites", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from deviation_reports", Integer.class)).isEqualTo(1);
    }

    @Test
    void emailIsUniqueIgnoringCase() {
        user("Ana@example.ro");
        assertThatThrownBy(() -> user("ana@example.ro")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void userReportNeedsAUserFieldReportDoesNot() {
        String report = """
                insert into deviation_reports (source, user_id, route_short_name, stop_key, direction_id,
                    service_date, feed_time_sec, actual_time_sec, deviation_min)
                values (?, null, '24B', 'S1', 0, date '2026-11-17', 27000, 27240, 4)""";
        assertThatThrownBy(() -> jdbc.update(report, "utilizator"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jdbc.update(report, "teren")).isEqualTo(1);
    }

    private long feedVersion(boolean active) {
        return jdbc.queryForObject(
                "insert into feed_versions (checksum, source_url, active) values ('abc', 'https://tursib.ro/trasee/gtfs', ?) returning id",
                Long.class, active);
    }

    private long user(String email) {
        return jdbc.queryForObject(
                "insert into users (email, password_hash, role) values (?, 'hash', 'USER') returning id",
                Long.class, email);
    }

    /** Un rând în fiecare tabel GTFS, cu o plecare la 25:10:00. */
    private void insertTripWithOneStop(long fv) {
        jdbc.update("insert into agency (feed_version_id, agency_id, agency_name, agency_url, agency_timezone) "
                + "values (?, 'TURSIB', 'Tursib S.A.', 'https://tursib.ro', 'Europe/Bucharest')", fv);
        jdbc.update("insert into routes (feed_version_id, route_id, agency_id, route_short_name, route_type) "
                + "values (?, 'R1', 'TURSIB', '24B', 3)", fv);
        jdbc.update("insert into stops (feed_version_id, stop_id, stop_name, stop_lat, stop_lon) "
                + "values (?, 'S1', 'Piața Unirii', 45.79, 24.15)", fv);
        jdbc.update("insert into calendar (feed_version_id, service_id, monday, tuesday, wednesday, thursday, "
                + "friday, saturday, sunday, start_date, end_date) "
                + "values (?, 'LV', true, true, true, true, true, false, false, date '2026-11-01', date '2027-06-30')", fv);
        jdbc.update("insert into calendar_dates (feed_version_id, service_id, date, exception_type) "
                + "values (?, 'LV', date '2026-12-01', 2)", fv);
        jdbc.update("insert into shapes (feed_version_id, shape_id, shape_pt_sequence, shape_pt_lat, shape_pt_lon) "
                + "values (?, 'SH1', 1, 45.79, 24.15)", fv);
        jdbc.update("insert into trips (feed_version_id, trip_id, route_id, service_id, trip_headsign, "
                + "direction_id, shape_id) values (?, 'T1', 'R1', 'LV', 'Gara', 0, 'SH1')", fv);
        jdbc.update("insert into stop_times (feed_version_id, trip_id, stop_sequence, stop_id, arrival_sec, "
                + "departure_sec) values (?, 'T1', 1, 'S1', 90600, 90600)", fv);
    }
}
