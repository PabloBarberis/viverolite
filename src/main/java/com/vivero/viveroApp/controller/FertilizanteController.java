
package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Fertilizante;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.service.FertilizanteService;
import com.vivero.viveroApp.service.PdfService;
import com.vivero.viveroApp.service.ProveedorService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
@RequestMapping("/fertilizante")
public class FertilizanteController {

    private final FertilizanteService fertilizanteService;
    private final ProveedorService proveedorService;
    private final PdfService pdfService;

    // Página de inicio de fertilizantes
    @GetMapping("/index")
    public String index() {
        return "index"; // Redirige a index.html
    }

    // Listar fertilizantes con búsqueda, filtro y paginación
    @GetMapping("/listar")
    public String listarFertilizantes(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "15") int size,
                                      @RequestParam(required = false) String nombre,
                                      @RequestParam(required = false) String marca,
                                      @RequestParam(required = false) String proveedor,
                                      Model model) {
        Page<Fertilizante> fertilizantePage = fertilizanteService.buscarFertilizantePaginado(nombre, marca, proveedor, page, size);

        model.addAttribute("fertilizantes", fertilizantePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", fertilizantePage.getTotalPages());
        model.addAttribute("nombre", nombre);
        model.addAttribute("marca", marca);
        model.addAttribute("proveedor", proveedor);
        return "fertilizante/listar-fertilizante";
    }

    // Mostrar formulario para crear un nuevo fertilizante
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("fertilizante", new Fertilizante());

        // Lista de proveedores para el formulario
        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);

        return "fertilizante/crear-fertilizante";
    }

    // Crear un nuevo fertilizante desde el formulario
    @PostMapping("/crear")
    public String crearFertilizanteDesdeVista(@ModelAttribute Fertilizante fertilizante,
                                              @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        fertilizante.setProveedores(proveedoresSeleccionados);
        fertilizanteService.createFertilizante(fertilizante);
        return "redirect:/fertilizante/listar";
    }

    // Mostrar formulario para editar un fertilizante
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Fertilizante> fertilizanteOpt = fertilizanteService.getFertilizanteById(id);
        if (fertilizanteOpt.isPresent()) {
            model.addAttribute("fertilizante", fertilizanteOpt.get());

            // Lista de proveedores para el formulario
            List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
            model.addAttribute("proveedores", proveedores);

            List<Long> proveedoresSeleccionados = fertilizanteOpt.get().getProveedores().stream()
                    .map(Proveedor::getId)
                    .toList();
            model.addAttribute("proveedoresSeleccionados", proveedoresSeleccionados);

            return "fertilizante/editar-fertilizante";
        } else {
            return "redirect:/fertilizante/listar"; // Redirige si no se encuentra el fertilizante
        }
    }

    // Actualizar fertilizante desde el formulario
    @PostMapping("/editar/{id}")
    public String actualizarFertilizanteDesdeVista(@PathVariable Long id,
                                                   @ModelAttribute Fertilizante fertilizanteDetails,
                                                   @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        fertilizanteDetails.setProveedores(proveedoresSeleccionados);
        fertilizanteService.updateFertilizante(id, fertilizanteDetails);
        return "redirect:/fertilizante/listar";
    }

    // Dar de baja un fertilizante desde la vista
    @PostMapping("/dar-de-baja")
    public String darDeBajaFertilizanteDesdeVista(@RequestParam("fertilizanteSeleccionado") Long id) {
        fertilizanteService.darDeBajaFertilizante(id);
        return "redirect:/fertilizante/listar";
    }

    // Generar PDF de fertilizantes
    @GetMapping("/pdf")
    public void generarPDFFertilizante(HttpServletResponse response) throws Exception {
        List<Fertilizante> fertilizantes = fertilizanteService.getAllFertilizantes();

        String[] headers = {"ID", "Nombre", "Marca", "Precio", "Stock"};
        Function<Object, String[]> rowMapper = fertilizante -> {
            Fertilizante f = (Fertilizante) fertilizante;
            return new String[]{
                    String.valueOf(f.getId()),
                    f.getNombre(),
                    f.getMarca(),
                    String.valueOf(f.getPrecio()),
                    String.valueOf(f.getStock())
            };
        };

        try {
            byte[] pdfBytes = pdfService.generarPDF(fertilizantes, headers, rowMapper, true);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=fertilizante.pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF");
        }
    }
}
