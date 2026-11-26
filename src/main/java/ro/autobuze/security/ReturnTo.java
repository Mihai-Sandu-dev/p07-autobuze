package ro.autobuze.security;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * Întoarcerea pe pagina de origine după autentificare (D-032). Se acceptă doar o cale locală
 * („/...”, nu „//...” sau „/\...”, fără caractere de control); orice altceva duce la „Acasă” („/”).
 */
public final class ReturnTo {

    private ReturnTo() {
    }

    /** {@code path}, dacă e o cale locală; altfel „/”. */
    public static String safe(String path) {
        if (path == null || !path.startsWith("/") || path.startsWith("//") || path.startsWith("/\\")) {
            return "/";
        }
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c < 0x20 || c == 0x7f || c == '\\') {
                return "/";
            }
        }
        return path;
    }

    /** Adresa paginii de autentificare cu un semnal ({@code eroare}, {@code creat}) și, dacă există, originea. */
    public static String loginUrl(String flag, String inapoi) {
        UriComponentsBuilder url = UriComponentsBuilder.fromPath("/autentificare").query(flag);
        if (inapoi != null && !inapoi.isBlank()) {
            url.queryParam("inapoi", safe(inapoi));
        }
        return url.encode().build().toUriString();
    }
}
