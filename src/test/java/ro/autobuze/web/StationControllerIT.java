package ro.autobuze.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
 * Pagina de stație și JSON-ul ei (C-002, D-050), pe mini-feed, cu ceasul oprit miercuri
 * 25.11.2026, 22:00, ora României.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class StationControllerIT {

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
    void stationPageShowsTheNextDepartures() throws Exception {
        mvc.perform(get("/statie/S_B"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("<h1")))
                .andExpect(content().string(containsString("Turnul Sfatului")))
                .andExpect(content().string(containsString("Miercuri, 25 noiembrie · zi lucrătoare · acum 22:00")))
                .andExpect(content().string(containsString("Piața Cibin")))
                .andExpect(content().string(containsString("23:50")))
                .andExpect(content().string(containsString("în 1 h 50 min")))
                .andExpect(content().string(containsString("07:15")))
                .andExpect(content().string(containsString("mâine")))
                // D-008, D-049; cu textul de neoficial, pagina se poate indexa
                .andExpect(content().string(containsString("Date de transport: Tursib S.A.")))
                .andExpect(content().string(containsString("Aplicație neoficială. Nu este afiliată cu Tursib S.A.")))
                .andExpect(content().string(not(containsString("name=\"robots\""))));
    }

    @Test
    void stationPageHasTheSmallMapLinkingToTheBigMap() throws Exception {
        // Harta mică din mock (DS-autobuze-statie, punctul 9): coordonatele din C-002, link spre harta mare
        mvc.perform(get("/statie/S_B"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"harta-mini\" href=\"/harta?statie=S_B\"")))
                .andExpect(content().string(containsString("data-lat=\"45.7929\" data-lon=\"24.152\"")))
                .andExpect(content().string(containsString("aria-label=\"Deschide harta mare, centrată pe stația Turnul Sfatului\"")))
                .andExpect(content().string(containsString("src=\"/vendor/leaflet/leaflet.js\"")))
                .andExpect(content().string(containsString("src=\"/js/harta-mini.js\"")))
                .andExpect(content().string(containsString("Vezi pe hartă")))
                // „Hartă” din antet (D-030); aici nu e pagina curentă
                .andExpect(content().string(containsString("<a href=\"/harta\">Hartă</a>")));
    }

    @Test
    void theLineLabelLinksToTheLinePage() throws Exception {
        // DS-autobuze-eticheta-linie: link spre pagina liniei (D-060)
        mvc.perform(get("/statie/S_B"))
                .andExpect(content().string(containsString("href=\"/linie/2\" aria-label=\"Linia 2, pagina liniei\">2</a>")));
    }

    @Test
    void stationWithoutDeparturesShowsTheEmptyState() throws Exception {
        // D-048: toate cursele se termină în S_CIBIN
        mvc.perform(get("/statie/S_CIBIN"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nicio plecare azi sau mâine")));
    }

    @Test
    void jsonHasTheFieldsOfC002() throws Exception {
        mvc.perform(get("/api/statii/S_B/plecari"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.feedImported").value(true))
                .andExpect(jsonPath("$.station.name").value("Turnul Sfatului"))
                .andExpect(jsonPath("$.station.stopIds[0]").value("S_B"))
                .andExpect(jsonPath("$.station.lat").value(45.7929))
                .andExpect(jsonPath("$.station.lon").value(24.152))
                .andExpect(jsonPath("$.now").value("2026-11-25T22:00:00"))
                .andExpect(jsonPath("$.dayLabel").value("Miercuri, 25 noiembrie · zi lucrătoare"))
                .andExpect(jsonPath("$.groups[0].line").value("2"))
                .andExpect(jsonPath("$.groups[0].headsign").value("Piața Cibin"))
                .andExpect(jsonPath("$.groups[0].color").value("FFFFFF"))
                .andExpect(jsonPath("$.groups[0].textColor").value("000000"))
                .andExpect(jsonPath("$.groups[0].departures[0].time").value("23:50"))
                .andExpect(jsonPath("$.groups[0].departures[0].at").value("2026-11-25T23:50:00"))
                .andExpect(jsonPath("$.groups[0].departures[0].tomorrow").value(false))
                .andExpect(jsonPath("$.groups[0].departures[0].minutes").value(110))
                .andExpect(jsonPath("$.groups[0].departures[1].tomorrow").value(true));
    }

    @Test
    void unknownStopIs404OnPageAndJson() throws Exception {
        mvc.perform(get("/statie/NU_EXISTA")).andExpect(status().isNotFound());
        mvc.perform(get("/api/statii/NU_EXISTA/plecari")).andExpect(status().isNotFound());
    }

    @Test
    void withoutAnImportedFeedThePageSaysSo() throws Exception {
        jdbc.execute("truncate feed_versions restart identity cascade");
        mvc.perform(get("/statie/S_B"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Orarul nu e încă importat")));
        mvc.perform(get("/api/statii/S_B/plecari"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedImported").value(false))
                .andExpect(jsonPath("$.groups").isEmpty());
    }
}
