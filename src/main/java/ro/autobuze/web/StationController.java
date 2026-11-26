package ro.autobuze.web;

import java.security.Principal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import ro.autobuze.schedule.Favorites;
import ro.autobuze.schedule.NextDepartures;
import ro.autobuze.schedule.StationBoard;

/**
 * Pagina de stație (DS-autobuze-statie), randată pe server, și aceleași date în JSON (C-002, D-050).
 * Ora curentă e cea de pe ceas, în Europe/Bucharest (D-047). Pentru utilizatorul logat, butonul de
 * favorită și, după a 11-a, mesajul limitei (D-057).
 */
@Controller
class StationController {

    private final NextDepartures next;
    private final Favorites favorites;
    private final Clock clock;

    StationController(NextDepartures next, Favorites favorites, Clock clock) {
        this.next = next;
        this.favorites = favorites;
        this.clock = clock;
    }

    @GetMapping("/statie/{stopId}")
    String page(@PathVariable String stopId, @RequestParam(required = false) String limita, Principal principal,
            Model model) {
        StationBoard board = board(stopId);
        model.addAttribute("board", board);
        model.addAttribute("stopId", stopId);
        model.addAttribute("favorita", principal != null && board.station() != null
                && favorites.any(principal.getName(), board.station().stopIds()));
        model.addAttribute("limita", principal != null && limita != null);
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
