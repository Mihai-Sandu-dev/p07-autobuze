package ro.autobuze.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Gruparea listei „Următoarele plecări” (D-018, D-051), fără DB. */
class NextDeparturesTest {

    static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 17, 16, 40);

    static Departure dep(String line, String headsign, int directionId, LocalDateTime time) {
        return new Departure(time.toLocalDate(), time, "S", 1, line + "-" + headsign + "-" + time, line, directionId,
                headsign);
    }

    static LocalDateTime today(int h, int m) {
        return NOW.toLocalDate().atTime(h, m);
    }

    @Test
    void groupsByLineAndHeadsignKeepingTheFirstThree() {
        List<Departure> deps = List.of(
                dep("11", "Aumovio", 0, today(16, 51)),
                dep("11", "Aumovio", 0, today(17, 6)),
                dep("11", "Aumovio", 0, today(17, 21)),
                dep("11", "Aumovio", 0, today(17, 36)),
                dep("11", "Aumovio", 0, today(17, 51)));

        List<StationBoard.Group> groups = NextDepartures.groups(deps, Map.of(), NOW);

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).departures()).extracting(StationBoard.Entry::time)
                .containsExactly("16:51", "17:06", "17:21");
    }

    @Test
    void aShortTurnWithAnotherHeadsignHasItsOwnGroup() {
        // D-051: aceeași linie și același direction_id, alt capăt → alt grup
        List<Departure> deps = List.of(
                dep("19", "Marquardt", 0, today(16, 44)),
                dep("19", "Cartier Magnolia", 0, today(16, 50)),
                dep("19", "Marquardt", 0, today(17, 4)));

        List<StationBoard.Group> groups = NextDepartures.groups(deps, Map.of(), NOW);

        assertThat(groups).extracting(StationBoard.Group::line, StationBoard.Group::headsign).containsExactly(
                tuple("19", "Marquardt"), tuple("19", "Cartier Magnolia"));
        assertThat(groups.get(0).departures()).extracting(StationBoard.Entry::time).containsExactly("16:44", "17:04");
    }

    @Test
    void groupsAreOrderedByTheirFirstDeparture() {
        List<Departure> deps = List.of(
                dep("19", "Marquardt", 0, today(16, 44)),
                dep("21", "Marquardt", 1, today(16, 47)),
                dep("11", "Aumovio", 0, today(16, 51)),
                dep("19", "Marquardt", 0, today(17, 4)));

        assertThat(NextDepartures.groups(deps, Map.of(), NOW)).extracting(StationBoard.Group::line)
                .containsExactly("19", "21", "11");
    }

    @Test
    void minutesAndTheirText() {
        List<Departure> deps = List.of(
                dep("19", "Marquardt", 0, today(16, 44)),
                dep("19", "Marquardt", 0, today(17, 47)),
                dep("19", "Marquardt", 0, today(18, 40)));

        List<StationBoard.Entry> entries = NextDepartures.groups(deps, Map.of(), NOW).get(0).departures();

        assertThat(entries).extracting(StationBoard.Entry::minutes, StationBoard.Entry::untilText).containsExactly(
                tuple(4, "în 4 min"), tuple(67, "în 1 h 7 min"), tuple(120, "în 2 h"));
    }

    @Test
    void tomorrowFollowsTheClockDateNotTheServiceDay() {
        LocalDateTime night = LocalDateTime.of(2026, 11, 18, 0, 30);
        // 00:41 e din ziua de serviciu de ieri (oră ≥ 24:00), dar data de pe ceas e azi: fără „mâine”
        Departure afterMidnight = new Departure(LocalDate.of(2026, 11, 17), night.plusMinutes(11), "S", 2, "T1", "11",
                0, "Aumovio");
        Departure nextDay = dep("11", "Aumovio", 0, LocalDateTime.of(2026, 11, 19, 5, 21));

        List<StationBoard.Entry> entries = NextDepartures.groups(List.of(afterMidnight, nextDay), Map.of(), night)
                .get(0).departures();

        assertThat(entries).extracting(StationBoard.Entry::time, StationBoard.Entry::tomorrow)
                .containsExactly(tuple("00:41", false), tuple("05:21", true));
    }

    @Test
    void colorsComeFromTheLineAndDefaultWhenMissing() {
        List<Departure> deps = List.of(dep("19", "Marquardt", 0, today(16, 44)), dep("118", "Gușterița", 0,
                today(16, 55)));

        List<StationBoard.Group> groups = NextDepartures.groups(deps, Map.of("19", new LineColors("F2C500", "000000")),
                NOW);

        assertThat(groups).extracting(StationBoard.Group::color, StationBoard.Group::textColor).containsExactly(
                tuple("F2C500", "000000"), tuple("FFFFFF", "000000"));
    }
}
