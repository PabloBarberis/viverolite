package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Maceta;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.model.enums.ColorMaceta;
import com.vivero.viveroApp.model.enums.MaterialMaceta;
import com.vivero.viveroApp.service.MacetaService;
import com.vivero.viveroApp.service.PdfService;
import com.vivero.viveroApp.service.ProveedorService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/maceta")
public class MacetaController {

    private final MacetaService macetaService;
    private final ProveedorService proveedorService;
    private final PdfService pdfService;

    // Listar macetas con búsqueda, filtro y paginación
    @GetMapping("/listar")
    public String listarMacetas(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) ColorMaceta color,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) MaterialMaceta material,
            Model model) {
        String colorStr = color != null ? color.name() : null;
        String materialStr = material != null ? material.name() : null;
        
        Page<Maceta> macetaPage = macetaService.buscarMacetaPaginado(nombre, colorStr, marca, materialStr, page, size);

        model.addAttribute("macetas", macetaPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", macetaPage.getTotalPages());
        model.addAttribute("nombre", nombre);
        model.addAttribute("color", color);
        model.addAttribute("material", material);
        model.addAttribute("marca",marca);
        model.addAttribute("colores", ColorMaceta.values());
        model.addAttribute("materiales", MaterialMaceta.values());
        model.addAttribute("selectedColor", color);
        model.addAttribute("selectedMaterial", material);
        return "maceta/listar-maceta";
    }

    // Mostrar formulario para crear una nueva maceta
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("maceta", new Maceta());
        model.addAttribute("coloresMaceta", ColorMaceta.values());
        model.addAttribute("materialesMaceta", MaterialMaceta.values());

        // Lista de proveedores para el formulario
        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);

        return "maceta/crear-maceta";
    }

    // Crear una nueva maceta desde el formulario
    @PostMapping("/crear")
    public String crearMacetaDesdeVista(@ModelAttribute Maceta maceta, @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        maceta.setProveedores(proveedoresSeleccionados);
        macetaService.createMaceta(maceta);
        return "redirect:/maceta/listar";
    }

    // Mostrar formulario para editar una maceta
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Maceta> macetaOpt = macetaService.getMacetaById(id);
        if (macetaOpt.isPresent()) {
            model.addAttribute("maceta", macetaOpt.get());
            model.addAttribute("coloresMaceta", ColorMaceta.values());
            model.addAttribute("materialesMaceta", MaterialMaceta.values());

            // Lista de proveedores para el formulario
            List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
            model.addAttribute("proveedores", proveedores);

            List<Long> proveedoresSeleccionados = macetaOpt.get().getProveedores().stream()
                    .map(Proveedor::getId)
                    .toList();
            model.addAttribute("proveedoresSeleccionados", proveedoresSeleccionados);

            return "maceta/editar-maceta";
        } else {
            return "redirect:/maceta/listar"; // Redirige si no se encuentra la maceta
        }
    }

    // Actualizar maceta desde el formulario
    @PostMapping("/editar/{id}")
    public String actualizarMacetaDesdeVista(@PathVariable Long id, @ModelAttribute Maceta macetaDetails,
            @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        macetaDetails.setProveedores(proveedoresSeleccionados);
        macetaService.updateMaceta(id, macetaDetails);
        return "redirect:/maceta/listar";
    }

    // Dar de baja una maceta desde la vista
    @PostMapping("/dar-de-baja")
    public String darDeBajaMacetaDesdeVista(@RequestParam("macetaSeleccionada") Long id) {
        macetaService.darDeBajaMaceta(id);
        return "redirect:/maceta/listar";
    }

    @GetMapping("/pdf")
    public void generarPDFMaceta(HttpServletResponse response) throws Exception {
        List<Maceta> macetas = macetaService.getAllMacetas();

        String[] headers = {"ID", "Nombre", "Color", "Tamaño", "Material", "Precio", "Stock"};
        Function<Object, String[]> rowMapper = maceta -> {
            Maceta m = (Maceta) maceta;
            return new String[]{
                String.valueOf(m.getId()),
                m.getNombre(),
                String.valueOf(m.getColor()),
                m.getTamaño(),
                String.valueOf(m.getMaterial()),
                String.valueOf(m.getPrecio()),
                String.valueOf(m.getStock())
            };
        };

        try {
            byte[] pdfBytes = pdfService.generarPDF(macetas, headers, rowMapper, true);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=maceta.pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF");
        }
    }

}
