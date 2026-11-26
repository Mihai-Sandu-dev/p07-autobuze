package ro.autobuze.schedule;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Pagina de linie (DS-autobuze-linie, D-060): linia, culorile etichetei și, pentru fiecare sens (linie +
 * capăt, D-051), stațiile și shape-ul traseului celui mai des, plus orarul din prima stație pe trei zile.
 */
public record LinePage(String line, String longName, LineColors colors, List<Direction> directions) {

    /** Un sens: capătul, stațiile traseului principal, shape-ul ([lat, lon]) și orarul. */
    public record Direction(String headsign, List<Stop> stops, List<double[]> shape, Timetable timetable,
            boolean hasOtherRoute) {
    }

    public record Stop(String stopId, String name, double lat, double lon) {
    }

    /** Orarul pe rânduri de oră (ca pe orarele din stații): o coloană pe fiecare zi. */
    public record Timetable(List<Day> days, List<Hour> hours) {

        /** Dacă ziua {@code index} nu are nicio cursă („Nu circulă”). */
        public boolean emptyDay(int index) {
            return hours.stream().allMatch(h -> h.perDay().get(index).isEmpty());
        }
    }

    /** O coloană: „Lucrătoare”, „Sâmbătă” sau „Duminică”, cu data din care vine orarul. */
    public record Day(String label, LocalDate date, boolean withWeekday) {

        private static final DateTimeFormatter WEEKDAY = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.of("ro"));
        private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM", Locale.of("ro"));

        /** „miercuri, 25 noiembrie” pentru zilele lucrătoare; „28 noiembrie” pentru sâmbătă și duminică. */
        public String dateText() {
            return date.format(withWeekday ? WEEKDAY : DATE);
        }
    }

    /** Un rând de oră („05”, … „23”, apoi „00” după miezul nopții) cu minutele fiecărei zile. */
    public record Hour(String label, List<List<Minute>> perDay) {
    }

    /** Un minut de plecare; {@code otherRoute}: cursa merge pe alt traseu decât cel din listă. */
    public record Minute(String minute, boolean otherRoute) {
    }
}
