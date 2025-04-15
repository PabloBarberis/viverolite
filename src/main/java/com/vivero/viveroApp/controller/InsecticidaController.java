package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Insecticida;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.service.InsecticidaService;
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
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/insecticida")
public class InsecticidaController {

    private final InsecticidaService insecticidaService;
    private final ProveedorService proveedorService;
    private final PdfService pdfService;

    @GetMapping("/listar")
    public String listarInsecticidas(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String proveedor,
            Model model) {
        Page<Insecticida> insecticidaPage = insecticidaService.buscarInsecticidaPaginado(nombre, marca, proveedor, page, size);

        model.addAttribute("insecticidas", insecticidaPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", insecticidaPage.getTotalPages());
        model.addAttribute("nombre", nombre);
        model.addAttribute("marca", marca);
        model.addAttribute("proveedor", proveedor);
        return "insecticida/listar-insecticida";
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("insecticida", new Insecticida());
        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);
        return "insecticida/crear-insecticida";
    }

    @PostMapping("/crear")
    public String crearInsecticidaDesdeVista(@ModelAttribute Insecticida insecticida,
            @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        insecticida.setProveedores(proveedoresSeleccionados);
        insecticidaService.createInsecticida(insecticida);
        return "redirect:/insecticida/listar";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Insecticida> insecticidaOpt = insecticidaService.getInsecticidaById(id);
        if (insecticidaOpt.isPresent()) {
            model.addAttribute("insecticida", insecticidaOpt.get());
            List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
            model.addAttribute("proveedores", proveedores);
            List<Long> proveedoresSeleccionados = insecticidaOpt.get().getProveedores().stream()
                    .map(Proveedor::getId)
                    .toList();
            model.addAttribute("proveedoresSeleccionados", proveedoresSeleccionados);
            return "insecticida/editar-insecticida";
        } else {
            return "redirect:/insecticida/listar";
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizarInsecticidaDesdeVista(@PathVariable Long id,
            @ModelAttribute Insecticida insecticidaDetails,
            @RequestParam(required = false) List<Long> proveedoresIds) {
        List<Proveedor> proveedoresSeleccionados = (proveedoresIds != null)
                ? proveedorService.getProveedoresByIds(proveedoresIds)
                : new ArrayList<>();
        insecticidaDetails.setProveedores(proveedoresSeleccionados);
        insecticidaService.updateInsecticida(id, insecticidaDetails);
        return "redirect:/insecticida/listar";
    }

    @PostMapping("/dar-de-baja")
    public String darDeBajaInsecticidaDesdeVista(@RequestParam("insecticidaSeleccionado") Long id) {
        insecticidaService.darDeBajaInsecticida(id);
        return "redirect:/insecticida/listar";
    }

    @GetMapping("/pdf")
    public void generarPDFInsecticida(HttpServletResponse response) throws Exception {
        List<Insecticida> insecticidas = insecticidaService.getAllInsecticidas();

        String[] headers = {"ID", "Nombre", "Marca", "Precio", "Stock"};
        Function<Object, String[]> rowMapper = insecticida -> {
            Insecticida i = (Insecticida) insecticida;
            return new String[]{
                String.valueOf(i.getId()),
                i.getNombre(),
                i.getMarca(),
                String.valueOf(i.getPrecio()),
                String.valueOf(i.getStock())
            };
        };

        try {
            byte[] pdfBytes = pdfService.generarPDF(insecticidas, headers, rowMapper, true);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=insecticida.pdf");
            response.getOutputStream().write(pdfBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF");
        }
    }
}
