package ro.autobuze.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Conturi de test scrise direct în {@code users}, cu parola în BCrypt (D-011). */
final class TestUsers {

    static final String PASSWORD = "parola-de-test";

    private TestUsers() {
    }

    static void create(JdbcTemplate jdbc, String email, String role) {
        jdbc.update("insert into users (email, password_hash, role) values (?, ?, ?)",
                email, new BCryptPasswordEncoder().encode(PASSWORD), role);
    }

    static void favorite(JdbcTemplate jdbc, String email, String stopKey) {
        jdbc.update("insert into favorites (user_id, stop_key) select id, ? from users where lower(email) = lower(?)",
                stopKey, email);
    }
}
