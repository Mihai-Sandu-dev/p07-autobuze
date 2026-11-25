package ro.autobuze.gtfs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Conversia valorilor din feed: orele în secunde (D-043) și datele GTFS. */
class GtfsValuesTest {

    @Test
    void timeAfterMidnightBecomesSecondsOfTheServiceDay() {
        assertThat(GtfsValues.seconds("25:10:00")).isEqualTo(90600);
        assertThat(GtfsValues.seconds("23:50:00")).isEqualTo(85800);
        assertThat(GtfsValues.seconds("00:00:00")).isZero();
    }

    @Test
    void hourMayHaveOneDigitAndSurroundingSpaces() {
        assertThat(GtfsValues.seconds("7:05:00")).isEqualTo(25500);
        assertThat(GtfsValues.seconds(" 07:05:00 ")).isEqualTo(25500);
    }

    @Test
    void emptyTimeIsNull() {
        assertThat(GtfsValues.seconds("")).isNull();
        assertThat(GtfsValues.seconds(null)).isNull();
    }

    @Test
    void malformedTimeIsRejected() {
        assertThatThrownBy(() -> GtfsValues.seconds("ab:cd")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GtfsValues.seconds("07:60:00")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GtfsValues.seconds("07:05")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gtfsDateBecomesIsoDate() {
        assertThat(GtfsValues.date("20261201")).isEqualTo("2026-12-01");
        assertThat(GtfsValues.date("")).isNull();
    }

    @Test
    void malformedDateIsRejected() {
        assertThatThrownBy(() -> GtfsValues.date("2026-12-01")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GtfsValues.date("20261301")).isInstanceOf(IllegalArgumentException.class);
    }
}
