package ro.autobuze.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ColorScheme;
import org.junit.jupiter.api.Test;

/** Butonul de temă (D-039): după sistem, comută light/dark, alegerea rămâne în browser. */
class TemaUiTest extends UiTest {

    static Page open(BrowserContext context) {
        Page page = context.newPage();
        page.navigate(BASE_URL + "/");
        return page;
    }

    static String pressed(Page page) {
        return page.getAttribute("button[aria-label='Temă întunecată']", "aria-pressed");
    }

    static String background(Page page) {
        return (String) page.evaluate("getComputedStyle(document.body).backgroundColor");
    }

    @Test
    void firstVisitFollowsTheSystemTheme() {
        try (BrowserContext dark = browser.newContext(new Browser.NewContextOptions().setColorScheme(ColorScheme.DARK));
             BrowserContext light = browser.newContext(new Browser.NewContextOptions().setColorScheme(ColorScheme.LIGHT))) {
            Page d = open(dark);
            Page l = open(light);
            assertThat(pressed(d)).isEqualTo("true");
            assertThat(pressed(l)).isEqualTo("false");
            assertThat(background(d)).isEqualTo("rgb(18, 20, 23)");   // --color-bg dark #121417
            assertThat(background(l)).isEqualTo("rgb(255, 255, 255)"); // --color-bg light #ffffff
        }
    }

    @Test
    void buttonSwitchesThemeAndTheChoiceSurvivesReload() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setColorScheme(ColorScheme.LIGHT))) {
            Page page = open(context);
            page.click("button[aria-label='Temă întunecată']");
            assertThat(pressed(page)).isEqualTo("true");
            assertThat(page.evaluate("document.documentElement.dataset.theme")).isEqualTo("dark");
            assertThat(page.evaluate("localStorage.getItem('tema')")).isEqualTo("dark");
            assertThat(background(page)).isEqualTo("rgb(18, 20, 23)");

            page.reload();
            assertThat(pressed(page)).isEqualTo("true");
            assertThat(background(page)).isEqualTo("rgb(18, 20, 23)");

            page.click("button[aria-label='Temă întunecată']");
            assertThat(pressed(page)).isEqualTo("false");
            assertThat(page.evaluate("localStorage.getItem('tema')")).isEqualTo("light");
        }
    }

    @Test
    void savedChoiceWinsOverTheSystem() {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setColorScheme(ColorScheme.DARK))) {
            Page page = open(context);
            page.evaluate("localStorage.setItem('tema', 'light')");
            page.reload();
            assertThat(pressed(page)).isEqualTo("false");
            assertThat(background(page)).isEqualTo("rgb(255, 255, 255)");
        }
    }
}
