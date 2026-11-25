package ro.autobuze.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDateTime;
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
 * Pagina de stație pe mini-feed (punctul 8): D-018 (3 pe grup, completate cu mâine), D-048 (fără
 * capătul cursei), D-031 (gruparea stației), twist 1+2. Pe mini-feed, S_B e începutul cursei
 * {@code T2_LV} (07:15) și al lui {@code T2_NOAPTE} (23:50); S_NOAPTE are {@code T2_NOAPTE}
 * (24:40, LV) și {@code T2_DUM_NOAPTE} (24:35, DUM).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class NextDeparturesIT {

    static final FeedServer FEED = FeedServer.get();

    @DynamicPropertySource
    static void feedUrl(DynamicPropertyRegistry registry) {
        registry.add("autobuze.import.feed-url", FEED::feedUrl);
    }

    @Autowired
    GtfsImportService importer;

    @Autowired
    NextDepartures next;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void importMiniFeed() {
        jdbc.execute("truncate feed_versions restart identity cascade");
        FEED.serve(MiniFeed.zip());
        importer.importNow();
    }

    StationBoard board(String stopId, LocalDateTime now) {
        return next.at(stopId, now).orElseThrow();
    }

    @Test
    void lessThanThreeLeftTodayCompletesWithTomorrow() {
        // miercuri 25.11.2026, 22:00: azi doar 23:50; apoi joi 07:15 și 23:50, marcate „mâine”
        StationBoard board = board("S_B", LocalDateTime.of(2026, 11, 25, 22, 0));

        assertThat(board.feedImported()).isTrue();
        assertThat(board.station().name()).isEqualTo("Turnul Sfatului");
        assertThat(board.dayLabel()).isEqualTo("Miercuri, 25 noiembrie · zi lucrătoare");
        assertThat(board.groups()).extracting(StationBoard.Group::line, StationBoard.Group::headsign)
                .containsExactly(tuple("2", "Piața Cibin"));
        assertThat(board.groups().get(0).departures())
                .extracting(StationBoard.Entry::time, StationBoard.Entry::tomorrow, StationBoard.Entry::minutes)
                .containsExactly(tuple("23:50", false, 110), tuple("07:15", true, 555), tuple("23:50", true, 1550));
    }

    @Test
    void afterMidnightYesterdaysServiceDayComesFirstWithoutTomorrow() {
        // joi 26.11.2026, 00:30: 00:40 e din ziua de serviciu de miercuri; vineri 00:40 e „mâine”
        StationBoard board = board("S_NOAPTE", LocalDateTime.of(2026, 11, 26, 0, 30));

        assertThat(board.groups()).hasSize(1);
        assertThat(board.groups().get(0).departures())
                .extracting(StationBoard.Entry::at, StationBoard.Entry::tomorrow)
                .containsExactly(tuple(LocalDateTime.of(2026, 11, 26, 0, 40), false),
                        tuple(LocalDateTime.of(2026, 11, 27, 0, 40), true));
    }

    @Test
    void mondayHolidayAt0030ShowsOnlyTheSundayNightTrip() {
        // luni 30.11.2026 (Sf. Andrei), 00:30: doar cursa DUM de 00:35, din ziua de serviciu duminică;
        // LV e scos pe 30.11 și 01.12, deci nimic altceva azi sau mâine
        StationBoard board = board("S_NOAPTE", LocalDateTime.of(2026, 11, 30, 0, 30));

        assertThat(board.dayLabel()).isEqualTo("Luni, 30 noiembrie · sărbătoare legală");
        assertThat(board.groups()).hasSize(1);
        assertThat(board.groups().get(0).departures())
                .extracting(StationBoard.Entry::time, StationBoard.Entry::tomorrow, StationBoard.Entry::minutes)
                .containsExactly(tuple("00:35", false, 5));
    }

    @Test
    void tripsEndingAtTheStopAreLeftOut() {
        // D-048: T1_LV se termină în S_HORNBACH; T2_LV, T2_NOAPTE și T2_DUM_NOAPTE în S_CIBIN
        LocalDateTime now = LocalDateTime.of(2026, 11, 25, 6, 0);

        assertThat(board("S_HORNBACH", now).groups()).isEmpty();
        assertThat(board("S_CIBIN", now).groups()).isEmpty();
        // același T1_LV, din stația de început, apare
        assertThat(board("S_GARA", now).groups()).extracting(StationBoard.Group::line).containsExactly("1");
    }

    @Test
    void stationGroupsRowsWithTheSameNameWithin150m() {
        // D-031: S_A (Piața Mică) la 45.792000, 24.152000; ~100 m la nord intră, ~300 m nu
        long fv = jdbc.queryForObject("select id from feed_versions where active", Long.class);
        jdbc.update("insert into stops (feed_version_id, stop_id, stop_name, stop_lat, stop_lon) values"
                + " (?, 'S_A2', 'Piața Mică', 45.792900, 24.152000),"
                + " (?, 'S_A3', 'Piața Mică', 45.794700, 24.152000)", fv, fv);

        LocalDateTime now = LocalDateTime.of(2026, 11, 25, 6, 0);

        assertThat(board("S_A", now).station().stopIds()).containsExactly("S_A", "S_A2");
        assertThat(board("S_A2", now).station().stopIds()).containsExactly("S_A", "S_A2");
        assertThat(board("S_A3", now).station().stopIds()).containsExactly("S_A3");
        // plecările stației vin din toate rândurile ei: T1_LV la 07:10 din S_A
        assertThat(board("S_A2", now).groups().get(0).departures()).extracting(StationBoard.Entry::time)
                .startsWith("07:10");
    }

    @Test
    void extraServiceDayFromCalendarDates() {
        // EXTRA circulă doar sâmbătă 05.12.2026 (calendar_dates, tip 1): T3_EXTRA din S_C la 10:00
        StationBoard saturday = board("S_C", LocalDateTime.of(2026, 12, 5, 9, 0));
        assertThat(saturday.dayLabel()).isEqualTo("Sâmbătă, 5 decembrie · sâmbătă");
        assertThat(saturday.groups().get(0).departures())
                .extracting(StationBoard.Entry::time, StationBoard.Entry::tomorrow, StationBoard.Entry::minutes)
                .containsExactly(tuple("10:00", false, 60));

        StationBoard friday = board("S_C", LocalDateTime.of(2026, 12, 4, 9, 0));
        assertThat(friday.groups()).extracting(StationBoard.Group::line, StationBoard.Group::headsign)
                .containsExactly(tuple("3", "Turnișor"));
        assertThat(friday.groups().get(0).departures()).extracting(StationBoard.Entry::tomorrow).containsExactly(true);
    }

    @Test
    void unknownStopAndMissingFeed() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 25, 6, 0);
        assertThat(next.at("NU_EXISTA", now)).isEmpty();

        jdbc.execute("truncate feed_versions restart identity cascade");
        StationBoard none = board("S_B", now);
        assertThat(none.feedImported()).isFalse();
        assertThat(none.station()).isNull();
        assertThat(none.groups()).isEmpty();
    }
}
