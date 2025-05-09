package com.vivero.viveroApp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViveroController {

    @GetMapping("/")
    public String index() {
        return "index"; 
    }

    @GetMapping("/navbar")
    public String navbar(Model model) {
        return "navbar";  // Debería devolver el archivo `navbar.html` que está en `src/main/resources/templates/`
    }
        
}
