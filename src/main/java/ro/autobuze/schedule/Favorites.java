package ro.autobuze.schedule;

import java.util.Collection;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Stațiile favorite ale unui utilizator (D-020, D-044): cheia stabilă a unui rând al stației
 * ({@code stop_key} = {@code stop_id}, Q-019), fără legătură cu o versiune de feed; cel mult 10 (D-057).
 * Utilizatorul e cel autentificat, după email.
 */
@Repository
public class Favorites {

    public static final int LIMIT = 10;

    /** Rezultatul unei adăugări. */
    public enum Added { ADDED, ALREADY, LIMIT }

    private static final String USER = "(select id from users where lower(email) = lower(:email))";

    private static final String ADD = """
            insert into favorites (user_id, stop_key)
            select u.id, :key from users u
            where lower(u.email) = lower(:email)
              and (select count(*) from favorites f where f.user_id = u.id) < :limit
            on conflict (user_id, stop_key) do nothing""";

    private final NamedParameterJdbcTemplate jdbc;

    public Favorites(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Cheile favoritelor, în ordinea adăugării. */
    public List<String> keys(String email) {
        return jdbc.queryForList("select stop_key from favorites where user_id = " + USER + " order by id",
                new MapSqlParameterSource("email", email), String.class);
    }

    /** Dacă vreuna dintre cheile stației e la favorite. */
    public boolean any(String email, Collection<String> keys) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "select exists (select 1 from favorites where user_id = " + USER + " and stop_key in (:keys))",
                new MapSqlParameterSource("email", email).addValue("keys", keys), Boolean.class));
    }

    /** Adaugă cheia; nimic dacă e deja acolo sau dacă utilizatorul are deja 10 (D-057). */
    public Added add(String email, String key) {
        int rows = jdbc.update(ADD, new MapSqlParameterSource("email", email).addValue("key", key)
                .addValue("limit", LIMIT));
        if (rows == 1) {
            return Added.ADDED;
        }
        return any(email, List.of(key)) ? Added.ALREADY : Added.LIMIT;
    }

    public void remove(String email, Collection<String> keys) {
        jdbc.update("delete from favorites where user_id = " + USER + " and stop_key in (:keys)",
                new MapSqlParameterSource("email", email).addValue("keys", keys));
    }

    /** Ultimul nume cunoscut al unei chei, din orice versiune de feed păstrată; altfel cheia. */
    public String lastKnownName(String key) {
        List<String> names = jdbc.queryForList(
                "select stop_name from stops where stop_id = :key order by feed_version_id desc limit 1",
                new MapSqlParameterSource("key", key), String.class);
        return names.isEmpty() ? key : names.get(0);
    }
}
