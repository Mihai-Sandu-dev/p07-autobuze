package ro.autobuze.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ColorScheme;
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

    static double routeWidth(Page page) {
        return ((Number) page.evaluate(
                "document.querySelector('.harta-mini path.traseu').getBoundingClientRect().width")).doubleValue();
    }

    @Test
    void theLineMapZoomsWithTheButtonsAndDragsButNotWithTheWheel() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {
            Page page = context.newPage();
            page.navigate(BASE_URL + "/linie/19");
            page.waitForSelector(".harta-mini path.traseu");
            double before = routeWidth(page);

            // Rotița derulează pagina, nu schimbă zoom-ul
            page.hover(".harta-linie");
            page.mouse().wheel(0, 300);
            page.waitForTimeout(400);
            assertThat(routeWidth(page)).isEqualTo(before);
            assertThat((Number) page.evaluate("window.scrollY")).satisfies(y -> assertThat(y.doubleValue()).isPositive());

            page.click(".harta-linie .leaflet-control-zoom-in");
            page.waitForTimeout(600);
            assertThat(routeWidth(page)).isGreaterThan(before * 1.5);
            assertThat(page.url()).isEqualTo(BASE_URL + "/linie/19");
        }
    }

    @Test
    void theRouteHasAnOutlineInBothThemes() {
        // Linia 117 are culoarea #808080: conturul în culoarea textului o face vizibilă și pe tile-urile întunecate
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setColorScheme(ColorScheme.DARK))) {
            Page page = context.newPage();
            page.navigate(BASE_URL + "/linie/117");
            page.waitForSelector(".harta-mini path.traseu-contur");
            assertThat(page.evaluate("getComputedStyle(document.querySelector('.harta-mini path.traseu-contur')).stroke"))
                    .isEqualTo("rgb(230, 238, 245)"); // --color-text dark #e6eef5
        }
    }

    @Test
    void theStationSmallMapStaysFixed() {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(BASE_URL + "/statie/1051");
            page.waitForSelector(".harta-mini .leaflet-control-attribution");
            assertThat(page.locator(".harta-mini .leaflet-control-zoom-in").count()).isZero();
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
