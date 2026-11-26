package ro.autobuze.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ro.autobuze.TestcontainersConfiguration;

/**
 * Înregistrarea (DS-autobuze-inregistrare): regulile D-032, textele din mock, rolul USER și, după
 * creare, pagina de autentificare cu „Contul a fost creat. Autentifică-te.”, fără login (D-056).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RegistrationIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void noUsers() {
        jdbc.execute("truncate users restart identity cascade");
    }

    ResultActions register(String email, String parola, String parola2) throws Exception {
        return mvc.perform(post("/inregistrare").with(csrf())
                .param("email", email).param("parola", parola).param("parola2", parola2)
                .param("inapoi", "/statie/S_B"));
    }

    int users() {
        return jdbc.queryForObject("select count(*) from users", Integer.class);
    }

    @Test
    void formHasTheMockFieldsAndKeepsTheOrigin() throws Exception {
        mvc.perform(get("/inregistrare").param("inapoi", "/statie/S_B"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(">Cont nou</h1>")))
                .andExpect(content().string(containsString("name=\"parola2\"")))
                .andExpect(content().string(containsString("Cel puțin 8 caractere.")))
                .andExpect(content().string(containsString("name=\"inapoi\" value=\"/statie/S_B\"")))
                .andExpect(content().string(containsString("href=\"/autentificare?inapoi=/statie/S_B\"")));
    }

    @Test
    void successCreatesAUserAndGoesToLoginWithTheMessage() throws Exception {
        register("Ana@Exemplu.ro", "parola-buna", "parola-buna")
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/autentificare?creat&inapoi=/statie/S_B"));

        assertThat(users()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select role from users", String.class)).isEqualTo("USER");
        String hash = jdbc.queryForObject("select password_hash from users", String.class);
        assertThat(hash).startsWith("$2");
        assertThat(encoder.matches("parola-buna", hash)).isTrue();

        mvc.perform(get("/autentificare").param("creat", "").param("inapoi", "/statie/S_B"))
                .andExpect(content().string(containsString("Contul a fost creat. Autentifică-te.")))
                .andExpect(content().string(containsString("name=\"inapoi\" value=\"/statie/S_B\"")));
    }

    @Test
    void anEmailThatExistsWithOtherCaseIsRefused() throws Exception {
        TestUsers.create(jdbc, "ana@exemplu.ro", "USER");
        register("ANA@exemplu.ro", "parola-buna", "parola-buna")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Există deja un cont cu acest email.")));
        assertThat(users()).isEqualTo(1);
    }

    @Test
    void emptyFieldsAreNamed() throws Exception {
        register("", "", "")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Scrie adresa de email.")))
                .andExpect(content().string(containsString("Scrie parola.")))
                .andExpect(content().string(containsString("Scrie parola încă o dată.")));
        assertThat(users()).isZero();
    }

    @Test
    void anInvalidEmailIsRefused() throws Exception {
        register("nume.exemplu.ro", "parola-buna", "parola-buna")
                .andExpect(content().string(containsString("Scrie o adresă de email validă, de forma nume@exemplu.ro.")));
        assertThat(users()).isZero();
    }

    @Test
    void aPasswordUnder8CharactersIsRefused() throws Exception {
        register("ana@exemplu.ro", "scurta1", "scurta1")
                .andExpect(content().string(containsString("Parola trebuie să aibă cel puțin 8 caractere.")));
        assertThat(users()).isZero();
    }

    @Test
    void aPasswordOver72BytesIsTooLongEvenUnder72Characters() throws Exception {
        String lunga = "ș".repeat(40); // 40 de caractere, 80 de octeți UTF-8
        register("ana@exemplu.ro", lunga, lunga)
                .andExpect(content().string(containsString("Parola e prea lungă")));
        assertThat(users()).isZero();
    }

    @Test
    void exactly72BytesIsAccepted() throws Exception {
        String exact = "a".repeat(72);
        register("ana@exemplu.ro", exact, exact).andExpect(status().is3xxRedirection());
        assertThat(users()).isEqualTo(1);
    }

    @Test
    void differentPasswordsAreRefusedAndThePasswordsAreNotSentBack() throws Exception {
        register("ana@exemplu.ro", "parola-buna", "alta-parola")
                .andExpect(content().string(containsString("Parolele nu sunt la fel.")))
                .andExpect(content().string(containsString("value=\"ana@exemplu.ro\"")))
                .andExpect(content().string(not(containsString("parola-buna"))));
        assertThat(users()).isZero();
    }
}
