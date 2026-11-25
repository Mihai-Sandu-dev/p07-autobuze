package ro.autobuze.gtfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.sql.DataSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ro.autobuze.gtfs.ImportStatus.State;

/**
 * Importul feed-ului GTFS (D-003, D-019, D-038): descarcă arhiva, îi calculează checksum-ul și,
 * dacă e nouă, o încarcă într-o versiune de feed nouă, într-o singură tranzacție. Fiecare fișier
 * intră printr-un singur COPY, în flux, fără să țină feed-ul în memorie. Versiunea nouă devine
 * activă; rămân doar ea și cea anterioară.
 */
@Service
public class GtfsImportService {

    private static final Logger log = LoggerFactory.getLogger(GtfsImportService.class);

    /** Câte versiuni de feed rămân în DB: cea activă și cea anterioară (D-019). */
    private static final int KEPT_VERSIONS = 2;

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;
    private final TransactionTemplate transaction;
    private final URI feedUrl;
    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final AtomicBoolean running = new AtomicBoolean();
    private volatile ImportStatus status = ImportStatus.idle();

    public GtfsImportService(JdbcTemplate jdbc, DataSource dataSource, TransactionTemplate transaction,
            @Value("${autobuze.import.feed-url}") URI feedUrl) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
        this.transaction = transaction;
        this.feedUrl = feedUrl;
    }

    /** Starea ultimului import. */
    public ImportStatus status() {
        return status;
    }

    /**
     * Pornește importul pe {@code executor} și se întoarce imediat (D-038).
     *
     * @return {@code false} dacă un import rulează deja
     */
    public boolean start(Executor executor) {
        if (!running.compareAndSet(false, true)) {
            return false;
        }
        Instant startedAt = Instant.now();
        status = ImportStatus.running(startedAt);
        try {
            executor.execute(() -> {
                try {
                    run(startedAt);
                } finally {
                    running.set(false);
                }
            });
        } catch (RuntimeException e) {
            running.set(false);
            status = ImportStatus.finished(State.FAILED, startedAt, null, null, null, null, e.getMessage());
            throw e;
        }
        return true;
    }

    /** Importul pe firul curent, până la final. */
    public ImportStatus importNow() {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("un import rulează deja");
        }
        try {
            Instant startedAt = Instant.now();
            status = ImportStatus.running(startedAt);
            run(startedAt);
            return status;
        } finally {
            running.set(false);
        }
    }

    private void run(Instant startedAt) {
        Path zip = null;
        String checksum = null;
        String sourceUrl = null;
        try {
            zip = Files.createTempFile("gtfs-", ".zip");
            sourceUrl = download(zip);
            checksum = sha256(zip);
            String active = jdbc.query("select checksum from feed_versions where active",
                    rs -> rs.next() ? rs.getString(1) : null);
            if (checksum.equals(active)) {
                // D-019: același feed ca versiunea activă; se notează doar verificarea
                log.info("Import GTFS: feed neschimbat (checksum {}), fără versiune nouă", checksum);
                status = ImportStatus.finished(State.UNCHANGED, startedAt, checksum, sourceUrl, null, null, null);
                return;
            }
            Path file = zip;
            String sum = checksum;
            String url = sourceUrl;
            Loaded loaded = transaction.execute(tx -> load(file, sum, url));
            status = ImportStatus.finished(State.DONE, startedAt, checksum, sourceUrl, loaded.feedVersionId(),
                    loaded.rows(), null);
            log.info("Import GTFS: versiunea {} activă (checksum {}), rânduri {}, în {} ms",
                    loaded.feedVersionId(), checksum, loaded.rows(), status.durationMs());
        } catch (Exception e) {
            String message = e instanceof GtfsException ? e.getMessage() : e.toString();
            status = ImportStatus.finished(State.FAILED, startedAt, checksum, sourceUrl, null, null, message);
            log.error("Import GTFS eșuat: {}", message, e);
        } finally {
            if (zip != null) {
                try {
                    Files.deleteIfExists(zip);
                } catch (IOException e) {
                    log.warn("Import GTFS: fișierul temporar {} nu s-a șters", zip, e);
                }
            }
        }
    }

    /** Descarcă feed-ul în {@code target}; întoarce adresa finală, după redirect. */
    private String download(Path target) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(feedUrl).timeout(Duration.ofMinutes(5)).GET().build();
        HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream body = response.body()) {
            if (response.statusCode() != 200) {
                throw new GtfsException("descărcarea feed-ului: HTTP " + response.statusCode() + " de la "
                        + response.uri());
            }
            Files.copy(body, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return response.uri().toString();
    }

    private static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new DigestInputStream(Files.newInputStream(file), digest)) {
                in.transferTo(java.io.OutputStream.nullOutputStream());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Totul în tranzacția curentă: versiunea nouă, datele ei, activarea și ștergerea celor vechi. */
    private Loaded load(Path zipFile, String checksum, String sourceUrl) {
        long feedVersionId = jdbc.queryForObject(
                "insert into feed_versions (checksum, source_url) values (?, ?) returning id",
                Long.class, checksum, sourceUrl);
        Map<String, Long> rows = new LinkedHashMap<>();
        // Conexiunea tranzacției curente: COPY intră în aceeași tranzacție ca restul
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            if (zip.getEntry(GtfsTable.CALENDAR.file()) == null
                    && zip.getEntry(GtfsTable.CALENDAR_DATES.file()) == null) {
                throw new GtfsException("lipsesc și " + GtfsTable.CALENDAR.file() + " și "
                        + GtfsTable.CALENDAR_DATES.file() + "; trebuie cel puțin unul");
            }
            PGConnection pg = connection.unwrap(PGConnection.class);
            for (GtfsTable table : GtfsTable.values()) {
                ZipEntry entry = zip.getEntry(table.file());
                if (entry == null) {
                    if (table.required()) {
                        throw new GtfsException("lipsește fișierul obligatoriu " + table.file());
                    }
                    rows.put(table.table(), 0L);
                    continue;
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    rows.put(table.table(), copy(pg, table, feedVersionId, in));
                }
            }
        } catch (IOException e) {
            throw new GtfsException("arhiva feed-ului nu se poate citi (" + e.getMessage() + ")", e);
        } catch (SQLException e) {
            throw new GtfsException("încărcarea în DB a eșuat (" + e.getMessage() + ")", e);
        }

        jdbc.update("update feed_versions set active = false where active");
        jdbc.update("update feed_versions set active = true where id = ?", feedVersionId);
        // D-019: rămân versiunea activă și cea anterioară; tabelele GTFS se șterg în cascadă (D-044)
        jdbc.update("delete from feed_versions where id not in (select id from feed_versions order by id desc limit ?)",
                KEPT_VERSIONS);
        for (GtfsTable table : GtfsTable.values()) {
            jdbc.execute("analyze " + table.table());
        }
        return new Loaded(feedVersionId, rows);
    }

    /** Un fișier → un singur {@code COPY ... FROM STDIN}, rând cu rând în flux, nu un INSERT pe rând. */
    private static long copy(PGConnection pg, GtfsTable table, long feedVersionId, InputStream in)
            throws SQLException, IOException {
        String sql = "copy " + table.table() + " (feed_version_id, " + String.join(", ", table.dbColumns())
                + ") from stdin with (format csv)";
        PGCopyOutputStream out = new PGCopyOutputStream(pg, sql, 1 << 16);
        String version = Long.toString(feedVersionId);
        try {
            // POSTGRESQL_CSV: null → câmp gol (NULL în DB), "" → text gol
            Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.POSTGRESQL_CSV);
            long count = table.read(new InputStreamReader(in, StandardCharsets.UTF_8), values -> {
                try {
                    printer.print(version);
                    printer.printRecord(values);
                } catch (IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            });
            printer.flush();
            out.endCopy();
            return count;
        } catch (RuntimeException | IOException | SQLException e) {
            if (out.isActive()) {
                out.cancelCopy();
            }
            throw e;
        }
    }

    private record Loaded(long feedVersionId, Map<String, Long> rows) {
    }
}
