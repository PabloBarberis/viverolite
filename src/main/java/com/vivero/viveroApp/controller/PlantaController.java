package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Planta;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.model.enums.TipoPlanta;
import com.vivero.viveroApp.service.PlantaService;
import com.vivero.viveroApp.service.ProveedorService;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/plantas")
public class PlantaController {

    private final PlantaService plantaService;
    private final ProveedorService proveedorService;
    
    // Listar plantas con búsqueda, filtro y paginación
    @GetMapping("/listar")
    public String listarPlantas(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipoPlanta,         
            Model model) {
        Page<Planta> plantaPage;

        // Verifica si hay parámetros de búsqueda y filtra las plantas en consecuencia
        if (nombre != null && !nombre.isEmpty() && tipoPlanta != null && !tipoPlanta.isEmpty()) {
            plantaPage = plantaService.buscarPorNombreYTipoPaginado(nombre, TipoPlanta.valueOf(tipoPlanta), page, size);
        } else if (nombre != null && !nombre.isEmpty()) {
            plantaPage = plantaService.buscarPorNombrePaginado(nombre, page, size);
        } else if (tipoPlanta != null && !tipoPlanta.isEmpty()) {
            plantaPage = plantaService.buscarPorTipoPaginado(TipoPlanta.valueOf(tipoPlanta), page, size);
        } else {
            plantaPage = plantaService.getAllPlantasPaginadas(page, size);
        }

        // Añade los atributos al modelo
        model.addAttribute("plantas", plantaPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", plantaPage.getTotalPages());
        model.addAttribute("tiposPlanta", TipoPlanta.values()); 
        model.addAttribute("nombre", nombre); 
        model.addAttribute("selectedTipo", tipoPlanta); 

        return "plantas/listar-planta"; 
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("planta", new Planta());
        model.addAttribute("tiposPlanta", TipoPlanta.values()); 

        // Agregar la lista de proveedores para seleccionar en el formulario
        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);

        return "plantas/crear-planta"; 
    }

    @PostMapping("/crear")
    public String crearPlantaDesdeVista(@ModelAttribute Planta planta, @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();

        planta.setProveedores(proveedoresSeleccionados);
        plantaService.createPlanta(planta);

        return "redirect:/plantas/listar";
    }

    // Mostrar formulario para editar una planta
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Planta> plantaOpt = plantaService.getPlantaByIdIncluyendoInactivas(id);
        if (plantaOpt.isPresent()) {
            model.addAttribute("planta", plantaOpt.get());
            model.addAttribute("tiposPlanta", TipoPlanta.values());

            // Agregar la lista de proveedores para seleccionar en el formulario
            List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
            model.addAttribute("proveedores", proveedores);

            // Agregar proveedores seleccionados
            List<Long> proveedoresSeleccionados = plantaOpt.get().getProveedores().stream()
                    .map(Proveedor::getId)
                    .toList();
            model.addAttribute("proveedoresSeleccionados", proveedoresSeleccionados);

            return "plantas/editar-planta"; 
        } else {
            return "redirect:/plantas/listar"; 
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizarPlantaDesdeVista(@PathVariable Long id,
            @ModelAttribute Planta plantaDetails,
            @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();

        plantaDetails.setProveedores(proveedoresSeleccionados);
        plantaService.updatePlanta(id, plantaDetails);

        return "redirect:/plantas/listar";
    }

    // Dar de baja una planta desde la vista
    @PostMapping("/dar-de-baja")
    public String darDeBajaPlantaDesdeVista(@RequestParam("plantaSeleccionada") Long id) {
        plantaService.darDeBajaPlanta(id);
        return "redirect:/plantas/listar";
    }

   
}
