package ro.autobuze.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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
 * Twist 1: serviciile active într-o zi, din calendar.txt și calendar_dates.txt; sărbătorile
 * legale din DB dau doar tipul de zi (D-014, D-015). Pe mini-feed: {@code LV} luni–vineri,
 * 01.11.2026–30.06.2027, scos pe 30.11 și 01.12.2026; {@code DUM} doar duminica, aceeași
 * perioadă; {@code EXTRA} doar pe 05.12.2026.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ServiceCalendarIT {

    static final FeedServer FEED = FeedServer.get();

    @DynamicPropertySource
    static void feedUrl(DynamicPropertyRegistry registry) {
        registry.add("autobuze.import.feed-url", FEED::feedUrl);
    }

    @Autowired
    GtfsImportService importer;

    @Autowired
    ServiceCalendar calendar;

    @Autowired
    JdbcTemplate jdbc;

    long fv;

    @BeforeEach
    void importMiniFeed() {
        jdbc.execute("truncate feed_versions restart identity cascade");
        FEED.serve(MiniFeed.zip());
        importer.importNow();
        fv = calendar.activeFeedVersion().orElseThrow();
    }

    @Test
    void weekdayRunsTheWeekdayService() {
        // miercuri 25.11.2026
        assertThat(calendar.activeServices(fv, LocalDate.of(2026, 11, 25))).containsExactly("LV");
    }

    @Test
    void removedDatesRunNothing() {
        // luni 30.11 (Sf. Andrei) și marți 01.12.2026: LV scos prin calendar_dates (tip 2)
        assertThat(calendar.activeServices(fv, LocalDate.of(2026, 11, 30))).isEmpty();
        assertThat(calendar.activeServices(fv, LocalDate.of(2026, 12, 1))).isEmpty();
    }

    @Test
    void serviceOnlyInCalendarDatesRunsOnItsDate() {
        // sâmbătă 05.12.2026: EXTRA adăugat (tip 1), fără rând în calendar
        assertThat(calendar.activeServices(fv, LocalDate.of(2026, 12, 5))).containsExactly("EXTRA");
        // sâmbătă 28.11.2026: nimic
        assertThat(calendar.activeServices(fv, LocalDate.of(2026, 11, 28))).isEmpty();
    }

    @Test
    void sundayRunsTheSundayService() {
        assertThat(calendar.activeServices(fv, LocalDate.of(2026, 11, 29))).containsExactly("DUM");
    }

    @Test
    void validityPeriodBoundaries() {
        // ultima zi din calendar (miercuri 30.06.2027) intră; joi 01.07.2027 nu
        assertThat(calendar.activeServices(fv, LocalDate.of(2027, 6, 30))).containsExactly("LV");
        assertThat(calendar.activeServices(fv, LocalDate.of(2027, 7, 1))).isEmpty();
        // prima zi: luni 02.11.2026
        assertThat(calendar.activeServices(fv, LocalDate.of(2026, 11, 2))).containsExactly("LV");
    }

    @Test
    void holidayListDoesNotChangeTheServices() {
        // D-015: luni 03.05.2027 e a doua zi de Paște, sărbătoare în DB, dar feed-ul n-o scoate:
        // LV circulă; tipul de zi e totuși „duminică și sărbătoare”
        LocalDate easterMonday = LocalDate.of(2027, 5, 3);
        assertThat(calendar.activeServices(fv, easterMonday)).containsExactly("LV");
        assertThat(calendar.dayType(easterMonday)).isEqualTo(DayType.DUMINICA_SARBATOARE);
        assertThat(calendar.dayType(LocalDate.of(2026, 11, 25))).isEqualTo(DayType.LUCRATOARE);
    }

    @Test
    void otherFeedVersionsDoNotCount() {
        long other = jdbc.queryForObject(
                "insert into feed_versions (checksum, source_url) values ('alt', 'test') returning id", Long.class);
        jdbc.update("insert into calendar values (?, 'ALT', true, true, true, true, true, true, true, "
                + "date '2026-01-01', date '2027-12-31')", other);
        jdbc.update("insert into calendar_dates values (?, 'ALT2', date '2026-11-25', 1)", other);

        assertThat(calendar.activeServices(fv, LocalDate.of(2026, 11, 25))).containsExactly("LV");
        assertThat(calendar.activeServices(other, LocalDate.of(2026, 11, 25))).containsExactlyInAnyOrder("ALT", "ALT2");
    }

    @Test
    void publicHolidaysFor2026And2027() {
        // D-015: 16 date în 2026 (01.06 = Rusalii și Ziua Copilului, un rând), 17 în 2027
        assertThat(jdbc.queryForObject(
                "select count(*) from public_holidays where extract(year from date) = 2026", Integer.class))
                .isEqualTo(16);
        assertThat(jdbc.queryForObject(
                "select count(*) from public_holidays where extract(year from date) = 2027", Integer.class))
                .isEqualTo(17);
        assertThat(jdbc.queryForObject(
                "select name from public_holidays where date = date '2026-11-30'", String.class))
                .isEqualTo("Sfântul Andrei");
    }
}
