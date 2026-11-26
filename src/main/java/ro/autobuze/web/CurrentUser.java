package ro.autobuze.web;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ro.autobuze.security.ReturnTo;

/**
 * Ce știe antetul (DS-autobuze-antet) despre utilizator, pe fiecare pagină: emailul celui logat (sau null)
 * și pagina curentă, pentru linkul „Autentificare” cu întoarcere (D-032).
 */
@ControllerAdvice(annotations = Controller.class)
class CurrentUser {

    @ModelAttribute("utilizator")
    String utilizator(Principal principal) {
        return principal == null ? null : principal.getName();
    }

    /** Pagina de întors după autentificare; pe paginile de cont, originea lor. */
    @ModelAttribute("caleCurenta")
    String caleCurenta(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.equals("/autentificare") || path.equals("/inregistrare")) {
            String inapoi = request.getParameter("inapoi");
            return inapoi == null || inapoi.isBlank() ? null : ReturnTo.safe(inapoi);
        }
        String query = request.getQueryString();
        return ReturnTo.safe(query == null ? path : path + "?" + query);
    }
}
