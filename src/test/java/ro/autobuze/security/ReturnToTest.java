package ro.autobuze.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Întoarcerea pe pagina de origine (D-032): doar căi locale, altfel „Acasă” („/”). */
class ReturnToTest {

    @ParameterizedTest
    @CsvSource({"/statie/1051, /statie/1051", "/harta?statie=1051, /harta?statie=1051", "/, /"})
    void localPathsAreKept(String in, String out) {
        assertThat(ReturnTo.safe(in)).isEqualTo(out);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"//evil.ro", "https://evil.ro/", "/\\evil.ro", "statie/1051", "javascript:alert(1)",
        "/statie/1\r\nSet-Cookie: x=1"})
    void anythingElseGoesHome(String in) {
        assertThat(ReturnTo.safe(in)).isEqualTo("/");
    }
}
