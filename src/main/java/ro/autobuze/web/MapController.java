package ro.autobuze.web;

import java.security.Principal;
import java.text.Collator;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import ro.autobuze.schedule.Favorites;
import ro.autobuze.schedule.NextDepartures;
import ro.autobuze.schedule.ServiceCalendar;
import ro.autobuze.schedule.StationBoard;
import ro.autobuze.schedule.StationDirectory;

/**
 * Pagina hărții (DS-autobuze-harta) la „/” și la „/harta” (D-022, D-030) și stațiile ei în JSON
 * (C-003). Pagina își citește stațiile din API în browser; căutarea se face acolo (D-054).
 * Utilizatorul logat vede la „/” pagina „Acasă” (DS-autobuze-acasa, D-022, D-057).
 */
@Controller
class MapController {

    /** Răspunsul lui {@code GET /api/statii} (C-003). */
    record StationList(boolean feedImported, List<StationDirectory.MapStation> stations) {
    }

    /** Răspunsul lui {@code GET /api/statii/aproape} (C-003). */
    record NearList(boolean feedImported, List<StationDirectory.NearStation> stations) {
    }

    /**
     * O favorită pe „Acasă”: grupurile stației (din fiecare se afișează prima plecare) sau, dacă stația
     * nu mai e în versiunea activă, {@code gone} cu ultimul nume cunoscut.
     */
    record HomeStation(String stopId, String name, boolean gone, StationBoard.Station station,
            List<StationBoard.Group> groups) {
    }

    private static final Comparator<HomeStation> BY_NAME =
            Comparator.comparing(HomeStation::name, Collator.getInstance(Locale.forLanguageTag("ro")));

    private final ServiceCalendar calendar;
    private final StationDirectory directory;
    private final NextDepartures next;
    private final Favorites favorites;
    private final Clock clock;

    MapController(ServiceCalendar calendar, StationDirectory directory, NextDepartures next, Favorites favorites,
            Clock clock) {
        this.calendar = calendar;
        this.directory = directory;
        this.next = next;
        this.favorites = favorites;
        this.clock = clock;
    }

    /** „/”: harta pentru vizitator, „Acasă” pentru utilizatorul logat (D-022). */
    @GetMapping("/")
    String root(Principal principal, Model model) {
        if (principal == null) {
            return "harta";
        }
        LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        List<HomeStation> stations = new ArrayList<>();
        for (String key : favorites.keys(principal.getName())) {
            stations.add(next.at(key, now)
                    .map(b -> new HomeStation(key, b.station() == null ? key : b.station().name(), false, b.station(),
                            b.groups()))
                    .orElseGet(() -> new HomeStation(key, favorites.lastKnownName(key), true, null, List.of())));
        }
        stations.sort(BY_NAME);
        model.addAttribute("now", now);
        model.addAttribute("dayLabel", calendar.dayLabel(now.toLocalDate()));
        model.addAttribute("feedImported", calendar.activeFeedVersion().isPresent());
        model.addAttribute("favorite", stations);
        model.addAttribute("puncte", stations.stream().filter(s -> s.station() != null)
                .map(s -> "[" + s.station().lat() + "," + s.station().lon() + "]")
                .collect(Collectors.joining(",", "[", "]")));
        return "acasa";
    }

    /** Harta, pentru toți (D-030). */
    @GetMapping("/harta")
    String page() {
        return "harta";
    }

    @GetMapping("/api/statii")
    @ResponseBody
    StationList stations() {
        Optional<Long> fv = calendar.activeFeedVersion();
        return new StationList(fv.isPresent(), fv.map(directory::all).orElse(List.of()));
    }

    /** 400 pentru coordonate lipsă, care nu sunt numere sau ies din intervalele WGS84. */
    @GetMapping("/api/statii/aproape")
    @ResponseBody
    NearList near(@RequestParam double lat, @RequestParam double lon) {
        if (!(lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lat/lon în afara intervalelor");
        }
        Optional<Long> fv = calendar.activeFeedVersion();
        return new NearList(fv.isPresent(), fv.map(v -> directory.near(v, lat, lon)).orElse(List.of()));
    }
}
