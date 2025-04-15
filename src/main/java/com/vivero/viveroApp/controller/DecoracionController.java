package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Decoracion;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.service.DecoracionService;
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
@RequestMapping("/decoracion")
public class DecoracionController {

    private final DecoracionService decoracionService;
    private final ProveedorService proveedorService;
    private final PdfService pdfService;
    
    // Listar decoraciones con búsqueda, filtro y paginación
    @GetMapping("/listar")
    public String listarDecoraciones(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String proveedor,
            Model model) {
        String keyword = (nombre != null ? nombre : "") + (marca != null ? " " + marca : "") + (proveedor != null ? " " + proveedor : "");
        Page<Decoracion> decoracionPage = decoracionService.buscarDecoracionesPaginado(keyword.trim(), page, size);

        model.addAttribute("decoraciones", decoracionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", decoracionPage.getTotalPages());
        model.addAttribute("nombre", nombre);
        model.addAttribute("marca", marca);
        model.addAttribute("proveedor", proveedor);
        return "decoracion/listar-decoracion";
    }

    // Mostrar formulario para crear una nueva decoración
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("decoracion", new Decoracion());

        // Lista de proveedores para el formulario
        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);

        return "decoracion/crear-decoracion";
    }

    // Crear una nueva decoración desde el formulario
    @PostMapping("/crear")
    public String crearDecoracionDesdeVista(@ModelAttribute Decoracion decoracion, @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        decoracion.setProveedores(proveedoresSeleccionados);
        decoracionService.createDecoracion(decoracion);
        return "redirect:/decoracion/listar";
    }

    // Mostrar formulario para editar una decoración
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Decoracion> decoracionOpt = decoracionService.getDecoracionByIdIncluyendoInactivos(id);
        if (decoracionOpt.isPresent()) {
            model.addAttribute("decoracion", decoracionOpt.get());

            // Lista de proveedores para el formulario
            List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
            model.addAttribute("proveedores", proveedores);

            List<Long> proveedoresSeleccionados = decoracionOpt.get().getProveedores().stream()
                    .map(Proveedor::getId)
                    .toList();
            model.addAttribute("proveedoresSeleccionados", proveedoresSeleccionados);

            return "decoracion/editar-decoracion";
        } else {
            return "redirect:/decoracion/listar"; // Redirige si no se encuentra la decoración
        }
    }

    // Actualizar una decoración desde el formulario
    @PostMapping("/editar/{id}")
    public String actualizarDecoracionDesdeVista(@PathVariable Long id, @ModelAttribute Decoracion decoracionDetails,
            @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        decoracionDetails.setProveedores(proveedoresSeleccionados);
        decoracionService.updateDecoracion(id, decoracionDetails);
        return "redirect:/decoracion/listar";
    }

    // Dar de baja una decoración desde la vista
    @PostMapping("/dar-de-baja")
    public String darDeBajaDecoracionDesdeVista(@RequestParam("decoracionSeleccionada") Long id) {
        decoracionService.darDeBajaDecoracion(id);
        return "redirect:/decoracion/listar"; // Redirige a la lista de decoraciones activas
    }

    @GetMapping("/pdf")
    public void generarPDFDecoracion(HttpServletResponse response) throws Exception {
        List<Decoracion> decoraciones = decoracionService.getAllDecoraciones();

        String[] headers = {"ID", "Nombre", "Marca", "Tamaño", "Precio", "Stock"};
        Function<Object, String[]> rowMapper = decoracion -> {
            Decoracion d = (Decoracion) decoracion;
            return new String[]{
                String.valueOf(d.getId()),
                d.getNombre(),
                d.getMarca(),
                d.getTamaño(),
                String.valueOf(d.getPrecio()),
                String.valueOf(d.getStock())
            };
        };

        try {
            byte[] pdfBytes = pdfService.generarPDF(decoraciones, headers, rowMapper, true);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=decoracion.pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF");
        }
    }

}
