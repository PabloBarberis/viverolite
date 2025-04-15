package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Planta;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.model.enums.TipoPlanta;
import com.vivero.viveroApp.service.PdfService;
import com.vivero.viveroApp.service.PlantaService;
import com.vivero.viveroApp.service.ProveedorService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/plantas")
public class PlantaController {

    private final PlantaService plantaService;
    private final ProveedorService proveedorService;
    private final PdfService pdfService;

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
        model.addAttribute("tiposPlanta", TipoPlanta.values()); // Para el menú desplegable de filtrado
        model.addAttribute("nombre", nombre); // Para mantener el valor de búsqueda
        model.addAttribute("selectedTipo", tipoPlanta); // Para mantener el tipo seleccionado

        return "plantas/listar-planta"; // Asegúrate de que este sea el nombre correcto de tu vista
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("planta", new Planta());
        model.addAttribute("tiposPlanta", TipoPlanta.values()); // Menú desplegable para TipoPlanta

        // Agregar la lista de proveedores para seleccionar en el formulario
        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);

        return "plantas/crear-planta"; // Vista para crear planta
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
            model.addAttribute("tiposPlanta", TipoPlanta.values()); // Menú desplegable para TipoPlanta

            // Agregar la lista de proveedores para seleccionar en el formulario
            List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
            model.addAttribute("proveedores", proveedores);

            // Agregar proveedores seleccionados
            List<Long> proveedoresSeleccionados = plantaOpt.get().getProveedores().stream()
                    .map(Proveedor::getId)
                    .toList();
            model.addAttribute("proveedoresSeleccionados", proveedoresSeleccionados);

            return "plantas/editar-planta"; // Vista para editar planta
        } else {
            return "redirect:/plantas/listar"; // Redirige si no se encuentra la planta
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
        return "redirect:/plantas/listar"; // Redirige a la lista de plantas activas
    }

    @GetMapping("/pdf")
    public void generarPDF(HttpServletResponse response) throws Exception {
        List<Planta> plantas = plantaService.getAllPlantasSinPaginacion();

        String[] headers = {"ID", "Nombre", "Precio", "Stock"};
        Function<Object, String[]> rowMapper = planta -> {
            Planta p = (Planta) planta;
            return new String[]{
                String.valueOf(p.getId()),
                p.getNombre(),
                String.valueOf(p.getPrecio()),
                String.valueOf(p.getStock())
            };
        };

        try {
            byte[] pdfBytes = pdfService.generarPDF(plantas, headers, rowMapper, false);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=plantas.pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF");
        }
    }

}
