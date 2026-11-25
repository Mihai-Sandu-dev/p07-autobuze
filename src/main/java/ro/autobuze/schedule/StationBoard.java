package ro.autobuze.schedule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Următoarele plecări dintr-o stație (D-018, D-048, D-051), pentru pagina de stație și pentru
 * JSON-ul din C-002 (D-050). Numele componentelor sunt câmpurile din C-002.
 *
 * @param station null dacă nu există versiune de feed activă
 */
public record StationBoard(Station station, LocalDateTime now, String dayLabel, boolean feedImported,
        List<Group> groups) {

    /** O stație din aplicație: rândurile grupate din stops.txt (D-031). */
    public record Station(String name, List<String> stopIds) {
    }

    /** O linie cu un capăt (D-051), cu primele 3 plecări; culorile sunt hex fără „#”. */
    public record Group(String line, String headsign, String color, String textColor, List<Entry> departures) {
    }

    /**
     * O plecare: ora de pe ceas („HH:mm”), momentul, dacă e mâine (data de pe ceas) și minutele de
     * acum până la ea.
     */
    public record Entry(String time, LocalDateTime at, boolean tomorrow, int minutes) {

        /** „în 4 min”, „în 1 h 7 min”, „în 2 h” (mock-ul DS-autobuze-statie); sub un minut, „acum” (D-052). */
        public String untilText() {
            if (minutes <= 0) {
                return "acum";
            }
            int hours = minutes / 60;
            int rest = minutes % 60;
            if (hours == 0) {
                return "în " + rest + " min";
            }
            return rest == 0 ? "în " + hours + " h" : "în " + hours + " h " + rest + " min";
        }
    }
}
