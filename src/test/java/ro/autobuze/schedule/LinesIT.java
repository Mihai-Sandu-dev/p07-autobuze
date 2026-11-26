package ro.autobuze.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ro.autobuze.TestcontainersConfiguration;
import ro.autobuze.gtfs.FeedServer;
import ro.autobuze.gtfs.GtfsImportService;
import ro.autobuze.gtfs.MiniFeed;

/**
 * Pagina de linie (D-060) pe mini-feed. Linia 2 spre „Piața Cibin” are trei curse pe trei trasee:
 * T2_LV (S_B → S_CIBIN, LV, 07:15), T2_NOAPTE (S_B → S_NOAPTE → S_CIBIN, LV, 23:50) și T2_DUM_NOAPTE
 * (S_NOAPTE → S_CIBIN, DUM, 24:35). Miercuri 25.11.2026 e zi lucrătoare; 30.11 și 01.12 sunt sărbători
 * legale (V3), iar LV nu circulă atunci (calendar_dates); EXTRA (linia 3) circulă doar sâmbătă, 5.12.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LinesIT {

    static final FeedServer FEED = FeedServer.get();
    static final LocalDate WEDNESDAY = LocalDate.of(2026, 11, 25);

    @DynamicPropertySource
    static void feedUrl(DynamicPropertyRegistry registry) {
        registry.add("autobuze.import.feed-url", FEED::feedUrl);
    }

    @Autowired
    Lines lines;

    @Autowired
    GtfsImportService importer;

    @Autowired
    JdbcTemplate jdbc;

    long fv;

    @BeforeEach
    void importMiniFeed() {
        jdbc.execute("truncate feed_versions restart identity cascade");
        FEED.serve(MiniFeed.zip());
        importer.importNow();
        fv = jdbc.queryForObject("select id from feed_versions where active", Long.class);
    }

    LinePage.Direction direction(String line, LocalDate today) {
        return lines.page(fv, line, today).orElseThrow().directions().get(0);
    }

    static List<String> names(LinePage.Direction d) {
        return d.stops().stream().map(LinePage.Stop::name).toList();
    }

    /** Minutele zilei {@code day} (0 lucrătoare, 1 sâmbătă, 2 duminică), ca „HH:mm” sau „HH:mm◦” pentru alt traseu. */
    static List<String> times(LinePage.Direction d, int day) {
        return d.timetable().hours().stream()
                .flatMap(h -> h.perDay().get(day).stream()
                        .map(m -> h.label() + ":" + m.minute() + (m.otherRoute() ? "◦" : "")))
                .toList();
    }

    void addTrip(String tripId, String hhmm1, String hhmm2) {
        jdbc.update("insert into trips (feed_version_id, route_id, service_id, trip_id, trip_headsign, direction_id)"
                + " values (?, 'R2', 'LV', ?, 'Piața Cibin', 0)", fv, tripId);
        jdbc.update("insert into stop_times (feed_version_id, trip_id, stop_id, stop_sequence, arrival_sec, departure_sec)"
                + " values (?, ?, 'S_B', 1, ?, ?), (?, ?, 'S_CIBIN', 2, ?, ?)",
                fv, tripId, sec(hhmm1), sec(hhmm1), fv, tripId, sec(hhmm2), sec(hhmm2));
    }

    static int sec(String hhmm) {
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 3600 + Integer.parseInt(p[1]) * 60;
    }

    @Test
    void theRouteMostTripsTakeGivesTheStopsAndTheOthersAreMarked() {
        addTrip("T2_EXTRA_1", "09:15", "09:25");
        addTrip("T2_EXTRA_2", "11:15", "11:25");
        LinePage.Direction d = direction("2", WEDNESDAY);

        assertThat(d.headsign()).isEqualTo("Piața Cibin");
        assertThat(names(d)).containsExactly("Turnul Sfatului", "Piața Cibin, peron 2");
        assertThat(times(d, 0)).containsExactly("07:15", "09:15", "11:15", "23:50◦");
        assertThat(d.hasOtherRoute()).isTrue();
    }

    @Test
    void onATieTheRouteWithMoreStopsWins() {
        LinePage.Direction d = direction("2", WEDNESDAY);
        assertThat(names(d)).containsExactly("Turnul Sfatului", "Strada Noaptea", "Piața Cibin, peron 2");
        assertThat(d.stops().get(0).stopId()).isEqualTo("S_B");
        assertThat(times(d, 0)).containsExactly("07:15◦", "23:50");
    }

    @Test
    void theShapeComesFromShapesTxtOrElseFromTheStops() {
        assertThat(direction("1", WEDNESDAY).shape())
                .containsExactly(new double[] {45.7995, 24.1617}, new double[] {45.792, 24.152});
        // linia 2 nu are shape_id: linie dreaptă între stații
        assertThat(direction("2", WEDNESDAY).shape())
                .containsExactly(new double[] {45.7929, 24.152}, new double[] {45.8019, 24.152},
                        new double[] {45.8003, 24.1475});
    }

    @Test
    void columnsComeFromTheNextWorkdaySaturdayAndSunday() {
        LinePage.Direction d = direction("2", WEDNESDAY);
        assertThat(d.timetable().days()).extracting(LinePage.Day::label)
                .containsExactly("Lucrătoare", "Sâmbătă", "Duminică");
        assertThat(d.timetable().days()).extracting(LinePage.Day::date).containsExactly(
                WEDNESDAY, LocalDate.of(2026, 11, 28), LocalDate.of(2026, 11, 29));
        assertThat(d.timetable().days().get(0).dateText()).isEqualTo("miercuri, 25 noiembrie");
        assertThat(d.timetable().days().get(1).dateText()).isEqualTo("28 noiembrie");
        assertThat(d.timetable().emptyDay(1)).isTrue();
        assertThat(times(d, 2)).containsExactly("00:35◦");
    }

    @Test
    void afterMidnightIsTheLastRow() {
        // 24:35 din ziua de serviciu de duminică apare ca „00”, după „23”
        assertThat(direction("2", WEDNESDAY).timetable().hours()).extracting(LinePage.Hour::label)
                .containsExactly("07", "23", "00");
    }

    @Test
    void publicHolidaysAreSkippedForTheWorkdayColumn() {
        // Sâmbătă 28.11: 30.11 și 01.12 sunt sărbători legale, deci ziua lucrătoare e miercuri, 2.12
        LinePage.Direction d = direction("2", LocalDate.of(2026, 11, 28));
        assertThat(d.timetable().days()).extracting(LinePage.Day::date).containsExactly(
                LocalDate.of(2026, 12, 2), LocalDate.of(2026, 11, 28), LocalDate.of(2026, 11, 29));
        assertThat(times(d, 0)).containsExactly("07:15◦", "23:50");
    }

    @Test
    void servicesAddedInCalendarDatesCount() {
        // EXTRA: doar sâmbătă, 5.12 (calendar_dates, tip 1)
        LinePage.Direction d = direction("3", LocalDate.of(2026, 12, 2));
        assertThat(d.timetable().days().get(1).date()).isEqualTo(LocalDate.of(2026, 12, 5));
        assertThat(times(d, 1)).containsExactly("10:00");
        assertThat(d.timetable().emptyDay(0)).isTrue();
        assertThat(d.hasOtherRoute()).isFalse();
    }

    @Test
    void anUnknownLineIsEmpty() {
        assertThat(lines.page(fv, "NU", WEDNESDAY)).isEmpty();
    }
}
