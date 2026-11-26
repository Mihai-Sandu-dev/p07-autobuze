package ro.autobuze.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

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
 * Stațiile pentru hartă și „aproape de mine” (C-003, D-031, D-034), pe mini-feed, cu două rânduri
 * în plus inserate aici: „Piața Mică” la ~55 m de S_A (aceeași stație) și „Turnișor” la ~2 km de
 * S_TURNISOR (altă stație, același nume).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class StationDirectoryIT {

    static final FeedServer FEED = FeedServer.get();

    @DynamicPropertySource
    static void feedUrl(DynamicPropertyRegistry registry) {
        registry.add("autobuze.import.feed-url", FEED::feedUrl);
    }

    @Autowired
    StationDirectory directory;

    @Autowired
    GtfsImportService importer;

    @Autowired
    JdbcTemplate jdbc;

    long fv;

    @BeforeEach
    void importMiniFeedWithExtraStops() {
        jdbc.execute("truncate feed_versions restart identity cascade");
        FEED.serve(MiniFeed.zip());
        importer.importNow();
        fv = jdbc.queryForObject("select id from feed_versions where active", Long.class);
        jdbc.update("insert into stops (feed_version_id, stop_id, stop_name, stop_lat, stop_lon) values (?, ?, ?, ?, ?)",
                fv, "S_A2", "Piața Mică", 45.792500, 24.152000);
        jdbc.update("insert into stops (feed_version_id, stop_id, stop_name, stop_lat, stop_lon) values (?, ?, ?, ?, ?)",
                fv, "S_TURNISOR_2", "Turnișor", 45.818000, 24.120000);
    }

    @Test
    void sameNameWithin150mIsOneStationAndFartherIsAnother() {
        List<StationDirectory.MapStation> all = directory.all(fv);

        assertThat(all).filteredOn(s -> s.name().equals("Piața Mică"))
                .singleElement()
                .satisfies(s -> assertThat(s.stopIds()).containsExactly("S_A", "S_A2"));
        assertThat(all).filteredOn(s -> s.name().equals("Turnișor"))
                .extracting(StationDirectory.MapStation::stopIds)
                .containsExactlyInAnyOrder(List.of("S_TURNISOR"), List.of("S_TURNISOR_2"));
        // 8 rânduri în mini-feed + 2 inserate; „Piața Mică” le unește pe 2
        assertThat(all).hasSize(9);
    }

    @Test
    void stationIdIsTheSmallestStopIdAndThePositionIsTheMean() {
        StationDirectory.MapStation piata = directory.all(fv).stream()
                .filter(s -> s.name().equals("Piața Mică")).findFirst().orElseThrow();
        assertThat(piata.stopId()).isEqualTo("S_A");
        assertThat(piata.lat()).isCloseTo(45.792250, within(1e-9));
        assertThat(piata.lon()).isCloseTo(24.152000, within(1e-9));
    }

    @Test
    void stationsAreOrderedByNameTheRomanianWay() {
        assertThat(directory.all(fv)).extracting(StationDirectory.MapStation::name).containsExactly(
                "Bulevardul Victoriei", "Gară", "Hornbach", "Piața Cibin, peron 2", "Piața Mică",
                "Strada Noaptea", "Turnișor", "Turnișor", "Turnul Sfatului");
    }

    @Test
    void nearReturnsTheClosestThreeStationsCountingEachStationOnce() {
        // Din S_A: S_A (0 m), S_A2 (~55 m, aceeași stație), S_B (~100 m), S_C (~200 m), S_CIBIN (~985 m)
        List<StationDirectory.NearStation> near = directory.near(fv, 45.792000, 24.152000);

        assertThat(near).extracting(StationDirectory.NearStation::name)
                .containsExactly("Piața Mică", "Turnul Sfatului", "Bulevardul Victoriei");
        assertThat(near).extracting(StationDirectory.NearStation::stopId).containsExactly("S_A", "S_B", "S_C");
        assertThat(near.get(0).distanceM()).isZero();
        assertThat(near.get(1).distanceM()).isBetween(95, 105);
        assertThat(near.get(2).distanceM()).isBetween(195, 205);
    }

    @Test
    void nearIsEmptyWhenNoStationIsWithin1Km() {
        assertThat(directory.near(fv, 45.750000, 24.250000)).isEmpty();
    }
}
