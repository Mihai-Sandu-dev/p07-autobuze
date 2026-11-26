package ro.autobuze.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import ro.autobuze.TestcontainersConfiguration;
import ro.autobuze.gtfs.FeedServer;
import ro.autobuze.gtfs.GtfsImportService;
import ro.autobuze.gtfs.MiniFeed;

/**
 * Favoritele de pe pagina de stație (DS-autobuze-statie, D-020, D-044): adăugare, ștergere, CSRF,
 * cel mult 10 pe utilizator (D-057), pe mini-feed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class FavoritesIT {

    static final FeedServer FEED = FeedServer.get();
    static final String EMAIL = "ana@exemplu.ro";
    static final RequestPostProcessor ANA = user(EMAIL).roles("USER");

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

    List<String> keys() {
        return jdbc.queryForList("select stop_key from favorites order by stop_key", String.class);
    }

    @Test
    void visitorSeesTheLoginTextFromTheMock() throws Exception {
        mvc.perform(get("/statie/S_B"))
                .andExpect(content().string(containsString("Ai văzut un autobuz întârziat?")))
                .andExpect(content().string(containsString(
                        "ca să raportezi o abatere sau să salvezi stația la favorite.")))
                .andExpect(content().string(not(containsString("Adaugă la favorite"))));
    }

    @Test
    void addSavesTheStationAndThePageSaysSo() throws Exception {
        mvc.perform(get("/statie/S_B").with(ANA))
                .andExpect(content().string(containsString("action=\"/favorite/S_B\" method=\"post\"")))
                .andExpect(content().string(containsString("aria-pressed=\"false\"")))
                .andExpect(content().string(containsString("Adaugă la favorite")));

        mvc.perform(post("/favorite/S_B").with(csrf()).with(ANA))
                .andExpect(redirectedUrl("/statie/S_B"));
        assertThat(keys()).containsExactly("S_B");

        mvc.perform(get("/statie/S_B").with(ANA))
                .andExpect(content().string(containsString("action=\"/favorite/S_B/sterge\" method=\"post\"")))
                .andExpect(content().string(containsString("aria-pressed=\"true\"")))
                .andExpect(content().string(containsString("În favorite")));
    }

    @Test
    void addingTwiceKeepsOneRow() throws Exception {
        mvc.perform(post("/favorite/S_B").with(csrf()).with(ANA));
        mvc.perform(post("/favorite/S_B").with(csrf()).with(ANA)).andExpect(redirectedUrl("/statie/S_B"));
        assertThat(keys()).containsExactly("S_B");
    }

    @Test
    void removeDeletesTheStation() throws Exception {
        TestUsers.favorite(jdbc, EMAIL, "S_B");
        mvc.perform(post("/favorite/S_B/sterge").with(csrf()).with(ANA))
                .andExpect(redirectedUrl("/statie/S_B"));
        assertThat(keys()).isEmpty();
    }

    @Test
    void removeGoesBackToASafeOrigin() throws Exception {
        // Din „Acasă”, pentru o stație care nu mai e în orar
        TestUsers.favorite(jdbc, EMAIL, "DISPARUTA");
        mvc.perform(post("/favorite/DISPARUTA/sterge").with(csrf()).with(ANA).param("inapoi", "/"))
                .andExpect(redirectedUrl("/"));
        assertThat(keys()).isEmpty();
    }

    @Test
    void withoutCsrfNothingChanges() throws Exception {
        mvc.perform(post("/favorite/S_B").with(ANA)).andExpect(status().isForbidden());
        assertThat(keys()).isEmpty();
    }

    @Test
    void anUnknownStationIs404() throws Exception {
        mvc.perform(post("/favorite/NU_EXISTA").with(csrf()).with(ANA)).andExpect(status().isNotFound());
        assertThat(keys()).isEmpty();
    }

    @Test
    void theEleventhFavoriteIsRefusedWithTheMessage() throws Exception {
        for (int i = 0; i < 10; i++) {
            TestUsers.favorite(jdbc, EMAIL, "K" + i);
        }
        mvc.perform(post("/favorite/S_B").with(csrf()).with(ANA))
                .andExpect(redirectedUrl("/statie/S_B?limita"));
        assertThat(keys()).hasSize(10).doesNotContain("S_B");

        mvc.perform(get("/statie/S_B").param("limita", "").with(ANA))
                .andExpect(content().string(containsString("Ai deja 10 stații favorite. Șterge una ca să adaugi alta.")));
    }

    @Test
    void favoritesBelongToTheirUser() throws Exception {
        TestUsers.create(jdbc, "bogdan@exemplu.ro", "USER");
        TestUsers.favorite(jdbc, "bogdan@exemplu.ro", "S_B");
        mvc.perform(get("/statie/S_B").with(ANA))
                .andExpect(content().string(containsString("Adaugă la favorite")));
    }
}
