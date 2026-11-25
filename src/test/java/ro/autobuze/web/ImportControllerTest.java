package ro.autobuze.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Accesul la endpoint-ul de import (C-001): fără token configurat endpoint-ul nu există,
 * iar fără tokenul corect răspunsul e 401. Importul nu se atinge în aceste cazuri.
 */
class ImportControllerTest {

    @Test
    void withoutConfiguredTokenTheEndpointIsNotFound() {
        ImportController controller = new ImportController(null, null, "");

        assertThat(controller.start("Bearer ceva").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(controller.status(null).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void missingOrWrongTokenIsUnauthorized() {
        ImportController controller = new ImportController(null, null, "tokenul-corect");

        assertThat(controller.start(null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(controller.start("Bearer altul").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(controller.start("tokenul-corect").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(controller.status("Bearer altul").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
