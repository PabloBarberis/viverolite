package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Grow;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.service.GrowService;
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
@RequestMapping("/grow")
public class GrowController {

    private final GrowService growService;

    private final ProveedorService proveedorService;

    private final PdfService pdfService;

    // Listar productos Grow con búsqueda, filtro y paginación
    @GetMapping("/listar")
    public String listarGrow(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String proveedor,
            Model model) {
        String keyword = (nombre != null ? nombre : "") + (marca != null ? " " + marca : "") + (proveedor != null ? " " + proveedor : "");
        Page<Grow> growPage = growService.buscarGrowPaginado(keyword.trim(), page, size);

        model.addAttribute("grows", growPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", growPage.getTotalPages());
        model.addAttribute("nombre", nombre);
        model.addAttribute("marca", marca);
        model.addAttribute("proveedor", proveedor);
        return "grow/listar-grow";
    }

    // Mostrar formulario para crear un nuevo producto Grow
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("grow", new Grow());

        // Lista de proveedores para el formulario
        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);

        return "grow/crear-grow";
    }

    // Crear un nuevo producto Grow desde el formulario
    @PostMapping("/crear")
    public String crearGrowDesdeVista(@ModelAttribute Grow grow, @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        grow.setProveedores(proveedoresSeleccionados);
        growService.createGrow(grow);
        return "redirect:/grow/listar";
    }

    // Mostrar formulario para editar un producto Grow
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Grow> growOpt = growService.getGrowByIdIncluyendoInactivos(id);
        if (growOpt.isPresent()) {
            model.addAttribute("grow", growOpt.get());

            // Lista de proveedores para el formulario
            List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
            model.addAttribute("proveedores", proveedores);

            List<Long> proveedoresSeleccionados = growOpt.get().getProveedores().stream()
                    .map(Proveedor::getId)
                    .toList();
            model.addAttribute("proveedoresSeleccionados", proveedoresSeleccionados);

            return "grow/editar-grow";
        } else {
            return "redirect:/grow/listar"; // Redirige si no se encuentra el producto Grow
        }
    }

    // Actualizar un producto Grow desde el formulario
    @PostMapping("/editar/{id}")
    public String actualizarGrowDesdeVista(@PathVariable Long id, @ModelAttribute Grow growDetails,
            @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        growDetails.setProveedores(proveedoresSeleccionados);
        growService.updateGrow(id, growDetails);
        return "redirect:/grow/listar";
    }

    // Dar de baja un producto Grow desde la vista
    @PostMapping("/dar-de-baja")
    public String darDeBajaGrowDesdeVista(@RequestParam("growSeleccionado") Long id) {
        growService.darDeBajaGrow(id);
        return "redirect:/grow/listar";
    }

    @GetMapping("/pdf")
    public void generarPDFGrow(HttpServletResponse response) throws Exception {
        List<Grow> grows = growService.getAllGrowActivos();

        String[] headers = {"ID", "Nombre", "Marca", "Precio", "Stock"};
        Function<Object, String[]> rowMapper = grow -> {
            Grow g = (Grow) grow;
            return new String[]{
                String.valueOf(g.getId()),
                g.getNombre(),
                g.getMarca(),
                String.valueOf(g.getPrecio()),
                String.valueOf(g.getStock())
            };
        };

        try {
            byte[] pdfBytes = pdfService.generarPDF(grows, headers, rowMapper, false);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=grow.pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF");
        }
    }

}
