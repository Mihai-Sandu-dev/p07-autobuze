package ro.autobuze.web;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import ro.autobuze.schedule.NextDepartures;
import ro.autobuze.schedule.StationBoard;

/**
 * Pagina de stație (DS-autobuze-statie), randată pe server, și aceleași date în JSON (C-002, D-050).
 * Ora curentă e cea de pe ceas, în Europe/Bucharest (D-047).
 */
@Controller
class StationController {

    private final NextDepartures next;
    private final Clock clock;

    StationController(NextDepartures next, Clock clock) {
        this.next = next;
        this.clock = clock;
    }

    @GetMapping("/statie/{stopId}")
    String page(@PathVariable String stopId, Model model) {
        model.addAttribute("board", board(stopId));
        return "statie";
    }

    @GetMapping("/api/statii/{stopId}/plecari")
    @ResponseBody
    StationBoard json(@PathVariable String stopId) {
        return board(stopId);
    }

    /** 404 pentru un stop_id care nu e în versiunea de feed activă. */
    private StationBoard board(String stopId) {
        LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        return next.at(stopId, now).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
