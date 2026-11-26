package ro.autobuze.security;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Conturile din {@code users} (D-011): autentificarea după email, fără diferență de majuscule, și
 * înregistrarea cu regulile din D-032. Textele sunt cele din mock-ul DS-autobuze-inregistrare.
 */
@Service
public class AppUsers implements UserDetailsService {

    static final int MIN_CHARS = 8;
    static final int MAX_BYTES = 72;
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    public AppUsers(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        List<UserDetails> found = jdbc.query(
                "select email, password_hash, role from users where lower(email) = lower(?)",
                (rs, n) -> User.withUsername(rs.getString(1)).password(rs.getString(2)).roles(rs.getString(3)).build(),
                email == null ? "" : email.trim());
        if (found.isEmpty()) {
            throw new UsernameNotFoundException("cont inexistent");
        }
        return found.get(0);
    }

    /** Ce nu e în regulă cu parola (D-032), sau null. Textul nu conține parola. */
    public static String passwordProblem(String password) {
        if (password == null || password.isEmpty()) {
            return "Scrie parola.";
        }
        if (password.codePointCount(0, password.length()) < MIN_CHARS) {
            return "Parola trebuie să aibă cel puțin 8 caractere.";
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            return "Parola e prea lungă. Folosește mai puține caractere (diacriticele ocupă mai mult).";
        }
        return null;
    }

    /** Ce nu e în regulă cu emailul, sau null. */
    public static String emailProblem(String email) {
        if (email == null || email.isBlank()) {
            return "Scrie adresa de email.";
        }
        if (email.length() > 254 || !EMAIL.matcher(email.trim()).matches()) {
            return "Scrie o adresă de email validă, de forma nume@exemplu.ro.";
        }
        return null;
    }

    /**
     * Creează un cont USER. Întoarce erorile pe câmpuri ({@code email}, {@code parola}, {@code parola2}) și
     * {@code existent} pentru un email care are deja cont; gol dacă s-a creat.
     */
    public Map<String, String> register(String email, String password, String repeat) {
        Map<String, String> errors = new LinkedHashMap<>();
        String e = emailProblem(email);
        if (e != null) {
            errors.put("email", e);
        }
        String p = passwordProblem(password);
        if (p != null) {
            errors.put("parola", p);
        }
        if (repeat == null || repeat.isEmpty()) {
            errors.put("parola2", "Scrie parola încă o dată.");
        } else if (p == null && !repeat.equals(password)) {
            errors.put("parola2", "Parolele nu sunt la fel.");
        }
        if (!errors.isEmpty()) {
            return errors;
        }
        try {
            jdbc.update("insert into users (email, password_hash, role) values (?, ?, 'USER')",
                    email.trim(), encoder.encode(password));
        } catch (DuplicateKeyException exists) {
            errors.put("existent", "Există deja un cont cu acest email.");
        }
        return errors;
    }
}
