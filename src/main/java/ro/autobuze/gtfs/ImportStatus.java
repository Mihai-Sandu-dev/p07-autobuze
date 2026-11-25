package ro.autobuze.gtfs;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Starea ultimului import, cum o dă endpoint-ul (C-001). Se ține doar în memorie: la repornirea
 * aplicației revine la {@code IDLE}.
 */
public record ImportStatus(State state, String checksum, String sourceUrl, Long feedVersionId,
        Map<String, Long> rows, Instant startedAt, Instant finishedAt, Long durationMs, String message) {

    public enum State { IDLE, RUNNING, DONE, UNCHANGED, FAILED }

    static ImportStatus idle() {
        return new ImportStatus(State.IDLE, null, null, null, null, null, null, null, null);
    }

    static ImportStatus running(Instant startedAt) {
        return new ImportStatus(State.RUNNING, null, null, null, null, startedAt, null, null, null);
    }

    /** Starea finală a unui import pornit la {@code startedAt}. */
    static ImportStatus finished(State state, Instant startedAt, String checksum, String sourceUrl,
            Long feedVersionId, Map<String, Long> rows, String message) {
        Instant now = Instant.now();
        return new ImportStatus(state, checksum, sourceUrl, feedVersionId, rows, startedAt, now,
                Duration.between(startedAt, now).toMillis(), message);
    }
}
