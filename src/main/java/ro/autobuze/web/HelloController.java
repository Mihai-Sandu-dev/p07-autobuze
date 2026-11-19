package ro.autobuze.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Pagina „hello” de la „/”, temporară până la harta reală (D-022). */
@Controller
class HelloController {

    @GetMapping("/")
    String hello() {
        return "hello";
    }
}
