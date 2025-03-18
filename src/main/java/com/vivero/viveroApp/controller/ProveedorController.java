package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.service.ProveedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/proveedores")
public class ProveedorController {

    
    private final ProveedorService proveedorService;

    
    // Obtener todos los proveedores activos para mostrar en la vista
    @GetMapping("/listar")
    public String vistaProveedoresActivos(Model model) {
        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);
        return "proveedores/listar-proveedor"; // Vista para listar proveedores activos
    }

    // Mostrar el formulario para crear un nuevo proveedor
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "proveedores/crear-proveedor"; // Vista para crear proveedor
    }

    // Crear un nuevo proveedor desde el formulario
    @PostMapping("/crear")
    public String crearProveedorDesdeVista(@ModelAttribute Proveedor proveedor) {
        proveedorService.createProveedor(proveedor);
        return "redirect:/proveedores/listar"; // Redirige a la lista de proveedores activos
    }

    // Actualizar un proveedor existente (formulario)
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Proveedor> proveedorOpt = proveedorService.getProveedorById(id);
        if (proveedorOpt.isPresent()) {
            model.addAttribute("proveedor", proveedorOpt.get());
            return "proveedores/editar-proveedor"; // Vista para editar proveedor
        } else {
            return "redirect:/proveedores/listar"; // Redirige si no se encuentra el proveedor
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizarProveedorDesdeVista(@PathVariable Long id, @ModelAttribute Proveedor proveedorDetails) {
        proveedorService.updateProveedor(id, proveedorDetails);
        return "redirect:/proveedores/listar"; // Redirige a la lista de proveedores activos
    }

    // Dar de baja a un proveedor desde la vista
    @PostMapping("/dar-de-baja")
    public String darDeBajaProveedorDesdeVista(@RequestParam("proveedorSeleccionado") Long id) {
        proveedorService.darDeBajaProveedor(id);
        return "redirect:/proveedores/listar"; // Redirige a la lista de proveedores activos
    }
}
