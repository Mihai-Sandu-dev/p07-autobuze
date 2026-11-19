package ro.autobuze.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

/**
 * Testele de UI rulează cu Playwright headless pe aplicația pornită în Docker Compose
 * (D-029): {@code docker compose up -d}, apoi {@code ./mvnw test -Pui}.
 */
@Tag("ui")
abstract class UiTest {

    static final String BASE_URL = System.getProperty("base.url", "http://localhost:8080");

    static Playwright playwright;
    static Browser browser;

    @BeforeAll
    static void startBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void stopBrowser() {
        playwright.close();
    }
}
