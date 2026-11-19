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
                // DS-autobuze-antet: doar numele aplicației și butonul de temă (D-039)
                .andExpect(content().string(containsString("Când vine autobuzul?")))
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
    void tokensStylesheetIsServed() throws Exception {
        mvc.perform(get("/css/tokens.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
    }
}
