package ro.autobuze.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.Geolocation;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Harta (DS-autobuze-harta): markerele din C-003, căutarea fără diacritice (D-054), „aproape de
 * mine” (D-034) cu locația simulată și atribuirea OSM (D-005). Cere feed-ul real importat în Compose.
 */
class HartaUiTest extends UiTest {

    static Page open(BrowserContext context, String path) {
        Page page = context.newPage();
        page.navigate(BASE_URL + path);
        // Markerele sunt în pagină (nu neapărat vizibile: la zoom mare, multe sunt în afara cadrului)
        page.waitForSelector(".harta .leaflet-interactive",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
        return page;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> firstStation(Page page) {
        var stations = (List<Map<String, Object>>) page.evaluate(
                "fetch('/api/statii').then(r => r.json()).then(j => j.stations)");
        return stations.get(0);
    }

    @Test
    void mapShowsTheStationsAndTheOsmAttribution() {
        try (BrowserContext context = browser.newContext()) {
            Page page = open(context, "/harta");
            int markers = page.locator(".harta .leaflet-interactive").count();
            assertThat(markers).isGreaterThan(100);
            assertThat(page.textContent(".harta .leaflet-control-attribution")).contains("© OpenStreetMap contributors");
        }
    }

    @Test
    void attributionUsesThePageColoursInDarkMode() {
        // Contrast ≥ 4,5:1 (D-042): atribuirea stă pe --color-bg, cu --color-text-muted, nu pe albul din leaflet.css
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setColorScheme(ColorScheme.DARK))) {
            Page page = open(context, "/harta");
            String bg = (String) page.evaluate(
                    "getComputedStyle(document.querySelector('.harta .leaflet-control-attribution')).backgroundColor");
            assertThat(bg).isEqualTo("rgb(17, 26, 35)"); // --color-bg dark #111a23
        }
    }

    @Test
    void searchIgnoresDiacriticsAndCase() {
        try (BrowserContext context = browser.newContext()) {
            Page page = open(context, "/harta");
            page.fill("#cauta", "TURNISOR");
            assertThat(page.textContent("#rezultate")).contains("Turnișor");

            page.fill("#cauta", "zzzz");
            assertThat(page.textContent("#rezultate")).contains("Nicio stație nu se potrivește cu „zzzz”");
        }
    }

    @Test
    void choosingAStationOpensTheCardWithTheLinkToItsPage() {
        try (BrowserContext context = browser.newContext()) {
            Page page = open(context, "/harta");
            page.fill("#cauta", "turnisor");
            // „turnisor” găsește și „Calea Turnisorului 1/2”; alegem stația „Turnișor”
            page.click("#rezultate button:text-is('Turnișor')");
            assertThat(page.textContent(".popup h2")).contains("Turnișor");
            assertThat(page.getAttribute(".popup a", "href")).startsWith("/statie/");
        }
    }

    @Test
    void statieParameterSelectsTheStation() {
        try (BrowserContext context = browser.newContext()) {
            Page page = open(context, "/harta?statie=1051");
            assertThat(page.getAttribute(".popup a", "href")).isEqualTo("/statie/1051");
        }
    }

    @Test
    void nearMeListsAtMostThreeStationsWithTheDistance() {
        try (BrowserContext context = browser.newContext()) {
            Map<String, Object> station = firstStation(open(context, "/harta"));
            double lat = ((Number) station.get("lat")).doubleValue();
            double lon = ((Number) station.get("lon")).doubleValue();
            context.grantPermissions(List.of("geolocation"));
            context.setGeolocation(new Geolocation(lat, lon));

            Page page = open(context, "/harta");
            page.click("#aproape");
            page.waitForSelector("#rezultate .dist");
            assertThat(page.textContent("#rezultate h2")).isEqualTo("Cele mai apropiate stații");
            assertThat(page.locator("#rezultate li").count()).isBetween(1, 3);
            assertThat(page.textContent("#rezultate li:first-child")).contains((String) station.get("name"));
            assertThat(page.textContent("#rezultate li:first-child .dist")).matches("\\d+0 m|0 m");
        }
    }

    @Test
    void nearMeFarFromSibiuSaysThereIsNoStationWithin1Km() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setPermissions(List.of("geolocation")).setGeolocation(44.4268, 26.1025))) {
            Page page = open(context, "/harta");
            page.click("#aproape");
            page.waitForSelector("#rezultate .mesaj:has-text('1 km')");
            assertThat(page.textContent("#rezultate")).contains("Nicio stație pe o rază de 1 km de tine.");
        }
    }

    @Test
    void refusedLocationSaysWhatToDo() {
        try (BrowserContext context = browser.newContext()) {
            Page page = open(context, "/harta");
            page.click("#aproape");
            page.waitForSelector("#rezultate .mesaj.eroare");
            assertThat(page.textContent("#rezultate")).contains("Nu am acces la locația ta.");
        }
    }

    @Test
    void stationPageSmallMapShowsTheOsmAttribution() {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(BASE_URL + "/statie/1051");
            page.waitForSelector(".harta-mini .leaflet-control-attribution");
            assertThat(page.textContent(".harta-mini .leaflet-control-attribution")).contains("© OpenStreetMap contributors");
        }
    }
}
