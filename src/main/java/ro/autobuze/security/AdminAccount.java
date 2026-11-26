package ro.autobuze.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Primul admin (D-055): la fiecare pornire, contul cu {@code ADMIN_EMAIL} primește rolul ADMIN și parola din
 * {@code ADMIN_PASSWORD}; dacă nu există, se creează. Înregistrarea e publică, deci un cont făcut înainte
 * cu emailul adminului își pierde parola. Fără variabile, nimic. Parola nu ajunge în log sau în mesaje.
 */
@Component
public class AdminAccount implements ApplicationRunner {

    private static final String UPSERT = """
            insert into users (email, password_hash, role) values (?, ?, 'ADMIN')
            on conflict ((lower(email))) do update set password_hash = excluded.password_hash, role = 'ADMIN'""";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminAccount(JdbcTemplate jdbc, PasswordEncoder encoder,
            @Value("${autobuze.admin.email:}") String email, @Value("${autobuze.admin.password:}") String password) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        sync(email, password);
    }

    /** Aplică D-055 pentru perechea dată; oprește pornirea dacă emailul sau parola nu respectă D-032. */
    public void sync(String adminEmail, String adminPassword) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isEmpty()) {
            return;
        }
        String e = AppUsers.emailProblem(adminEmail);
        if (e != null) {
            throw new IllegalStateException("ADMIN_EMAIL nu e o adresă de email validă.");
        }
        String p = AppUsers.passwordProblem(adminPassword);
        if (p != null) {
            throw new IllegalStateException("ADMIN_PASSWORD nu respectă regulile parolei (D-032): " + p);
        }
        jdbc.update(UPSERT, adminEmail.trim(), encoder.encode(adminPassword));
    }
}
