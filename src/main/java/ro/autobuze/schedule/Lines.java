package ro.autobuze.schedule;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Stub (punctul 11): pagina de linie (D-060). */
@Repository
public class Lines {

    public Optional<LinePage> page(long feedVersionId, String line, LocalDate today) {
        throw new UnsupportedOperationException("punctul 11");
    }
}
