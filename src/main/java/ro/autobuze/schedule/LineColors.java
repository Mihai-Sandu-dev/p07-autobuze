package ro.autobuze.schedule;

/** Culorile etichetei liniei (DESIGN.md → Eticheta liniei), hex fără „#”. */
public record LineColors(String background, String text) {

    /** Din {@code route_color} și {@code route_text_color} ale feed-ului; oricare poate lipsi. */
    public static LineColors of(String routeColor, String routeTextColor) {
        throw new UnsupportedOperationException("punctul 8: de scris");
    }
}
