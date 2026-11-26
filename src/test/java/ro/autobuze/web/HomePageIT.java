package ro.autobuze.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
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
 * „Acasă” la „/” pentru utilizatorul logat (DS-autobuze-acasa, D-022, D-057), pe mini-feed, cu ceasul
 * oprit miercuri 25.11.2026, 22:00, ora României (ca în StationControllerIT).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class HomePageIT {

    static final ZoneId BUCHAREST = ZoneId.of("Europe/Bucharest");
    static final FeedServer FEED = FeedServer.get();
    static final String EMAIL = "ana@exemplu.ro";
    static final RequestPostProcessor ANA = user(EMAIL).roles("USER");

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClock {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(LocalDateTime.of(2026, 11, 25, 22, 0).atZone(BUCHAREST).toInstant(), BUCHAREST);
        }
    }

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
    void visitorStillSeesTheMap() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(containsString(">Stații</h1>")))
                .andExpect(content().string(not(containsString(">Acasă</h1>"))));
    }

    @Test
    void userSeesTheNextDeparturePerGroupForEachFavorite() throws Exception {
        TestUsers.favorite(jdbc, EMAIL, "S_B");
        mvc.perform(get("/").with(ANA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(">Acasă</h1>")))
                .andExpect(content().string(containsString("Miercuri, 25 noiembrie · zi lucrătoare · acum 22:00")))
                .andExpect(content().string(containsString("<a href=\"/statie/S_B\">Turnul Sfatului</a>")))
                // linia 2 spre Piața Cibin: doar prima plecare (23:50), nu și următoarele (07:15 mâine)
                .andExpect(content().string(containsString("Piața Cibin")))
                .andExpect(content().string(containsString("23:50")))
                .andExpect(content().string(containsString("în 1 h 50 min")))
                .andExpect(content().string(not(containsString("07:15"))))
                // DS-autobuze-antet: „Acasă” e pagina curentă
                .andExpect(content().string(containsString("<a href=\"/\" aria-current=\"page\">Acasă</a>")));
    }

    @Test
    void favoritesAreOrderedByNameAndHaveTheMapBelow() throws Exception {
        TestUsers.favorite(jdbc, EMAIL, "S_B");
        TestUsers.favorite(jdbc, EMAIL, "S_A");
        mvc.perform(get("/").with(ANA))
                .andExpect(content().string(matchesPattern("(?s).*Piața Mică.*Turnul Sfatului.*Favoritele pe hartă.*")))
                .andExpect(content().string(containsString("data-puncte=\"[[45.792,24.152],[45.7929,24.152]]\"")))
                .andExpect(content().string(containsString("src=\"/js/harta-mini.js\"")));
    }

    @Test
    void aFavoriteWithoutDeparturesSaysSo() throws Exception {
        // D-048: toate cursele se termină în S_CIBIN
        TestUsers.favorite(jdbc, EMAIL, "S_CIBIN");
        mvc.perform(get("/").with(ANA))
                .andExpect(content().string(containsString("Piața Cibin, peron 2")))
                .andExpect(content().string(containsString("Nicio plecare azi sau mâine.")));
    }

    @Test
    void aFavoriteThatLeftTheFeedCanBeRemoved() throws Exception {
        TestUsers.favorite(jdbc, EMAIL, "DISPARUTA");
        mvc.perform(get("/").with(ANA))
                .andExpect(content().string(containsString("Stația nu mai e în orarul actual Tursib.")))
                .andExpect(content().string(containsString("action=\"/favorite/DISPARUTA/sterge\" method=\"post\"")))
                .andExpect(content().string(containsString("Șterge din favorite")));
    }

    @Test
    void withoutFavoritesThePageSaysWhatToDo() throws Exception {
        mvc.perform(get("/").with(ANA))
                .andExpect(content().string(containsString("Nu ai încă stații favorite")))
                .andExpect(content().string(containsString("Poți salva până la 10 stații.")))
                .andExpect(content().string(containsString("Caută o stație pe hartă")))
                .andExpect(content().string(not(containsString("Favoritele pe hartă"))));
    }

    @Test
    void withoutAnImportedFeedThePageSaysSo() throws Exception {
        TestUsers.favorite(jdbc, EMAIL, "S_B");
        jdbc.execute("truncate feed_versions restart identity cascade");
        mvc.perform(get("/").with(ANA))
                .andExpect(content().string(containsString("Orarul nu e încă importat")));
    }
}
