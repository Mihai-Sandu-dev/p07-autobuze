package ro.autobuze.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
 * Pagina hărții (DS-autobuze-harta) la „/” și la „/harta” (D-022, D-030) și JSON-ul stațiilor
 * (C-003), pe mini-feed. Aserțiunile despre antet, subsol, logo și favicon vin neschimbate din
 * HelloPageIT (pagina „hello” a fost înlocuită de hartă, cu acordul autorului, 2026-11-26).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class MapControllerIT {

    static final FeedServer FEED = FeedServer.get();

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
    void mapPageHasHeaderFooterAndTursibFooter() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("<html lang=\"ro\"")))
                // Numele (D-041): antetul arată mereu „Staționar”; <title> numele complet
                .andExpect(content().string(containsString("<title>Staționar — când vine autobuzul?</title>")))
                .andExpect(content().string(containsString("alt=\"\">Staționar</a>")))
                .andExpect(content().string(not(containsString("Când vine autobuzul?"))))
                // DS-autobuze-antet: numele și butonul de temă (D-039)
                .andExpect(content().string(containsString("aria-label=\"Temă întunecată\"")))
                .andExpect(content().string(not(containsString("Planificator"))))
                // DS-autobuze-subsol (D-008)
                .andExpect(content().string(containsString("Date de transport: Tursib S.A.")))
                .andExpect(content().string(containsString("href=\"/css/tokens.css\"")));
    }

    @Test
    void mapPageIsIndexed() throws Exception {
        // Fostul test „hello”: noindex s-a scos odată cu harta (D-022), ca la pagina de stație (D-049).
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("name=\"robots\""))));
    }

    @Test
    void logoAndFaviconAreLinkedAndServed() throws Exception {
        // Logo-ul din antet și favicon-ul (logoStationarHero.png, la cererea autorului, 2026-11-26)
        mvc.perform(get("/"))
                .andExpect(content().string(containsString("<img class=\"logo\" src=\"/img/logo-64.png\"")))
                .andExpect(content().string(containsString("<link rel=\"icon\" href=\"/favicon.ico\" sizes=\"48x48\">")))
                .andExpect(content().string(containsString(
                        "<link rel=\"icon\" href=\"/img/favicon-32.png\" type=\"image/png\">")))
                .andExpect(content().string(containsString(
                        "<link rel=\"apple-touch-icon\" href=\"/img/apple-touch-icon.png\">")));
        mvc.perform(get("/img/logo-64.png")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/png"));
        mvc.perform(get("/favicon.ico")).andExpect(status().isOk());
        mvc.perform(get("/img/favicon-32.png")).andExpect(status().isOk());
        mvc.perform(get("/img/apple-touch-icon.png")).andExpect(status().isOk());
    }

    @Test
    void tokensStylesheetIsServed() throws Exception {
        mvc.perform(get("/css/tokens.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
    }

    @Test
    void mapIsAtRootAndAtHartaWithSearchLeafletAndTheHeaderLink() throws Exception {
        for (String path : new String[] {"/", "/harta"}) {
            mvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString(">Stații</h1>")))
                    .andExpect(content().string(containsString("Caută o stație după nume")))
                    .andExpect(content().string(containsString("Aproape de mine")))
                    .andExpect(content().string(containsString("href=\"/vendor/leaflet/leaflet.css\"")))
                    .andExpect(content().string(containsString("src=\"/vendor/leaflet/leaflet.js\"")))
                    .andExpect(content().string(containsString("src=\"/js/harta.js\"")))
                    // D-030: „Hartă” din antet duce la /harta și e pagina curentă
                    .andExpect(content().string(containsString(
                            "<a href=\"/harta\" aria-current=\"page\">Hartă</a>")))
                    .andExpect(content().string(containsString("Aplicație neoficială. Nu este afiliată cu Tursib S.A.")));
        }
    }

    @Test
    void leafletIsServedWithItsLicence() throws Exception {
        // D-053
        mvc.perform(get("/vendor/leaflet/leaflet.js")).andExpect(status().isOk());
        mvc.perform(get("/vendor/leaflet/leaflet.css")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
        mvc.perform(get("/vendor/leaflet/LICENSE")).andExpect(status().isOk())
                .andExpect(content().string(containsString("BSD 2-Clause")));
    }

    @Test
    void stationsJsonHasTheFieldsOfC003() throws Exception {
        mvc.perform(get("/api/statii"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.feedImported").value(true))
                .andExpect(jsonPath("$.stations.length()").value(8))
                .andExpect(jsonPath("$.stations[0].stopId").value("S_C"))
                .andExpect(jsonPath("$.stations[0].name").value("Bulevardul Victoriei"))
                .andExpect(jsonPath("$.stations[0].lat").value(45.7902))
                .andExpect(jsonPath("$.stations[0].lon").value(24.152))
                .andExpect(jsonPath("$.stations[0].stopIds[0]").value("S_C"));
    }

    @Test
    void nearJsonHasTheFieldsOfC003() throws Exception {
        // Din S_A: S_A (0 m), S_B (~100 m), S_C (~200 m); S_CIBIN (~985 m) e al patrulea
        mvc.perform(get("/api/statii/aproape").param("lat", "45.792").param("lon", "24.152"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedImported").value(true))
                .andExpect(jsonPath("$.stations.length()").value(3))
                .andExpect(jsonPath("$.stations[0].stopId").value("S_A"))
                .andExpect(jsonPath("$.stations[0].name").value("Piața Mică"))
                .andExpect(jsonPath("$.stations[0].lat").value(45.792))
                .andExpect(jsonPath("$.stations[0].lon").value(24.152))
                .andExpect(jsonPath("$.stations[0].distanceM").value(0))
                .andExpect(jsonPath("$.stations[1].stopId").value("S_B"))
                .andExpect(jsonPath("$.stations[2].stopId").value("S_C"));
    }

    @Test
    void nearRejectsMissingOrInvalidCoordinates() throws Exception {
        mvc.perform(get("/api/statii/aproape")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/statii/aproape").param("lat", "45.79")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/statii/aproape").param("lat", "nord").param("lon", "24.15"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/statii/aproape").param("lat", "91").param("lon", "24.15"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/statii/aproape").param("lat", "45.79").param("lon", "-181"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withoutAnImportedFeedTheListsAreEmpty() throws Exception {
        jdbc.execute("truncate feed_versions restart identity cascade");
        mvc.perform(get("/api/statii"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedImported").value(false))
                .andExpect(jsonPath("$.stations").isEmpty());
        mvc.perform(get("/api/statii/aproape").param("lat", "45.792").param("lon", "24.152"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedImported").value(false))
                .andExpect(jsonPath("$.stations").isEmpty());
    }
}
