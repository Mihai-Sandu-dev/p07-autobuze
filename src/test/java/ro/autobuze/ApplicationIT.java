package ro.autobuze;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/** Aplicația pornește pe PostGIS și Flyway aplică prima migrare. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ApplicationIT {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void flywayAppliesTheFirstMigration() {
        Integer applied = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where version = '1' and success", Integer.class);
        assertThat(applied).isEqualTo(1);
    }
}
