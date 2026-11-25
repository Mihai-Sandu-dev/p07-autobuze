package ro.autobuze.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Tipul de zi (D-014): o sărbătoare legală se tratează ca duminică. */
class DayTypeTest {

    @Test
    void weekdaySaturdayAndSunday() {
        assertThat(DayType.of(LocalDate.of(2026, 11, 25), false)).isEqualTo(DayType.LUCRATOARE);
        assertThat(DayType.of(LocalDate.of(2026, 11, 28), false)).isEqualTo(DayType.SAMBATA);
        assertThat(DayType.of(LocalDate.of(2026, 11, 29), false)).isEqualTo(DayType.DUMINICA_SARBATOARE);
    }

    @Test
    void holidayOnAWeekdayCountsAsSunday() {
        // luni 30.11.2026, Sfântul Andrei
        assertThat(DayType.of(LocalDate.of(2026, 11, 30), true)).isEqualTo(DayType.DUMINICA_SARBATOARE);
    }

    @Test
    void holidayOnASaturdayCountsAsSunday() {
        // sâmbătă 15.08.2026, Adormirea Maicii Domnului
        assertThat(DayType.of(LocalDate.of(2026, 8, 15), true)).isEqualTo(DayType.DUMINICA_SARBATOARE);
    }
}
