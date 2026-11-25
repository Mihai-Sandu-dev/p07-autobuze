package ro.autobuze.schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Tipul de zi (D-014): lucrătoare, sâmbătă, sau duminică și sărbătoare legală. Dă doar eticheta
 * zilei (orarul pe tipuri de zi, media abaterilor); plecările nu îl folosesc (D-015).
 */
public enum DayType {
    LUCRATOARE, SAMBATA, DUMINICA_SARBATOARE;

    /** O sărbătoare legală se tratează ca duminică, și când cade sâmbăta. */
    static DayType of(LocalDate date, boolean holiday) {
        if (holiday || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return DUMINICA_SARBATOARE;
        }
        return date.getDayOfWeek() == DayOfWeek.SATURDAY ? SAMBATA : LUCRATOARE;
    }
}
