package ro.autobuze.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.autobuze.gtfs.GtfsImportService;
import ro.autobuze.gtfs.ImportStatus;

/**
 * Endpoint-ul de import (C-001, D-038), apelat de workflow-ul zilnic din GitHub Actions.
 * Protejat cu tokenul din {@code IMPORT_TOKEN}; fără token configurat, endpoint-ul nu există.
 */
@RestController
@RequestMapping("/admin/import")
class ImportController {

    private final GtfsImportService importer;
    private final TaskExecutor executor;
    private final byte[] expected;

    ImportController(GtfsImportService importer, @Qualifier("applicationTaskExecutor") TaskExecutor executor,
            @Value("${autobuze.import.token:}") String token) {
        this.importer = importer;
        this.executor = executor;
        this.expected = token.isBlank() ? null : ("Bearer " + token).getBytes(StandardCharsets.UTF_8);
    }

    /** Pornește importul și răspunde imediat: 202, sau 409 dacă rulează deja. */
    @PostMapping
    ResponseEntity<ImportStatus> start(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
            String authorization) {
        HttpStatus denied = denied(authorization);
        if (denied != null) {
            return ResponseEntity.status(denied).build();
        }
        boolean started = importer.start(executor);
        return ResponseEntity.status(started ? HttpStatus.ACCEPTED : HttpStatus.CONFLICT).body(importer.status());
    }

    /** Starea ultimului import. */
    @GetMapping
    ResponseEntity<ImportStatus> status(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
            String authorization) {
        HttpStatus denied = denied(authorization);
        if (denied != null) {
            return ResponseEntity.status(denied).build();
        }
        return ResponseEntity.ok(importer.status());
    }

    /** 404 fără token configurat, 401 la token lipsă sau greșit; null dacă e permis. */
    private HttpStatus denied(String authorization) {
        if (expected == null) {
            return HttpStatus.NOT_FOUND;
        }
        // comparație în timp constant, ca tokenul să nu se poată ghici după durata răspunsului
        if (authorization == null
                || !MessageDigest.isEqual(expected, authorization.getBytes(StandardCharsets.UTF_8))) {
            return HttpStatus.UNAUTHORIZED;
        }
        return null;
    }
}
