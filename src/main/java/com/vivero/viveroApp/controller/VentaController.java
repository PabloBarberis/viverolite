package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.dto.VentaProyeccion;
import com.vivero.viveroApp.model.Cliente;
import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.Venta;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.model.enums.Descuento;
import com.vivero.viveroApp.model.enums.MetodoPago;
import com.vivero.viveroApp.repository.VentaRepository;
import com.vivero.viveroApp.service.VentaService;
import com.vivero.viveroApp.service.ProductoService;
import com.vivero.viveroApp.service.ClienteService;
import com.vivero.viveroApp.service.IngresoEgresoService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;
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

    private final IngresoEgresoService ingresoEgresoService;

    // Mostrar formulario para crear una nueva venta
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {

        List<Producto> productos = productoService.getAllProductosActivos();
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

        venta.setFecha(LocalDateTime.now());
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
        List<Producto> productos = productoService.getAllProductosActivos();
        List<Cliente> clientes = clienteService.getAllClientesActivos();
        model.addAttribute("productos", productos);
        model.addAttribute("clientes", clientes);
        model.addAttribute("metodosPago", MetodoPago.values());
        model.addAttribute("descuentos", Descuento.values());
        model.addAttribute("venta", venta);
        System.out.println("ANTES DE CARGAR LA PAGINA EL ID DE LA VENTA ES: " + venta.getId());
        return "ventas/editar-venta";
    }

    @PostMapping("/actualizar")
    public String actualizarVenta(@RequestParam Long id,
            @RequestParam String fecha,
            @RequestParam String hora,
            @RequestParam List<Long> productoIds,
            @RequestParam List<Integer> cantidades,
            @RequestParam Descuento descuento,
            @RequestParam MetodoPago metodoPago,
            @RequestParam(required = false) Long clienteId,
            @RequestParam String total,
            Model model) {
        try {
            double totalDouble = Double.parseDouble(total.replace(",", "."));

            // Buscar la venta existente
            Venta ventaExistente = ventaService.getVentaById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + id));

            // Convertir fecha y hora a LocalDateTime
            LocalDateTime fechaHora = LocalDateTime.of(LocalDate.parse(fecha), LocalTime.parse(hora));
            ventaExistente.setFecha(fechaHora);
            ventaExistente.setTotal(totalDouble);
            ventaExistente.setDescuento(descuento);
            ventaExistente.setMetodoPago(metodoPago);

            // Actualizar cliente si existe
            if (clienteId != null) {
                Cliente cliente = clienteService.getClienteById(clienteId).orElse(null);
                if (cliente != null) {
                    ventaExistente.setCliente(cliente);
                }
            }

            // Limpiar y actualizar productos
            ventaExistente.getProductos().clear();
            for (int i = 0; i < productoIds.size(); i++) {
                Producto producto = productoService.getProductoById(productoIds.get(i)).orElse(null);
                if (producto != null) {
                    ventaExistente.agregarProducto(producto, cantidades.get(i));
                }
            }

            // Guardar la venta actualizada
            ventaService.updateVenta(ventaExistente);

        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "El formato del total no es válido.");
            return "ventas/editar-venta";
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

        // Convertimos LocalDate a LocalDateTime para la hora 00:00:00 (inicio del día)
        LocalDateTime inicioFecha = fechaLocal.atStartOfDay();

        // Convertimos LocalDate a LocalDateTime para la hora 23:59:59 (fin del día)
        LocalDateTime finFecha = fechaLocal.atTime(23, 59, 59);

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

        // Construir la respuesta con los nuevos métodos de pago
        Map<String, Object> response = new HashMap<>();
        response.put("totalEfectivo", totales.getOrDefault(MetodoPago.EFECTIVO, 0.0));
        response.put("totalCredito", totales.getOrDefault(MetodoPago.CREDITO, 0.0));
        response.put("totalDebito", totales.getOrDefault(MetodoPago.DEBITO, 0.0));
        response.put("totalMercadoPagoVal", totales.getOrDefault(MetodoPago.MERCADOPAGO_VAL, 0.0));
        response.put("totalMercadoPagoSac", totales.getOrDefault(MetodoPago.MERCADOPAGO_SAC, 0.0));
        response.put("totalGeneral", ventas.stream().mapToDouble(Venta::getTotal).sum());

        return ResponseEntity.ok(response); // Devolvemos la respuesta en formato JSON
    }

    @GetMapping("/entrada-salida")
    public String mostrarEntradaSalida() {
        return "ventas/entrada-salida";  // Esto devolverá la plantilla de HTML ubicada en resources/templates/venta/entrada-salida.html
    }

    @GetMapping("/efectivo")
    public List<VentaProyeccion> obtenerVentasEfectivo() {
        return ventaService.obtenerVentasPorMetodoPago(MetodoPago.EFECTIVO);
    }

    @GetMapping("/ventas-gastos")
    @ResponseBody
    public Map<String, Object> obtenerVentasYGastos(@RequestParam int mes, @RequestParam int anio) {
        Map<String, Object> response = new HashMap<>();

        // Obtener ventas filtradas por mes y año
        List<Venta> ventas = ventaService.obtenerVentasPorMesYAnio(mes, anio);
        System.out.println("");
        System.out.println("TAMAÑO VENTAS" + ventas.size());
        for (Venta venta : ventas) {
            System.out.println(venta.getTotal() + " " + venta.getMetodoPago());
        }
        System.out.println("");
        // Obtener ingresos y egresos filtrados por mes y año
        List<IngresoEgreso> ingresosEgresos = ingresoEgresoService.obtenerIngresosEgresosPorMesYAnio(mes, anio);
        System.out.println("");
        System.out.println("TAMAÑO INGRESOS" + ingresosEgresos.size());
        for (IngresoEgreso ingresosEgreso : ingresosEgresos) {
            System.out.println(ingresosEgreso.getMonto() + " " + ingresosEgreso.getMetodoPago());;
        }
        System.out.println("");
        response.put("ventas", ventas);
        response.put("ingresosEgresos", ingresosEgresos);

        return response;
    }

}
