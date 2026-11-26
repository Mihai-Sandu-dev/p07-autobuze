package ro.autobuze.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Fluxul din browser (punctul 10): cont nou → autentificare cu „Contul a fost creat” (D-056) →
 * înapoi pe pagina de stație (D-032) → favorită → „Acasă” (D-057) → ieșire. Cere feed-ul real în Compose.
 */
class AuthUiTest extends UiTest {

    static String newEmail() {
        return "ui-" + UUID.randomUUID().toString().substring(0, 8) + "@exemplu.ro";
    }

    /** Creează un cont și se autentifică, pornind de pe {@code path}; rămâne pe {@code path}. */
    static void registerAndLogIn(Page page, String path, String email) {
        page.navigate(BASE_URL + path);
        page.click(".antet a:text-is('Autentificare')");
        page.click("a:text-is('Creează unul')");
        page.fill("#email", email);
        page.fill("#parola", "parola-de-test");
        page.fill("#parola2", "parola-de-test");
        page.click("button:text-is('Creează contul')");
        assertThat(page.textContent("main")).contains("Contul a fost creat. Autentifică-te.");
        page.fill("#email", email);
        page.fill("#parola", "parola-de-test");
        page.click("button:text-is('Autentifică-te')");
        page.waitForURL(BASE_URL + path);
    }

    @Test
    void registerLogInAddAFavoriteSeeItAtHomeAndLogOut() {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            registerAndLogIn(page, "/statie/1051", newEmail());

            page.click("button:has-text('Adaugă la favorite')");
            assertThat(page.textContent(".actiuni")).contains("În favorite");

            page.click(".antet a:text-is('Acasă')");
            assertThat(page.textContent("h1")).isEqualTo("Acasă");
            assertThat(page.textContent(".favorite")).contains("Gară");
            page.waitForSelector(".harta-mini .leaflet-control-attribution");

            page.click(".antet button:has-text('Ieșire')");
            page.waitForURL(BASE_URL + "/");
            assertThat(page.textContent("h1")).isEqualTo("Stații");
        }
    }

    @Test
    void emptyLoginFieldsAreNamedWithoutLeavingThePage() {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(BASE_URL + "/autentificare");
            page.click("button:text-is('Autentifică-te')");
            assertThat(page.textContent("#eroare-email")).isEqualTo("Scrie adresa de email.");
            assertThat(page.textContent("#eroare-parola")).isEqualTo("Scrie parola.");
            assertThat(page.url()).isEqualTo(BASE_URL + "/autentificare");
        }
    }
}
