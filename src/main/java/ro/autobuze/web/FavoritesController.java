package ro.autobuze.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import ro.autobuze.schedule.Favorites;
import ro.autobuze.schedule.ServiceCalendar;
import ro.autobuze.schedule.StationBoard;
import ro.autobuze.schedule.Stations;
import ro.autobuze.security.ReturnTo;

/**
 * Butonul de favorită de pe pagina de stație (DS-autobuze-statie), cu formulare POST și CSRF (D-058).
 * Se salvează primul {@code stop_id} al stației (D-044); cel mult 10 (D-057).
 */
@Controller
@RequestMapping("/favorite")
class FavoritesController {

    private final Favorites favorites;
    private final ServiceCalendar calendar;
    private final Stations stations;

    FavoritesController(Favorites favorites, ServiceCalendar calendar, Stations stations) {
        this.favorites = favorites;
        this.calendar = calendar;
        this.stations = stations;
    }

    @PostMapping("/{stopId}")
    String add(@PathVariable String stopId, Principal principal) {
        StationBoard.Station station = calendar.activeFeedVersion().flatMap(fv -> stations.of(fv, stopId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Favorites.Added result = favorites.add(principal.getName(), station.stopIds().get(0));
        return "redirect:" + stationUrl(stopId) + (result == Favorites.Added.LIMIT ? "?limita" : "");
    }

    /** Șterge stația (oricare dintre rândurile ei) și cheia primită, chiar dacă stația nu mai e în orar. */
    @PostMapping("/{stopId}/sterge")
    String remove(@PathVariable String stopId, @RequestParam(required = false) String inapoi, Principal principal) {
        List<String> keys = new ArrayList<>(List.of(stopId));
        calendar.activeFeedVersion().flatMap(fv -> stations.of(fv, stopId)).ifPresent(s -> keys.addAll(s.stopIds()));
        favorites.remove(principal.getName(), keys);
        return "redirect:" + (inapoi == null ? stationUrl(stopId) : ReturnTo.safe(inapoi));
    }

    private static String stationUrl(String stopId) {
        return UriComponentsBuilder.fromPath("/statie/{id}").buildAndExpand(stopId).encode().toUriString();
    }
}
