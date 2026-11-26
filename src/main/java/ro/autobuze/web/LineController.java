package ro.autobuze.web;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import ro.autobuze.schedule.LinePage;
import ro.autobuze.schedule.Lines;
import ro.autobuze.schedule.ServiceCalendar;

/**
 * Pagina de linie (DS-autobuze-linie, D-060), randată pe server (D-050); traseele și stațiile pentru hartă
 * vin în pagină ca liste de coordonate (harta-mini.js). 404 pentru o linie care nu e în versiunea activă.
 */
@Controller
class LineController {

    private final Lines lines;
    private final ServiceCalendar calendar;
    private final Clock clock;

    LineController(Lines lines, ServiceCalendar calendar, Clock clock) {
        this.lines = lines;
        this.calendar = calendar;
        this.clock = clock;
    }

    @GetMapping("/linie/{line}")
    String page(@PathVariable String line, Model model) {
        model.addAttribute("line", line);
        Optional<Long> fv = calendar.activeFeedVersion();
        model.addAttribute("feedImported", fv.isPresent());
        if (fv.isEmpty()) {
            return "linie";
        }
        LinePage page = lines.page(fv.get(), line, LocalDate.now(clock))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("page", page);
        model.addAttribute("trasee", page.directions().stream()
                .map(d -> points(d.shape())).collect(Collectors.joining(",", "[", "]")));
        Set<String> stops = new LinkedHashSet<>();
        page.directions().forEach(d -> d.stops().forEach(s -> stops.add(point(s.lat(), s.lon()))));
        model.addAttribute("statii", "[" + String.join(",", stops) + "]");
        return "linie";
    }

    private static String points(List<double[]> shape) {
        return shape.stream().map(p -> point(p[0], p[1])).collect(Collectors.joining(",", "[", "]"));
    }

    /** [lat,lon], rotunjite la 5 zecimale (~1 m), ca pagina să rămână mică. */
    private static String point(double lat, double lon) {
        return "[" + Math.round(lat * 1e5) / 1e5 + "," + Math.round(lon * 1e5) / 1e5 + "]";
    }
}
