package ro.autobuze.web;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import ro.autobuze.schedule.ServiceCalendar;
import ro.autobuze.schedule.StationDirectory;

/**
 * Pagina hărții (DS-autobuze-harta) la „/” și la „/harta” (D-022, D-030) și stațiile ei în JSON
 * (C-003). Pagina își citește stațiile din API în browser; căutarea se face acolo (D-054).
 */
@Controller
class MapController {

    /** Răspunsul lui {@code GET /api/statii} (C-003). */
    record StationList(boolean feedImported, List<StationDirectory.MapStation> stations) {
    }

    /** Răspunsul lui {@code GET /api/statii/aproape} (C-003). */
    record NearList(boolean feedImported, List<StationDirectory.NearStation> stations) {
    }

    private final ServiceCalendar calendar;
    private final StationDirectory directory;

    MapController(ServiceCalendar calendar, StationDirectory directory) {
        this.calendar = calendar;
        this.directory = directory;
    }

    /** Până la autentificare (punctul 10), „/” arată harta tuturor (D-022). */
    @GetMapping({"/", "/harta"})
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
