package ro.autobuze.gtfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Mini-feed-ul de test din {@code src/test/resources/gtfs/mini}, arhivat la cerere (scenariile:
 * {@link GtfsImportIT}). Variantele pentru teste înlocuiesc sau scot câte un fișier.
 */
public final class MiniFeed {

    static final List<String> FILES = List.of("agency.txt", "stops.txt", "routes.txt", "calendar.txt",
            "calendar_dates.txt", "shapes.txt", "trips.txt", "stop_times.txt");

    private MiniFeed() {
    }

    /** Conținutul unui fișier din mini-feed. */
    static String file(String name) {
        try (InputStream in = MiniFeed.class.getResourceAsStream("/gtfs/mini/" + name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Arhiva mini-feed-ului, neschimbat. */
    public static byte[] zip() {
        return zip(Map.of());
    }

    /** Arhiva cu fișierele schimbate: nume → conținut nou, sau {@code null} ca să lipsească. */
    static byte[] zip(Map<String, String> changes) {
        Map<String, String> files = new LinkedHashMap<>();
        FILES.forEach(name -> files.put(name, file(name)));
        files.putAll(changes);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, String> f : files.entrySet()) {
                if (f.getValue() == null) {
                    continue;
                }
                ZipEntry entry = new ZipEntry(f.getKey());
                entry.setTime(0); // aceeași arhivă, același checksum, la fiecare apel
                zip.putNextEntry(entry);
                zip.write(f.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }
}
