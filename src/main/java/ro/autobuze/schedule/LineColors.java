package ro.autobuze.schedule;

import java.util.Locale;

/** Culorile etichetei liniei (DESIGN.md → Eticheta liniei), hex fără „#”. */
public record LineColors(String background, String text) {

    static final String WHITE = "FFFFFF";
    static final String BLACK = "000000";

    /**
     * Din {@code route_color} și {@code route_text_color} ale feed-ului; lipsă → valorile implicite
     * GTFS (fundal alb, text negru). Sub 4,5:1, textul trece pe alb sau negru, care contrastează mai bine.
     */
    public static LineColors of(String routeColor, String routeTextColor) {
        String background = hexOr(routeColor, WHITE);
        String text = hexOr(routeTextColor, BLACK);
        if (contrast(background, text) < 4.5) {
            text = contrast(background, WHITE) >= contrast(background, BLACK) ? WHITE : BLACK;
        }
        return new LineColors(background, text);
    }

    /** O culoare care nu e hex de 6 cifre (GTFS) se tratează ca lipsă. */
    private static String hexOr(String value, String fallback) {
        String hex = value == null ? "" : value.strip().toUpperCase(Locale.ROOT);
        return hex.matches("[0-9A-F]{6}") ? hex : fallback;
    }

    /** Raportul de contrast WCAG 2 între două culori. */
    static double contrast(String a, String b) {
        double la = luminance(a);
        double lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double luminance(String hex) {
        int rgb = Integer.parseInt(hex, 16);
        return 0.2126 * channel(rgb >> 16) + 0.7152 * channel(rgb >> 8) + 0.0722 * channel(rgb);
    }

    private static double channel(int value) {
        double c = (value & 0xFF) / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
