package ro.autobuze.web;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ro.autobuze.security.AppUsers;
import ro.autobuze.security.ReturnTo;

/**
 * Autentificarea (DS-autobuze-login; formularul e procesat de Spring Security, D-011) și înregistrarea
 * (DS-autobuze-inregistrare, D-032). După înregistrare: pagina de autentificare cu „Contul a fost creat”,
 * cu originea păstrată, fără login (D-056).
 */
@Controller
class AuthController {

    private final AppUsers users;

    AuthController(AppUsers users) {
        this.users = users;
    }

    @GetMapping("/autentificare")
    String login(@RequestParam(required = false) String eroare, @RequestParam(required = false) String creat,
            @RequestParam(required = false) String inapoi, Model model) {
        model.addAttribute("eroare", eroare != null);
        model.addAttribute("creat", creat != null);
        model.addAttribute("inapoi", origin(inapoi));
        return "autentificare";
    }

    @GetMapping("/inregistrare")
    String registerForm(@RequestParam(required = false) String inapoi, Model model) {
        model.addAttribute("inapoi", origin(inapoi));
        model.addAttribute("erori", Map.of());
        return "inregistrare";
    }

    @PostMapping("/inregistrare")
    String register(@RequestParam(defaultValue = "") String email, @RequestParam(defaultValue = "") String parola,
            @RequestParam(defaultValue = "") String parola2, @RequestParam(required = false) String inapoi,
            Model model) {
        Map<String, String> erori = users.register(email, parola, parola2);
        if (erori.isEmpty()) {
            return "redirect:" + ReturnTo.loginUrl("creat", inapoi);
        }
        // Parolele nu se trimit înapoi în pagină
        model.addAttribute("erori", erori);
        model.addAttribute("email", email);
        model.addAttribute("inapoi", origin(inapoi));
        return "inregistrare";
    }

    private static String origin(String inapoi) {
        return inapoi == null || inapoi.isBlank() ? null : ReturnTo.safe(inapoi);
    }
}
