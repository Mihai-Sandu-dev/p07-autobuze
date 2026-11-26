package ro.autobuze.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ro.autobuze.TestcontainersConfiguration;
import ro.autobuze.gtfs.FeedServer;
import ro.autobuze.gtfs.GtfsImportService;
import ro.autobuze.gtfs.MiniFeed;

/**
 * Rutele și autentificarea (D-011, D-032, D-058), pe mini-feed: paginile publice, favoritele cu login,
 * /admin/** cu ADMIN, /admin/import doar cu token (C-001), întoarcerea pe pagina de origine, antetul.
 */
@SpringBootTest(properties = "autobuze.import.token=test-token")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SecurityIT {

    static final FeedServer FEED = FeedServer.get();
    static final String EMAIL = "ana@exemplu.ro";

    @DynamicPropertySource
    static void feedUrl(DynamicPropertyRegistry registry) {
        registry.add("autobuze.import.feed-url", FEED::feedUrl);
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    GtfsImportService importer;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void miniFeedAndOneUser() {
        jdbc.execute("truncate feed_versions restart identity cascade");
        jdbc.execute("truncate users restart identity cascade");
        FEED.serve(MiniFeed.zip());
        importer.importNow();
        TestUsers.create(jdbc, EMAIL, "USER");
    }

    @Test
    void pagesApiAndStaticFilesArePublic() throws Exception {
        for (String path : new String[] {"/", "/harta", "/statie/S_B", "/api/statii", "/api/statii/S_B/plecari",
            "/api/statii/aproape?lat=45.792&lon=24.152", "/autentificare", "/inregistrare", "/css/app.css",
            "/js/harta.js", "/vendor/leaflet/leaflet.js"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    void favoritesNeedALogin() throws Exception {
        mvc.perform(post("/favorite/S_B").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/autentificare"));
    }

    @Test
    void adminRoutesNeedTheAdminRole() throws Exception {
        mvc.perform(get("/admin/rapoarte"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/autentificare"));
        mvc.perform(get("/admin/rapoarte").with(user(EMAIL).roles("USER")))
                .andExpect(status().isForbidden());
        // ADMIN trece de filtru; pagina nu există încă (punctul 13)
        mvc.perform(get("/admin/rapoarte").with(user("admin@exemplu.ro").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void importKeepsItsTokenWithoutSessionOrCsrf() throws Exception {
        // C-001 neschimbat (D-058): fără token 401, cu token merge, fără CSRF
        mvc.perform(get("/admin/import")).andExpect(status().isUnauthorized());
        mvc.perform(post("/admin/import")).andExpect(status().isUnauthorized());
        mvc.perform(get("/admin/import").header("Authorization", "Bearer test-token")).andExpect(status().isOk());
        // o sesiune de USER nu deschide importul
        mvc.perform(get("/admin/import").with(user(EMAIL).roles("USER"))).andExpect(status().isUnauthorized());
    }

    @Test
    void loginGoesBackToTheOriginPage() throws Exception {
        mvc.perform(post("/autentificare").with(csrf())
                        .param("email", EMAIL).param("parola", TestUsers.PASSWORD).param("inapoi", "/statie/S_B"))
                .andExpect(authenticated().withUsername(EMAIL))
                .andExpect(redirectedUrl("/statie/S_B"));
    }

    @Test
    void emailIsNotCaseSensitiveAtLogin() throws Exception {
        mvc.perform(post("/autentificare").with(csrf())
                        .param("email", "ANA@Exemplu.RO").param("parola", TestUsers.PASSWORD))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void anUnsafeOrMissingOriginGoesHome() throws Exception {
        for (String inapoi : new String[] {"//evil.ro", "https://evil.ro/", ""}) {
            mvc.perform(post("/autentificare").with(csrf())
                            .param("email", EMAIL).param("parola", TestUsers.PASSWORD).param("inapoi", inapoi))
                    .andExpect(redirectedUrl("/"));
        }
    }

    @Test
    void wrongPasswordShowsTheMockMessageAndKeepsTheOrigin() throws Exception {
        mvc.perform(post("/autentificare").with(csrf())
                        .param("email", EMAIL).param("parola", "gresita-123").param("inapoi", "/statie/S_B"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/autentificare?eroare&inapoi=/statie/S_B"));
        mvc.perform(get("/autentificare").param("eroare", "").param("inapoi", "/statie/S_B"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Emailul sau parola nu sunt corecte.")))
                .andExpect(content().string(containsString("name=\"inapoi\" value=\"/statie/S_B\"")));
    }

    @Test
    void loginFormPostsWithCsrfAndTheMockFields() throws Exception {
        mvc.perform(get("/autentificare").param("inapoi", "/statie/S_B"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(">Autentificare</h1>")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("name=\"email\"")))
                .andExpect(content().string(containsString("name=\"parola\"")))
                .andExpect(content().string(containsString("href=\"/inregistrare?inapoi=/statie/S_B\"")));
    }

    @Test
    void headerForAVisitorLinksToLoginWithTheCurrentPage() throws Exception {
        mvc.perform(get("/statie/S_B"))
                .andExpect(content().string(containsString("href=\"/autentificare?inapoi=/statie/S_B\"")))
                .andExpect(content().string(containsString(">Autentificare</a>")))
                .andExpect(content().string(not(containsString("Ieșire"))));
    }

    @Test
    void headerForAUserHasHomeAndAPostLogout() throws Exception {
        mvc.perform(get("/statie/S_B").with(user(EMAIL).roles("USER")))
                .andExpect(content().string(containsString("<a href=\"/\">Acasă</a>")))
                .andExpect(content().string(containsString("action=\"/iesire\" method=\"post\"")))
                .andExpect(content().string(containsString("Ieșire")))
                .andExpect(content().string(not(containsString(">Autentificare</a>"))));
    }

    @Test
    void logoutEndsTheSessionAndGoesToTheMap() throws Exception {
        mvc.perform(post("/iesire").with(csrf()).with(user(EMAIL).roles("USER")))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/"));
    }
}
