package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Herramienta;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.service.HerramientaService;
import com.vivero.viveroApp.service.ProveedorService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/herramienta")
public class HerramientaController {

    private final HerramientaService herramientaService;
    private final ProveedorService proveedorService;

    // Listar herramientas con búsqueda, filtro y paginación
    @GetMapping("/listar")
    public String listarHerramientas(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String proveedor,
            Model model) {
        Page<Herramienta> herramientaPage = herramientaService.buscarHerramientaPaginada(nombre, marca, proveedor, page, size);

        model.addAttribute("herramientas", herramientaPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", herramientaPage.getTotalPages());
        model.addAttribute("nombre", nombre);
        model.addAttribute("marca", marca);
        model.addAttribute("proveedor", proveedor);
        return "herramienta/listar-herramienta";
    }

    // Mostrar formulario para crear una nueva herramienta
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("herramienta", new Herramienta());

        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);

        return "herramienta/crear-herramienta";
    }

    // Crear una nueva herramienta desde el formulario
    @PostMapping("/crear")
    public String crearHerramientaDesdeVista(@ModelAttribute Herramienta herramienta,
            @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        herramienta.setProveedores(proveedoresSeleccionados);
        herramientaService.createHerramienta(herramienta);
        return "redirect:/herramienta/listar";
    }

    // Mostrar formulario para editar una herramienta
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Herramienta> herramientaOpt = herramientaService.getHerramientaById(id);
        if (herramientaOpt.isPresent()) {
            model.addAttribute("herramienta", herramientaOpt.get());

            List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
            model.addAttribute("proveedores", proveedores);

            List<Long> proveedoresSeleccionados = herramientaOpt.get().getProveedores().stream()
                    .map(Proveedor::getId)
                    .toList();
            model.addAttribute("proveedoresSeleccionados", proveedoresSeleccionados);

            return "herramienta/editar-herramienta";
        } else {
            return "redirect:/herramienta/listar";
        }
    }

    // Actualizar herramienta desde el formulario
    @PostMapping("/editar/{id}")
    public String actualizarHerramientaDesdeVista(@PathVariable Long id,
            @ModelAttribute Herramienta herramientaDetails,
            @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        herramientaDetails.setProveedores(proveedoresSeleccionados);
        herramientaService.updateHerramienta(id, herramientaDetails);
        return "redirect:/herramienta/listar";
    }

    // Dar de baja una herramienta desde la vista
    @PostMapping("/dar-de-baja")
    public String darDeBajaHerramientaDesdeVista(@RequestParam("herramientaSeleccionada") Long id) {
        herramientaService.darDeBajaHerramienta(id);
        return "redirect:/herramienta/listar";
    }

}
