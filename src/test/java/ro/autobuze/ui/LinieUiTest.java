package ro.autobuze.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

/** Pagina de linie (DS-autobuze-linie, D-060) pe feed-ul real din Compose. */
class LinieUiTest extends UiTest {

    @Test
    void theMapDrawsTheRouteAndTheOsmAttribution() {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(BASE_URL + "/linie/19");
            page.waitForSelector(".harta-mini path.traseu");
            assertThat(page.locator(".harta-mini path.traseu").count()).isGreaterThanOrEqualTo(2);
            assertThat(page.textContent(".harta-mini .leaflet-control-attribution")).contains("© OpenStreetMap contributors");
            assertThat(page.locator("details.sens-bloc").count()).isGreaterThan(1);
            assertThat(page.locator("details.sens-bloc[open]").count()).isEqualTo(1);
        }
    }

    @Test
    void theLineLabelOnTheStationPageOpensTheLinePage() {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(BASE_URL + "/statie/1051");
            String line = page.textContent(".grup .eticheta").trim();
            page.click(".grup .eticheta");
            page.waitForURL(BASE_URL + "/linie/" + line);
            assertThat(page.textContent("h1")).contains("Linia " + line);
        }
    }
}
