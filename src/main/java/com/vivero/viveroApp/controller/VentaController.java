package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Cliente;
import com.vivero.viveroApp.model.Venta;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.model.enums.Descuento;
import com.vivero.viveroApp.model.enums.MetodoPago;
import com.vivero.viveroApp.repository.VentaRepository;
import com.vivero.viveroApp.service.VentaService;
import com.vivero.viveroApp.service.ProductoService;
import com.vivero.viveroApp.service.ClienteService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;

    private final VentaRepository ventaRepository;

    private final ProductoService productoService;

    private final ClienteService clienteService;

    // Mostrar formulario para crear una nueva venta
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {

        List<Producto> productos = productoService.getAllProductos();
        for (Producto producto : productos) {
            System.out.println(producto);
        }
        List<Cliente> clientes = clienteService.getAllClientesActivos();
        model.addAttribute("productos", productos);
        model.addAttribute("clientes", clientes);
        model.addAttribute("metodosPago", MetodoPago.values());
        model.addAttribute("descuentos", Descuento.values());
        model.addAttribute("venta", new Venta());
        return "ventas/crear-venta";
    }

    // Crear una nueva venta
    @PostMapping("/crear")
    public String crearVenta(@ModelAttribute Venta venta, @RequestParam List<Long> productoIds, @RequestParam List<Integer> cantidades,
            @RequestParam Descuento descuento, @RequestParam MetodoPago metodoPago, @RequestParam(required = false) Long clienteId,
            @RequestParam double total, Model model) {
        venta.setFecha(new Date());
        venta.setDescuento(descuento);
        venta.setMetodoPago(metodoPago);
        venta.setTotal(total);
        if (clienteId != null) {
            Cliente cliente = clienteService.getClienteById(clienteId).orElse(null);
            if (cliente != null) {
                venta.setCliente(cliente);
            }
        }
        venta.getProductos().clear();
        for (int i = 0; i < productoIds.size(); i++) {
            Producto producto = productoService.getProductoById(productoIds.get(i)).orElse(null);
            if (producto != null) {
                venta.agregarProducto(producto, cantidades.get(i));
            }
        }
        try {
            ventaService.createVenta(venta);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "ventas/crear-venta";
        }
        return "redirect:/ventas/listar";
    }

    // Listar ventas
// Listar ventas
    @GetMapping("/listar")
    public String listarVentas(Model model, @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Venta> ventas = ventaService.getAllVentas(pageable);
        model.addAttribute("ventas", ventas);
        return "ventas/listar-venta";
    }

    // Ver detalle de una venta
    @GetMapping("/detalle/{id}")
    public String verDetalleVenta(@PathVariable Long id, Model model) {
        Venta venta = ventaService.getVentaById(id).orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + id));
        model.addAttribute("venta", venta);
        return "ventas/detalle-venta";
    }

    // Obtener venta por ID para editar
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Venta venta = ventaService.getVentaById(id).orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + id));
        List<Producto> productos = productoService.getAllProductos();
        List<Cliente> clientes = clienteService.getAllClientesActivos();
        model.addAttribute("productos", productos);
        model.addAttribute("clientes", clientes);
        model.addAttribute("metodosPago", MetodoPago.values());
        model.addAttribute("descuentos", Descuento.values());
        model.addAttribute("venta", venta);
        System.out.println("ANTES DE CARGAR LA PAGINA EL ID DE LA VENTA ES: " + venta.getId());
        return "ventas/editar-venta";
    }

    // Actualizar venta
    @PostMapping("/actualizar")
    public String actualizarVenta(@ModelAttribute Venta venta, @RequestParam List<Long> productoIds, @RequestParam List<Integer> cantidades,
            @RequestParam Descuento descuento, @RequestParam MetodoPago metodoPago, @RequestParam(required = false) Long clienteId,
            @RequestParam String total, Model model) {

        try {
            double totalDouble = Double.parseDouble(total.replace(",", "."));
            venta.setTotal(totalDouble);
        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "El formato del total no es válido.");
            return "ventas/editar-venta";
        }

        // Asegurar que se edita una venta existente
        Venta ventaExistente = ventaService.getVentaById(venta.getId())
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + venta.getId()));

        venta.setFecha(new Date());
        venta.setDescuento(descuento);
        venta.setMetodoPago(metodoPago);

        if (clienteId != null) {
            Cliente cliente = clienteService.getClienteById(clienteId).orElse(null);
            if (cliente != null) {
                venta.setCliente(cliente);
            }
        }

        venta.getProductos().clear();
        for (int i = 0; i < productoIds.size(); i++) {
            Producto producto = productoService.getProductoById(productoIds.get(i)).orElse(null);
            if (producto != null) {
                venta.agregarProducto(producto, cantidades.get(i));
            }
        }

        try {
            ventaService.updateVenta(venta);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "ventas/editar-venta";
        }
        return "redirect:/ventas/listar";
    }

    // Eliminar venta
    @PostMapping("/eliminar/{id}")
    public String eliminarVenta(@PathVariable Long id) {
        try {
            ventaService.deleteVenta(id);
            return "redirect:/ventas/listar";
        } catch (EntityNotFoundException e) {
            return "redirect:/error";
        }
    }

    @GetMapping("/fechas")
    public ResponseEntity<Map<String, Object>> obtenerTotalesPorFecha(
            @RequestParam(name = "fecha", required = false) String fechaStr) {

        // Si no se pasa fecha, usamos la fecha actual
        LocalDate fechaLocal = (fechaStr == null) ? LocalDate.now() : LocalDate.parse(fechaStr);

        // Convertimos LocalDate a Date para la hora 00:00:00 (inicio del día)
        Date inicioFecha = Date.from(fechaLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Convertimos LocalDate a Date para la hora 23:59:59 (fin del día)
        Date finFecha = Date.from(fechaLocal.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minus(1, ChronoUnit.MILLIS));

        // Buscamos las ventas en ese rango de fechas
        List<Venta> ventas = ventaRepository.findByFechaBetween(inicioFecha, finFecha);

        // Calcular los totales por método de pago
        Map<MetodoPago, Double> totales = new HashMap<>();
        for (MetodoPago metodo : MetodoPago.values()) {
            double total = ventas.stream()
                    .filter(v -> v.getMetodoPago() == metodo)
                    .mapToDouble(Venta::getTotal)
                    .sum();
            totales.put(metodo, total);
        }

        // Construir la respuesta como un Map
        Map<String, Object> response = new HashMap<>();
        response.put("totalEfectivo", totales.getOrDefault(MetodoPago.EFECTIVO, 0.0));
        response.put("totalCredito", totales.getOrDefault(MetodoPago.CREDITO, 0.0));
        response.put("totalDebito", totales.getOrDefault(MetodoPago.DEBITO, 0.0));
        response.put("totalMercadoPago", totales.getOrDefault(MetodoPago.MERCADOPAGO, 0.0));
        response.put("totalGeneral", ventas.stream().mapToDouble(Venta::getTotal).sum());

        return ResponseEntity.ok(response); // Devolvemos la respuesta en formato JSON
    }

}
