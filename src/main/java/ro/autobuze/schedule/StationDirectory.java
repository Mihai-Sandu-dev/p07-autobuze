package ro.autobuze.schedule;

import java.util.List;
import org.springframework.stereotype.Repository;

/** Stub: stațiile pentru hartă și „aproape de mine” (C-003). */
@Repository
public class StationDirectory {

    public record MapStation(String stopId, String name, double lat, double lon, List<String> stopIds) {
    }

    public record NearStation(String stopId, String name, double lat, double lon, int distanceM) {
    }

    public List<MapStation> all(long feedVersionId) {
        throw new UnsupportedOperationException("punctul 9");
    }

    public List<NearStation> near(long feedVersionId, double lat, double lon) {
        throw new UnsupportedOperationException("punctul 9");
    }
}
