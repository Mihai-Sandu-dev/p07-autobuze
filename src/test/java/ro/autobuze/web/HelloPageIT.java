package ro.autobuze.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ro.autobuze.TestcontainersConfiguration;

/** Pagina „hello” de la „/” (temporară până la DS-autobuze-harta, D-022). */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class HelloPageIT {

    @Autowired
    MockMvc mvc;

    @Test
    void helloPageHasHeaderTextAndTursibFooter() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("<html lang=\"ro\"")))
                // Numele (D-041): antetul arată mereu „Staționar”; <title> și <h1> numele complet
                .andExpect(content().string(containsString("<title>Staționar — când vine autobuzul?</title>")))
                .andExpect(content().string(containsString("<h1>Staționar — când vine autobuzul?</h1>")))
                .andExpect(content().string(containsString("alt=\"\">Staționar</a>")))
                .andExpect(content().string(not(containsString("Când vine autobuzul?"))))
                // DS-autobuze-antet: numele și butonul de temă (D-039)
                .andExpect(content().string(containsString("aria-label=\"Temă întunecată\"")))
                .andExpect(content().string(not(containsString("Planificator"))))
                // textul aprobat în plan
                .andExpect(content().string(containsString(
                        "În curând: următoarele plecări din stațiile Tursib din Sibiu.")))
                // DS-autobuze-subsol (D-008)
                .andExpect(content().string(containsString("Date de transport: Tursib S.A.")))
                .andExpect(content().string(containsString("href=\"/css/tokens.css\"")));
    }

    @Test
    void temporaryHelloPageIsNotIndexed() throws Exception {
        // Pagina „hello” e temporară; noindex se scoate odată cu harta (D-022).
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<meta name=\"robots\" content=\"noindex\">")));
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
}
