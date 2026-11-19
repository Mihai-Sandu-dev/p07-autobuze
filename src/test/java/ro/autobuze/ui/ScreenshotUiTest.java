package ro.autobuze.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ColorScheme;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Screenshot-uri light și dark, la 360 și 1280 px (D-029), în docs/design/screens/ ca
 * {@code <nume>.<light|dark>.<360|1280>.png}. Pagina „hello” e temporară și nu are DS-ID.
 */
class ScreenshotUiTest extends UiTest {

    static final Path SCREENS = Path.of("docs/design/screens");

    @ParameterizedTest(name = "{0} · {1} · {2} px")
    @CsvSource({
        "hello, /, light, 360", "hello, /, light, 1280",
        "hello, /, dark, 360", "hello, /, dark, 1280",
    })
    void screenshot(String name, String path, String scheme, int width) {
        var options = new Browser.NewContextOptions()
                .setColorScheme("dark".equals(scheme) ? ColorScheme.DARK : ColorScheme.LIGHT)
                .setViewportSize(width, 800);
        try (BrowserContext context = browser.newContext(options)) {
            Page page = context.newPage();
            page.navigate(BASE_URL + path);
            page.screenshot(new Page.ScreenshotOptions()
                    .setFullPage(true)
                    .setPath(SCREENS.resolve(name + "." + scheme + "." + width + ".png")));
        }
    }
}
