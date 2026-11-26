package ro.autobuze.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;

/**
 * Autentificarea (D-011): formular pe /autentificare cu email și parolă, sesiune, BCrypt, roluri USER și
 * ADMIN. Rutele (D-058): public totul, în afară de /favorite/** (login) și /admin/** (ADMIN);
 * /admin/import rămâne doar cu tokenul lui (C-001), fără sesiune și fără CSRF.
 * După autentificare, înapoi la pagina din câmpul {@code inapoi} (D-032).
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/admin/import").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/favorite/**").authenticated()
                        .anyRequest().permitAll())
                .csrf(c -> c.ignoringRequestMatchers("/admin/import"))
                .formLogin(f -> f
                        .loginPage("/autentificare")
                        .loginProcessingUrl("/autentificare")
                        .usernameParameter("email")
                        .passwordParameter("parola")
                        .successHandler((req, res, auth) -> res.sendRedirect(ReturnTo.safe(req.getParameter("inapoi"))))
                        .failureHandler((req, res, e) ->
                                res.sendRedirect(ReturnTo.loginUrl("eroare", req.getParameter("inapoi"))))
                        .permitAll())
                .logout(l -> l.logoutUrl("/iesire").logoutSuccessUrl("/"))
                // Întoarcerea vine din câmpul „inapoi”, nu din cererea salvată
                .requestCache(r -> r.requestCache(new NullRequestCache()))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
