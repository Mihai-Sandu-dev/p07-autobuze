package ro.autobuze.schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Următoarele plecări dintr-o stație (D-018, D-048, D-051). */
@Service
public class NextDepartures {

    /** Lista stației lui {@code stopId} la ora {@code now}; gol dacă stația nu există în feed. */
    public Optional<StationBoard> at(String stopId, LocalDateTime now) {
        throw new UnsupportedOperationException("punctul 8: de scris");
    }

    /** Grupurile pe linie + capăt, cu primele 3 plecări, ordonate după prima plecare. */
    static List<StationBoard.Group> groups(List<Departure> departures, Map<String, LineColors> colors,
            LocalDateTime now) {
        throw new UnsupportedOperationException("punctul 8: de scris");
    }
}
