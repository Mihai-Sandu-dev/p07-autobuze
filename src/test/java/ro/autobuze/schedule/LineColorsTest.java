package ro.autobuze.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Eticheta liniei (DESIGN.md): sub 4,5:1, textul trece pe alb sau negru, care contrastează mai bine. */
class LineColorsTest {

    @Test
    void feedColorsWithEnoughContrastStay() {
        assertThat(LineColors.of("0057A8", "FFFFFF")).isEqualTo(new LineColors("0057A8", "FFFFFF"));
    }

    @Test
    void lowContrastTextSwitchesToBlackOrWhite() {
        // galben cu text alb: ~1,6:1 → negru
        assertThat(LineColors.of("F2C500", "FFFFFF")).isEqualTo(new LineColors("F2C500", "000000"));
        // roșu fără culoare de text (implicit GTFS: negru, ~3,4:1) → alb
        assertThat(LineColors.of("C4161C", null)).isEqualTo(new LineColors("C4161C", "FFFFFF"));
    }

    @Test
    void lineWithoutColorUsesTheGtfsDefaults() {
        assertThat(LineColors.of(null, null)).isEqualTo(new LineColors("FFFFFF", "000000"));
        assertThat(LineColors.of("", " ")).isEqualTo(new LineColors("FFFFFF", "000000"));
    }

    @Test
    void lowercaseHexIsNormalised() {
        assertThat(LineColors.of("0057a8", "ffffff")).isEqualTo(new LineColors("0057A8", "FFFFFF"));
    }
}
