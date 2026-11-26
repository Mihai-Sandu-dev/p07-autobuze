package ro.autobuze.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.LoadState;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Screenshot-uri light și dark, la 360 și 1280 px (D-029), în docs/design/screens/ ca
 * {@code <nume>.<light|dark>.<360|1280>.png}.
 * Harta: așteaptă markerele și tile-urile OSM (din rețea).
 * Pagina de stație: Gară (stop_id 1051), stația cu cele mai multe plecări din feed-ul Tursib;
 * cere feed-ul real importat în Compose.
 */
class ScreenshotUiTest extends UiTest {

    static final Path SCREENS = Path.of("docs/design/screens");

    @ParameterizedTest(name = "{0} · {1} · {2} px")
    @CsvSource({
        "DS-autobuze-harta, /harta, light, 360", "DS-autobuze-harta, /harta, light, 1280",
        "DS-autobuze-harta, /harta, dark, 360", "DS-autobuze-harta, /harta, dark, 1280",
        "DS-autobuze-statie, /statie/1051, light, 360", "DS-autobuze-statie, /statie/1051, light, 1280",
        "DS-autobuze-statie, /statie/1051, dark, 360", "DS-autobuze-statie, /statie/1051, dark, 1280",
        "DS-autobuze-login, /autentificare, light, 360", "DS-autobuze-login, /autentificare, light, 1280",
        "DS-autobuze-login, /autentificare, dark, 360", "DS-autobuze-login, /autentificare, dark, 1280",
        "DS-autobuze-inregistrare, /inregistrare, light, 360", "DS-autobuze-inregistrare, /inregistrare, light, 1280",
        "DS-autobuze-inregistrare, /inregistrare, dark, 360", "DS-autobuze-inregistrare, /inregistrare, dark, 1280",
    })
    void screenshot(String name, String path, String scheme, int width) {
        var options = new Browser.NewContextOptions()
                .setColorScheme("dark".equals(scheme) ? ColorScheme.DARK : ColorScheme.LIGHT)
                .setViewportSize(width, 800);
        try (BrowserContext context = browser.newContext(options)) {
            Page page = context.newPage();
            page.navigate(BASE_URL + path);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.screenshot(new Page.ScreenshotOptions()
                    .setFullPage(true)
                    .setPath(SCREENS.resolve(name + "." + scheme + "." + width + ".png")));
        }
    }

    /** „Acasă” cere un cont: unul nou la fiecare rulare, cu Gară (1051) la favorite. */
    @ParameterizedTest(name = "DS-autobuze-acasa · {0} · {1} px")
    @CsvSource({"light, 360", "light, 1280", "dark, 360", "dark, 1280"})
    void homeScreenshot(String scheme, int width) {
        var options = new Browser.NewContextOptions()
                .setColorScheme("dark".equals(scheme) ? ColorScheme.DARK : ColorScheme.LIGHT)
                .setViewportSize(width, 800);
        try (BrowserContext context = browser.newContext(options)) {
            Page page = context.newPage();
            AuthUiTest.registerAndLogIn(page, "/statie/1051", AuthUiTest.newEmail());
            page.click("button:has-text('Adaugă la favorite')");
            page.navigate(BASE_URL + "/");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.screenshot(new Page.ScreenshotOptions()
                    .setFullPage(true)
                    .setPath(SCREENS.resolve("DS-autobuze-acasa." + scheme + "." + width + ".png")));
        }
    }
}
