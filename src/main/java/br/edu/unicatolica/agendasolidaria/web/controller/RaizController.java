package br.edu.unicatolica.agendasolidaria.web.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Hidden
@Controller
public class RaizController {

    @GetMapping("/")
    public String inicio() {
        return "forward:/index.html";
    }

    @GetMapping("/docs-api")
    public String documentacaoDaApi() {
        return "redirect:/swagger-ui/index.html";
    }
}
