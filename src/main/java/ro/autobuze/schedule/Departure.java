package ro.autobuze.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * O plecare dintr-o stație, cu ora reală de pe ceas (D-047). {@code serviceDate} e ziua de serviciu
 * a cursei: la o oră din feed ≥ 24:00, e ziua dinaintea lui {@code time}.
 */
public record Departure(LocalDate serviceDate, LocalDateTime time, String stopId, int stopSequence, String tripId,
        String routeShortName, Integer directionId, String headsign) {
}
