package ro.autobuze;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * tokens.css e copia tokenilor din docs/design/DESIGN.md (D-027): fiecare token din tabele
 * există în tokens.css, cu aceeași valoare light și, pentru culori, aceeași valoare dark
 * (după sistem și după butonul de temă, D-039).
 */
class TokensTest {

    static final Path DESIGN = Path.of("docs/design/DESIGN.md");
    static final Path TOKENS = Path.of("src/main/resources/static/css/tokens.css");

    static final Pattern ROW = Pattern.compile("^\\|\\s*`(--[a-z0-9-]+)`\\s*\\|(.*)$");
    static final Pattern HEX = Pattern.compile("#[0-9a-fA-F]{3,8}");
    static final Pattern CODE = Pattern.compile("`([^`]+)`");
    static final Pattern BLOCK = Pattern.compile("([^{}]+)\\{([^{}]*)\\}");
    static final Pattern DECL = Pattern.compile("(--[a-z0-9-]+)\\s*:\\s*([^;]+);");

    record Design(Map<String, String> light, Map<String, String> dark) {}

    @Test
    void everyDesignTokenIsInTokensCssWithTheSameValues() throws IOException {
        Design design = readDesign();
        assertThat(design.light()).as("tokeni citiți din DESIGN.md").hasSizeGreaterThan(30);

        Map<String, String> light = new LinkedHashMap<>();
        Map<String, String> darkBySystem = new LinkedHashMap<>();
        Map<String, String> darkByButton = new LinkedHashMap<>();
        Matcher b = BLOCK.matcher(Files.readString(TOKENS).replaceAll("(?s)/\\*.*?\\*/", ""));
        while (b.find()) {
            String selector = b.group(1).replaceAll("\\s+", " ").trim();
            Map<String, String> target = switch (selector) {
                case ":root" -> light;
                case ":root:not([data-theme=\"light\"])" -> darkBySystem;
                case ":root[data-theme=\"dark\"]" -> darkByButton;
                default -> null;
            };
            if (target == null) continue;
            Matcher d = DECL.matcher(b.group(2));
            while (d.find()) target.put(d.group(1), d.group(2).replaceAll("\\s+", " ").trim());
        }

        assertThat(light).as(":root (light)").containsAllEntriesOf(design.light());
        assertThat(darkBySystem).as("dark după sistem").containsAllEntriesOf(design.dark());
        assertThat(darkByButton).as("dark după buton").containsAllEntriesOf(design.dark());
    }

    static Design readDesign() throws IOException {
        Map<String, String> light = new LinkedHashMap<>();
        Map<String, String> dark = new LinkedHashMap<>();
        String section = "";
        boolean inTokens = false;
        for (String line : Files.readAllLines(DESIGN)) {
            if (line.startsWith("## ")) inTokens = line.equals("## Tokens");
            if (line.startsWith("### ")) section = line.substring(4).trim();
            Matcher row = ROW.matcher(line);
            if (!inTokens || !row.matches()) continue;
            String token = row.group(1);
            String[] cols = row.group(2).split("\\|");
            if (section.equals("Color")) {
                light.put(token, firstHex(cols[0]));
                dark.put(token, firstHex(cols[1]));
            } else if (section.equals("Typography")) {
                light.put(token, token.startsWith("--font-") ? firstCode(cols[0]) : size(cols[1]));
            } else {
                light.put(token, size(cols[0]));
            }
        }
        return new Design(light, dark);
    }

    static String firstHex(String col) {
        Matcher m = HEX.matcher(col);
        assertThat(m.find()).as("culoare în „%s”", col).isTrue();
        return m.group().toLowerCase();
    }

    static String firstCode(String col) {
        Matcher m = CODE.matcher(col);
        assertThat(m.find()).as("valoare în „%s”", col).isTrue();
        return m.group(1).trim();
    }

    /** „12 / 16 px” → 12px/16px; „4 px” → 4px; „150 ms” → 150ms; `…` → conținutul. */
    static String size(String col) {
        String v = col.trim();
        if (v.startsWith("`")) return firstCode(v);
        Matcher pair = Pattern.compile("^(\\d+) / (\\d+) px$").matcher(v);
        if (pair.matches()) return pair.group(1) + "px/" + pair.group(2) + "px";
        return v.replaceAll("^(\\d+) (px|ms)$", "$1$2");
    }
}
