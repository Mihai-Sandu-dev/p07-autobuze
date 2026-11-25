package ro.autobuze.gtfs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ro.autobuze.TestcontainersConfiguration;
import ro.autobuze.gtfs.ImportStatus.State;

/**
 * Importul feed-ului GTFS (D-003, D-019, D-038, D-043, D-044), pe mini-feed-ul din
 * {@code src/test/resources/gtfs/mini}, servit de un server HTTP local.
 *
 * <p>Scenariile mini-feed-ului sunt ale autorului; fiecare are un test mai jos:
 * <ul>
 *   <li>a) serviciul {@code LV}, luni–vineri, scos pe 30.11.2026 (Sf. Andrei) și 01.12.2026 prin
 *       calendar_dates (tip 2): sărbători legale, luni și marți;</li>
 *   <li>b) serviciul {@code EXTRA}, doar în calendar_dates (tip 1 pe 05.12.2026), fără rând în calendar;</li>
 *   <li>c) cursa {@code T2_NOAPTE}: pleacă la 23:50:00 din {@code S_B}, trece prin {@code S_NOAPTE}
 *       (~1 km, nu e capăt) la 24:40:00 și ajunge în {@code S_CIBIN} la 25:10:00;</li>
 *   <li>d) stațiile {@code S_A} (linia 1) și {@code S_B} (linia 2), la ~100 m: transfer;</li>
 *   <li>e) stația {@code S_C} (linia 3), la ~200 m de {@code S_A}: NU contează ca transfer;</li>
 *   <li>f) stația {@code S_CIBIN}, cu virgulă în nume: „Piața Cibin, peron 2”.</li>
 * </ul>
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class GtfsImportIT {

    static final FeedServer FEED = FeedServer.get();

    @DynamicPropertySource
    static void feedUrl(DynamicPropertyRegistry registry) {
        registry.add("autobuze.import.feed-url", FEED::feedUrl);
    }

    @Autowired
    GtfsImportService importer;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void emptyFeedVersions() {
        jdbc.execute("truncate feed_versions restart identity cascade");
        FEED.serve(MiniFeed.zip());
    }

    @Test
    void importsTheMiniFeedAsTheActiveVersion() throws Exception {
        ImportStatus status = importer.importNow();

        assertThat(status.state()).isEqualTo(State.DONE);
        assertThat(status.rows()).containsOnly(
                entry("agency", 1L), entry("stops", 8L), entry("routes", 3L), entry("calendar", 1L),
                entry("calendar_dates", 3L), entry("shapes", 3L), entry("trips", 4L), entry("stop_times", 10L));
        for (Map.Entry<String, Long> table : status.rows().entrySet()) {
            assertThat(jdbc.queryForObject("select count(*) from " + table.getKey(), Long.class))
                    .as(table.getKey()).isEqualTo(table.getValue());
        }
        Map<String, Object> version = jdbc.queryForMap("select id, checksum, source_url, active from feed_versions");
        assertThat(version.get("id")).isEqualTo(status.feedVersionId());
        assertThat(version.get("active")).isEqualTo(true);
        assertThat(version.get("checksum")).isEqualTo(sha256(MiniFeed.zip())).isEqualTo(status.checksum());
        // URL-ul final, după redirect: la Tursib conține numele versiunii
        assertThat(version.get("source_url")).isEqualTo(FEED.fileUrl());
        assertThat(jdbc.queryForObject("select count(*) from stops where geog is null", Integer.class)).isZero();
    }

    @Test
    void scenarioA_weekdayServiceRemovedOnDecemberFirst() throws Exception {
        importer.importNow();
        // a) LV circulă luni–vineri, dar nu pe 30.11 și 01.12.2026, sărbători legale (calendar_dates, tip 2)
        assertThat(jdbc.queryForObject(
                "select monday and friday and not saturday and not sunday from calendar where service_id = 'LV'",
                Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject(
                "select exception_type from calendar_dates where service_id = 'LV' and date = date '2026-12-01'",
                Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject(
                "select exception_type from calendar_dates where service_id = 'LV' and date = date '2026-11-30'",
                Integer.class)).isEqualTo(2);
    }

    @Test
    void scenarioB_serviceOnlyInCalendarDates() throws Exception {
        importer.importNow();
        // b) EXTRA nu are rând în calendar, doar o zi adăugată (tip 1); cursa lui se importă
        assertThat(jdbc.queryForObject("select count(*) from calendar where service_id = 'EXTRA'", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject(
                "select exception_type from calendar_dates where service_id = 'EXTRA' and date = date '2026-12-05'",
                Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select service_id from trips where trip_id = 'T3_EXTRA'", String.class))
                .isEqualTo("EXTRA");
    }

    @Test
    void scenarioC_tripAfterMidnightIsStoredInSeconds() throws Exception {
        importer.importNow();
        // c) 23:50:00 → 85800 s; 24:40:00 → 88800 s; 25:10:00 → 90600 s de la începutul zilei de serviciu (D-043)
        List<Integer> times = jdbc.queryForList(
                "select departure_sec from stop_times where trip_id = 'T2_NOAPTE' order by stop_sequence",
                Integer.class);
        assertThat(times).containsExactly(85800, 88800, 90600);
        // o plecare după miezul nopții dintr-o stație care nu e capăt de linie
        assertThat(jdbc.queryForObject(
                "select departure_sec from stop_times where trip_id = 'T2_NOAPTE' and stop_id = 'S_NOAPTE'",
                Integer.class)).isEqualTo(88800);
        assertThat(distance("S_B", "S_NOAPTE")).isBetween(950.0, 1050.0);
        assertThat(jdbc.queryForObject(
                "select arrival_sec from stop_times where trip_id = 'T2_NOAPTE' and stop_sequence = 3",
                Integer.class)).isEqualTo(90600);
    }

    @Test
    void scenarioD_stopsAbout100mApartOnDifferentLines() throws Exception {
        importer.importNow();
        // d) S_A (linia 1) și S_B (linia 2) la ~100 m: sub pragul de transfer de 150 m (D-012)
        assertThat(distance("S_A", "S_B")).isBetween(90.0, 110.0);
        assertThat(lineAt("S_A")).isEqualTo("1");
        assertThat(lineAt("S_B")).isEqualTo("2");
    }

    @Test
    void scenarioE_stopFartherThan150m() throws Exception {
        importer.importNow();
        // e) S_C (linia 3) e la ~200 m de S_A și ~300 m de S_B: peste 150 m, nu e transfer
        assertThat(distance("S_A", "S_C")).isGreaterThan(150.0);
        assertThat(distance("S_B", "S_C")).isGreaterThan(150.0);
        assertThat(lineAt("S_C")).isEqualTo("3");
    }

    @Test
    void scenarioF_stopNameWithComma() throws Exception {
        importer.importNow();
        // f) virgula din nume rămâne în nume, nu desparte coloane
        assertThat(jdbc.queryForObject("select stop_name from stops where stop_id = 'S_CIBIN'", String.class))
                .isEqualTo("Piața Cibin, peron 2");
        assertThat(jdbc.queryForObject("select stop_lat from stops where stop_id = 'S_CIBIN'", Double.class))
                .isEqualTo(45.8003);
    }

    @Test
    void sameChecksumCreatesNoNewVersion() throws Exception {
        ImportStatus first = importer.importNow();

        ImportStatus second = importer.importNow();

        // D-019: același checksum ca versiunea activă → doar verificarea, fără versiune nouă
        assertThat(second.state()).isEqualTo(State.UNCHANGED);
        assertThat(second.checksum()).isEqualTo(first.checksum());
        assertThat(jdbc.queryForObject("select count(*) from feed_versions", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from stop_times", Integer.class)).isEqualTo(10);
    }

    @Test
    void newFeedBecomesActiveAndOnlyTwoVersionsAreKept() throws Exception {
        long v1 = importer.importNow().feedVersionId();
        FEED.serve(MiniFeed.zip(Map.of("agency.txt", MiniFeed.file("agency.txt").replace("Mini Tursib", "Mini 2"))));
        long v2 = importer.importNow().feedVersionId();

        assertThat(activeVersions()).containsExactly(v2);
        assertThat(jdbc.queryForList("select id from feed_versions order by id", Long.class)).containsExactly(v1, v2);

        FEED.serve(MiniFeed.zip(Map.of("agency.txt", MiniFeed.file("agency.txt").replace("Mini Tursib", "Mini 3"))));
        long v3 = importer.importNow().feedVersionId();

        // D-019: rămân versiunea activă și cea anterioară; cele mai vechi se șterg, cu datele lor
        assertThat(jdbc.queryForList("select id from feed_versions order by id", Long.class)).containsExactly(v2, v3);
        assertThat(activeVersions()).containsExactly(v3);
        assertThat(jdbc.queryForObject("select count(*) from stop_times where feed_version_id = ?", Integer.class, v1))
                .isZero();
        assertThat(jdbc.queryForObject("select count(*) from stop_times where feed_version_id = ?", Integer.class, v2))
                .isEqualTo(10);
    }

    @Test
    void brokenFeedRollsBackAndKeepsTheActiveVersion() throws Exception {
        long v1 = importer.importNow().feedVersionId();
        FEED.serve(MiniFeed.zip(Map.of("stop_times.txt",
                MiniFeed.file("stop_times.txt") + "T1_LV,07:40:00,07:40:00,NU_EXISTA,4\n")));

        ImportStatus status = importer.importNow();

        // D-003: tot sau nimic; versiunea activă rămâne
        assertThat(status.state()).isEqualTo(State.FAILED);
        assertThat(status.message()).isNotBlank();
        assertThat(jdbc.queryForList("select id from feed_versions", Long.class)).containsExactly(v1);
        assertThat(activeVersions()).containsExactly(v1);
        assertThat(jdbc.queryForObject("select count(*) from stop_times", Integer.class)).isEqualTo(10);
    }

    @Test
    void missingRequiredFileFails() throws Exception {
        FEED.serve(MiniFeed.zip(java.util.Collections.singletonMap("stops.txt", null)));

        ImportStatus status = importer.importNow();

        assertThat(status.state()).isEqualTo(State.FAILED);
        assertThat(status.message()).contains("stops.txt");
        assertThat(jdbc.queryForObject("select count(*) from feed_versions", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from agency", Integer.class)).isZero();
    }

    @Test
    void missingCalendarAndCalendarDatesFails() throws Exception {
        Map<String, String> without = new HashMap<>();
        without.put("calendar.txt", null);
        without.put("calendar_dates.txt", null);
        FEED.serve(MiniFeed.zip(without));

        ImportStatus status = importer.importNow();

        assertThat(status.state()).isEqualTo(State.FAILED);
        assertThat(status.message()).contains("calendar.txt").contains("calendar_dates.txt");
        assertThat(jdbc.queryForObject("select count(*) from feed_versions", Integer.class)).isZero();
    }

    @Test
    void missingRequiredColumnFails() throws Exception {
        FEED.serve(MiniFeed.zip(Map.of("stops.txt", "stop_id,stop_name,stop_lon\nS_A,Piața Mică,24.152\n")));

        ImportStatus status = importer.importNow();

        assertThat(status.state()).isEqualTo(State.FAILED);
        assertThat(status.message()).contains("stops.txt").contains("stop_lat");
        assertThat(jdbc.queryForObject("select count(*) from feed_versions", Integer.class)).isZero();
    }

    @Test
    void everyForeignKeyOfTheGtfsTablesHasAnIndex() {
        // Indecși expliciți: ștergerea în cascadă a unei versiuni vechi (D-019) nu scanează tabelele întregi
        List<String> unindexed = jdbc.queryForList("""
                select c.conrelid::regclass::text || ' ' || c.conname
                from pg_constraint c
                where c.contype = 'f'
                  and c.conrelid::regclass::text in
                      ('agency', 'stops', 'routes', 'calendar', 'calendar_dates', 'shapes', 'trips', 'stop_times')
                  and not exists (
                      select 1 from pg_index i
                      where i.indrelid = c.conrelid
                        and (string_to_array(i.indkey::text, ' ')::int2[])[1:array_length(c.conkey, 1)] = c.conkey)
                """, String.class);
        assertThat(unindexed).isEmpty();
    }

    private List<Long> activeVersions() {
        return jdbc.queryForList("select id from feed_versions where active", Long.class);
    }

    private double distance(String a, String b) {
        return jdbc.queryForObject(
                "select ST_Distance(x.geog, y.geog) from stops x, stops y where x.stop_id = ? and y.stop_id = ?",
                Double.class, a, b);
    }

    private String lineAt(String stopId) {
        return jdbc.queryForObject("""
                select distinct r.route_short_name
                from stop_times st
                join trips t on t.feed_version_id = st.feed_version_id and t.trip_id = st.trip_id
                join routes r on r.feed_version_id = t.feed_version_id and r.route_id = t.route_id
                where st.stop_id = ?""", String.class, stopId);
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
