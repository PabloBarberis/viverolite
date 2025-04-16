package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Semilla;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.service.SemillaService;
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
@RequestMapping("/semilla")
public class SemillaController {

    private final SemillaService semillaService;
    private final ProveedorService proveedorService;

    @GetMapping("/listar")
    public String listarSemillas(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String proveedor,
            Model model) {
        Page<Semilla> semillaPage = semillaService.buscarSemillaPaginado(nombre, marca, proveedor, page, size);

        model.addAttribute("semillas", semillaPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", semillaPage.getTotalPages());
        model.addAttribute("nombre", nombre);
        model.addAttribute("marca", marca);
        model.addAttribute("proveedor", proveedor);
        return "semilla/listar-semilla";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("semilla", new Semilla());

        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);

        return "semilla/crear-semilla";
    }

    @PostMapping("/crear")
    public String crearSemillaDesdeVista(@ModelAttribute Semilla semilla,
            @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        semilla.setProveedores(proveedoresSeleccionados);
        semillaService.createSemilla(semilla);
        return "redirect:/semilla/listar";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Semilla> semillaOpt = semillaService.getSemillaById(id);
        if (semillaOpt.isPresent()) {
            model.addAttribute("semilla", semillaOpt.get());

            List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
            model.addAttribute("proveedores", proveedores);

            List<Long> proveedoresSeleccionados = semillaOpt.get().getProveedores().stream()
                    .map(Proveedor::getId)
                    .toList();
            model.addAttribute("proveedoresSeleccionados", proveedoresSeleccionados);

            return "semilla/editar-semilla";
        } else {
            return "redirect:/semilla/listar";
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizarSemillaDesdeVista(@PathVariable Long id,
            @ModelAttribute Semilla semillaDetails,
            @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        semillaDetails.setProveedores(proveedoresSeleccionados);
        semillaService.updateSemilla(id, semillaDetails);
        return "redirect:/semilla/listar";
    }

    @PostMapping("/dar-de-baja")
    public String darDeBajaSemillaDesdeVista(@RequestParam("semillaSeleccionada") Long id) {
        semillaService.darDeBajaSemilla(id);
        return "redirect:/semilla/listar";
    }

}
