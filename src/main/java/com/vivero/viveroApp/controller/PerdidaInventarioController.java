package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.PerdidaInventario;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.service.PerdidaInventarioService;
import com.vivero.viveroApp.service.ProductoService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/perdida_inventario")
public class PerdidaInventarioController {

    private final PerdidaInventarioService perdidaInventarioService;

    private final ProductoService productoService;

    @GetMapping
    public String listarPerdidasInventario(Model model,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PerdidaInventario> perdidas = perdidaInventarioService.getAllPerdidas(pageable);
        model.addAttribute("perdidas", perdidas);

        return "perdida/perdida-inventario"; // Carga la vista principal
    }

    @PostMapping("/guardar")
    public ResponseEntity<String> guardarPerdidaInventario(
            @RequestParam("productoId") Long productoId,
            @RequestParam("cantidad") Integer cantidad,
            @RequestParam("descripcion") String descripcion) {

        if (productoId == null || cantidad == null || cantidad <= 0) {
            return ResponseEntity.badRequest().body("Error: Datos inválidos.");
        }

        try {
            // Crear y guardar la pérdida de inventario
            Optional<Producto> prOptional = productoService.getProductoById(productoId);
            Producto producto = prOptional.get();
            perdidaInventarioService.createPerdidaInventario(new PerdidaInventario(productoId, producto, descripcion, null, cantidad));
            return ResponseEntity.ok("Pérdida de inventario registrada correctamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar la pérdida: " + e.getMessage());
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarPerdidaInventario(@PathVariable Long id, Model model) {
        if (id == null || id <= 0) {
            model.addAttribute("error", "Error: ID inválido.");
            return "redirect:/perdida_inventario";
        }

        try {
            perdidaInventarioService.deletePerdidaInventario(id);
            model.addAttribute("success", "Pérdida de inventario eliminada correctamente.");
            return "redirect:/perdida_inventario"; // Redirige de vuelta a la página principal
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "Error: No se encontró la pérdida de inventario.");
            return "redirect:/perdida_inventario";
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar la pérdida de inventario.");
            return "redirect:/perdida_inventario";
        }
    }

    @GetMapping("/productos")
    @ResponseBody
    public List<Producto> obtenerProductos() {

        return productoService.getAllProductosActivos();
    }

}
