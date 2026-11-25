package ro.autobuze.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * Twist 2: plecările de după miezul nopții (ore ≥ 24:00) vin din ziua de serviciu anterioară
 * (D-018, D-043, D-047). Pe mini-feed, din {@code S_NOAPTE}: {@code T2_NOAPTE} (LV, luni–vineri)
 * la 24:40:00 și {@code T2_DUM_NOAPTE} (DUM, duminica) la 24:35:00.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DeparturesIT {

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
    Departures departures;

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
    void afterMidnightTheTripOfYesterdaysServiceDayLeaves() {
        // joi 26.11.2026, 00:30: cursa de miercuri (ziua de serviciu 25.11), 24:40:00 → 00:40
        List<Departure> result = departures.between(fv, List.of("S_NOAPTE"),
                LocalDateTime.of(2026, 11, 26, 0, 30), LocalDateTime.of(2026, 11, 26, 2, 30));

        assertThat(result).extracting(Departure::tripId, Departure::serviceDate, Departure::time)
                .containsExactly(tuple("T2_NOAPTE", LocalDate.of(2026, 11, 25), LocalDateTime.of(2026, 11, 26, 0, 40)));
        assertThat(result.get(0).routeShortName()).isEqualTo("2");
        assertThat(result.get(0).headsign()).isEqualTo("Piața Cibin");
    }

    @Test
    void mondayHolidayAt0030OnlyTheSundayNightTrip() {
        // Răspunsul la întrebarea din fișă: „următoarele plecări la 00:30 într-o zi de luni,
        // sărbătoare legală”. Luni 30.11.2026 (Sf. Andrei), 00:30, din S_NOAPTE: pleacă doar cursa
        // DUM de la 00:35, din ziua de serviciu duminică 29.11. Nimic din LV: LV e scos pe 30.11,
        // iar cursa LV de noapte ar ține oricum de ziua de serviciu 30.11 (pleacă pe 01.12, 00:40).
        List<Departure> result = departures.between(fv, List.of("S_NOAPTE"),
                LocalDateTime.of(2026, 11, 30, 0, 30), LocalDateTime.of(2026, 11, 30, 23, 59));

        assertThat(result).extracting(Departure::tripId, Departure::serviceDate, Departure::time)
                .containsExactly(tuple("T2_DUM_NOAPTE", LocalDate.of(2026, 11, 29),
                        LocalDateTime.of(2026, 11, 30, 0, 35)));
    }

    @Test
    void removedServiceDayHasNoNightTrip() {
        // marți 01.12.2026, 00:30: ziua de serviciu luni 30.11 e scoasă, deci nicio plecare la 00:40
        assertThat(departures.between(fv, List.of("S_NOAPTE"),
                LocalDateTime.of(2026, 12, 1, 0, 30), LocalDateTime.of(2026, 12, 1, 2, 30))).isEmpty();
    }

    @Test
    void saturdayNightBelongsToFriday() {
        // sâmbătă 28.11.2026, 00:30: cursa de vineri (ziua de serviciu 27.11), 00:40
        assertThat(departures.between(fv, List.of("S_NOAPTE"),
                LocalDateTime.of(2026, 11, 28, 0, 30), LocalDateTime.of(2026, 11, 28, 2, 30)))
                .extracting(Departure::tripId, Departure::serviceDate, Departure::time)
                .containsExactly(tuple("T2_NOAPTE", LocalDate.of(2026, 11, 27), LocalDateTime.of(2026, 11, 28, 0, 40)));
    }

    @Test
    void intervalStartIsInclusiveAndEndExclusive() {
        // de la 00:40 exact: plecarea de la 00:40 intră; până la 00:40: nu intră
        assertThat(departures.between(fv, List.of("S_NOAPTE"),
                LocalDateTime.of(2026, 11, 26, 0, 40), LocalDateTime.of(2026, 11, 26, 1, 0))).hasSize(1);
        assertThat(departures.between(fv, List.of("S_NOAPTE"),
                LocalDateTime.of(2026, 11, 26, 0, 0), LocalDateTime.of(2026, 11, 26, 0, 40))).isEmpty();
    }

    @Test
    void severalStopsInTheOrderOfTheRealTime() {
        // stațiile grupate (D-031) vin ca listă de stop_id; plecările, în ordinea orei reale
        List<Departure> result = departures.between(fv, List.of("S_B", "S_NOAPTE"),
                LocalDateTime.of(2026, 11, 25, 23, 45), LocalDateTime.of(2026, 11, 26, 23, 59));

        assertThat(result).extracting(Departure::stopId, Departure::time).containsExactly(
                tuple("S_B", LocalDateTime.of(2026, 11, 25, 23, 50)),       // T2_NOAPTE, miercuri
                tuple("S_NOAPTE", LocalDateTime.of(2026, 11, 26, 0, 40)),   // aceeași cursă, 24:40:00
                tuple("S_B", LocalDateTime.of(2026, 11, 26, 7, 15)),        // T2_LV, joi
                tuple("S_B", LocalDateTime.of(2026, 11, 26, 23, 50)));      // T2_NOAPTE, joi
    }
}
