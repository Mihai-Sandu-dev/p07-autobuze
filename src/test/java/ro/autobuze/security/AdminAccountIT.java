package ro.autobuze.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.autobuze.TestcontainersConfiguration;

/** Primul admin din ADMIN_EMAIL și ADMIN_PASSWORD, la fiecare pornire (D-055). */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AdminAccountIT {

    @Autowired
    AdminAccount admin;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void noUsers() {
        jdbc.execute("truncate users restart identity cascade");
    }

    String role(String email) {
        return jdbc.queryForObject("select role from users where lower(email) = lower(?)", String.class, email);
    }

    String hash(String email) {
        return jdbc.queryForObject("select password_hash from users where lower(email) = lower(?)", String.class, email);
    }

    @Test
    void createsTheAdminWhenTheEmailIsNew() {
        admin.sync("admin@exemplu.ro", "parola-admin-1");
        assertThat(role("admin@exemplu.ro")).isEqualTo("ADMIN");
        assertThat(encoder.matches("parola-admin-1", hash("admin@exemplu.ro"))).isTrue();
    }

    @Test
    void anExistingAccountGetsTheAdminRoleAndTheEnvironmentPassword() {
        // Cineva s-a înregistrat înainte cu emailul adminului (cu alte majuscule): contul trece la admin
        jdbc.update("insert into users (email, password_hash, role) values (?, ?, 'USER')",
                "Admin@Exemplu.ro", encoder.encode("parola-intrusului"));
        admin.sync("admin@exemplu.ro", "parola-admin-1");

        assertThat(jdbc.queryForObject("select count(*) from users", Integer.class)).isEqualTo(1);
        assertThat(role("admin@exemplu.ro")).isEqualTo("ADMIN");
        assertThat(encoder.matches("parola-admin-1", hash("admin@exemplu.ro"))).isTrue();
        assertThat(encoder.matches("parola-intrusului", hash("admin@exemplu.ro"))).isFalse();
    }

    @Test
    void withoutVariablesNoAdminIsCreated() {
        admin.sync("", "");
        admin.sync(null, null);
        admin.sync("admin@exemplu.ro", "");
        assertThat(jdbc.queryForObject("select count(*) from users", Integer.class)).isZero();
    }

    @Test
    void aPasswordAgainstD032StopsTheStartWithoutShowingIt() {
        assertThatThrownBy(() -> admin.sync("admin@exemplu.ro", "scurta"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_PASSWORD")
                .hasMessageNotContaining("scurta");
        assertThatThrownBy(() -> admin.sync("admin@exemplu.ro", "ș".repeat(40)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_PASSWORD");
        assertThat(jdbc.queryForObject("select count(*) from users", Integer.class)).isZero();
    }
}
