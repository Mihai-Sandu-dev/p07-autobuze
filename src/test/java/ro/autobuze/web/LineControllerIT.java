package ro.autobuze.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
import ro.autobuze.TestcontainersConfiguration;
import ro.autobuze.gtfs.FeedServer;
import ro.autobuze.gtfs.GtfsImportService;
import ro.autobuze.gtfs.MiniFeed;

/**
 * Pagina de linie (DS-autobuze-linie, D-060) pe mini-feed, cu ceasul oprit miercuri 25.11.2026, 22:00.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LineControllerIT {

    static final ZoneId BUCHAREST = ZoneId.of("Europe/Bucharest");
    static final FeedServer FEED = FeedServer.get();

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
    void importMiniFeed() {
        jdbc.execute("truncate feed_versions restart identity cascade");
        FEED.serve(MiniFeed.zip());
        importer.importNow();
    }

    @Test
    void linePageShowsTheMockParts() throws Exception {
        mvc.perform(get("/linie/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Linia 2 · Staționar — când vine autobuzul?</title>")))
                .andExpect(content().string(containsString("Linia 2</span></h1>")))
                .andExpect(content().string(containsString("Turnul Sfatului - Piața Cibin")))
                // sensul și stațiile cu link (D-060)
                .andExpect(content().string(containsString("Piața Cibin")))
                .andExpect(content().string(containsString("Stațiile, în ordine")))
                .andExpect(content().string(containsString("<a href=\"/statie/S_NOAPTE\">Strada Noaptea</a>")))
                // orarul pe rânduri de oră, cu data fiecărei coloane
                .andExpect(content().string(containsString("Orarul din Turnul Sfatului")))
                .andExpect(content().string(containsString("Lucrătoare<span>miercuri, 25 noiembrie</span>")))
                .andExpect(content().string(containsString("Sâmbătă<span>28 noiembrie</span>")))
                .andExpect(content().string(containsString("Duminică<span>29 noiembrie</span>")))
                .andExpect(content().string(containsString("Nu circulă")))
                .andExpect(content().string(containsString("class=\"alt-traseu\"")))
                .andExpect(content().string(containsString(
                        "◦ Traseu diferit: cursa nu trece prin toate stațiile de mai sus. Vezi pagina stației.")))
                .andExpect(content().string(containsString(
                        "Duminică: de sărbători, Tursib poate circula după alt orar: vezi pagina stației pentru ziua aceea.")))
                // harta: traseul și stațiile, pentru harta-mini.js
                .andExpect(content().string(containsString("data-trasee=\"[[[45.7929,24.152],[45.8019,24.152],[45.8003,24.1475]]]\"")))
                .andExpect(content().string(containsString("src=\"/js/harta-mini.js\"")))
                // fără abateri (punctul 13)
                .andExpect(content().string(not(containsString("abatere"))));
    }

    @Test
    void anUnknownLineIs404() throws Exception {
        mvc.perform(get("/linie/NU_EXISTA")).andExpect(status().isNotFound());
    }

    @Test
    void withoutAnImportedFeedThePageSaysSo() throws Exception {
        jdbc.execute("truncate feed_versions restart identity cascade");
        mvc.perform(get("/linie/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Orarul nu e încă importat")))
                .andExpect(content().string(containsString(
                        "Liniile apar după primul import al orarului Tursib. Revino peste câteva minute.")));
    }
}
