package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.enums.TipoPlanta;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViveroController {

    @GetMapping("/")
    public String index() {
        return "index";  // Página principal
    }
    
    @GetMapping("/fotos")
    public String fotos(){
        return "fotos";
    }

@GetMapping("/plantasvivero")
    public String plantasVivero(Model model) {
        // Obtener todos los valores del enum TipoPlanta
        TipoPlanta[] categorias = TipoPlanta.values();

        // Pasar las categorías a la vista
        model.addAttribute("categorias", categorias);
        
        return "plantas";  // Página de plantas
    }


    @GetMapping("/macetasvivero")
    public String macetasVivero() {
        return "macetas";  // Página de macetas
    }

    @GetMapping("/decoracionvivero")
    public String decoracionVivero() {
        return "decoracion";  // Página de decoración
    }

    @GetMapping("/growshopvivero")
    public String growshopVivero() {
        return "growshop";  // Página de growshop
    }

    @GetMapping("/contactovivero")
    public String contactoVivero() {
        return "contacto";  // Página de contacto
    }

    @GetMapping("/navbar")
    public String navbar(Model model) {
        return "navbar";  // Debería devolver el archivo `navbar.html` que está en `src/main/resources/templates/`
    }
    
    
}
