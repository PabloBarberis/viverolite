package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.model.Tierra;
import com.vivero.viveroApp.model.enums.TipoTierra;
import com.vivero.viveroApp.service.ProveedorService;
import com.vivero.viveroApp.service.TierraService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/tierra")
public class TierraController {

    private final TierraService tierraService;
    private final ProveedorService proveedorService;
    
    // Listar tierras con búsqueda, filtro y paginación
    @GetMapping("/listar")
    public String listarTierras(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) TipoTierra tipo,
            @RequestParam(required = false) String proveedor,
            Model model) {
        String tipoStr = tipo != null ? tipo.name() : null;
        Page<Tierra> tierraPage = tierraService.buscarTierraPaginado(nombre, marca, tipoStr, proveedor, page, size);

        model.addAttribute("tierras", tierraPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tierraPage.getTotalPages());
        model.addAttribute("nombre", nombre);
        model.addAttribute("marca", marca);
        model.addAttribute("tipo", tipoStr);
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("tiposTierra", TipoTierra.values());
        model.addAttribute("selectedTipo", tipoStr);
        return "tierra/listar-tierra";
    }

    // Mostrar formulario para crear una nueva tierra
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("tierra", new Tierra());
        model.addAttribute("tiposTierra", TipoTierra.values());

        // Lista de proveedores para el formulario
        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);

        return "tierra/crear-tierra";
    }

    // Crear una nueva tierra desde el formulario
    @PostMapping("/crear")
    public String crearTierraDesdeVista(@ModelAttribute Tierra tierra, @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        tierra.setProveedores(proveedoresSeleccionados);
        tierraService.createTierra(tierra);
        return "redirect:/tierra/listar";
    }

    // Mostrar formulario para editar una tierra
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Tierra> tierraOpt = tierraService.getTierraById(id);
        if (tierraOpt.isPresent()) {
            model.addAttribute("tierra", tierraOpt.get());
            model.addAttribute("tiposTierra", TipoTierra.values());

            // Lista de proveedores para el formulario
            List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
            model.addAttribute("proveedores", proveedores);

            List<Long> proveedoresSeleccionados = tierraOpt.get().getProveedores().stream()
                    .map(Proveedor::getId)
                    .toList();
            model.addAttribute("proveedoresSeleccionados", proveedoresSeleccionados);

            return "tierra/editar-tierra";
        } else {
            return "redirect:/tierra/listar"; // Redirige si no se encuentra la tierra
        }
    }

    // Actualizar tierra desde el formulario
    @PostMapping("/editar/{id}")
    public String actualizarTierraDesdeVista(@PathVariable Long id, @ModelAttribute Tierra tierraDetails,
            @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        tierraDetails.setProveedores(proveedoresSeleccionados);
        tierraService.updateTierra(id, tierraDetails);
        return "redirect:/tierra/listar";
    }

    // Dar de baja una tierra desde la vista
    @PostMapping("/dar-de-baja")
    public String darDeBajaTierraDesdeVista(@RequestParam("tierraSeleccionada") Long id) {
        tierraService.darDeBajaTierra(id);
        return "redirect:/tierra/listar";
    }

}
