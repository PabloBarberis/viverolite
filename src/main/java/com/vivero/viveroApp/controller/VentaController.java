package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.Repository.IngresoEgresoRepository;
import com.vivero.viveroApp.dto.MetodoPagoDTO;
import com.vivero.viveroApp.dto.VentaDTO;
import com.vivero.viveroApp.model.Cliente;
import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.Venta;
import com.vivero.viveroApp.model.enums.MetodoPago;
import com.vivero.viveroApp.repository.VentaRepository;
import com.vivero.viveroApp.service.VentaService;
import com.vivero.viveroApp.service.ClienteService;
import com.vivero.viveroApp.service.IngresoEgresoService;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final VentaRepository ventaRepository;
    private final ClienteService clienteService;
    private final IngresoEgresoService ingresoEgresoService;
    private final IngresoEgresoRepository ingresoEgresoRepository;

    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {

        //List<Producto> productos = productoService.getAllProductosActivos();
        List<Cliente> clientes = clienteService.getAllClientesActivos();

        //model.addAttribute("productos", productos);
        model.addAttribute("clientes", clientes);
        model.addAttribute("metodosPago", MetodoPago.values());
        model.addAttribute("ventaDTO", new VentaDTO());

        return "ventas/crear-venta";
    }

    @PostMapping("/crear")
    public ResponseEntity<Map<String, String>> crearVenta(@RequestBody VentaDTO ventaDTO) {

        Map<String, String> response = new HashMap<>();
        try {

            if (ventaDTO.getProductos() == null || ventaDTO.getPagos() == null) {
                response.put("error", "Los datos de productos o pagos son nulos.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            ventaService.createVenta(ventaDTO);
            response.put("message", "Venta creada correctamente");
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            response.put("error", "Error al crear la venta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("error", "Error interno en el servidor.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Listar ventas
    @GetMapping("/listar")
    public String listarVentas(Model model, @PageableDefault(size = 10, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Venta> ventas = ventaService.getAllVentas(pageable);
        String sortFormatted = pageable.getSort()
                .stream()
                .map(order -> order.getProperty() + "," + order.getDirection()) // Ajustar formato
                .findFirst()
                .orElse("fecha,DESC"); // Valor por defecto
        model.addAttribute("ventas", ventas);
        model.addAttribute("currentPage", ventas.getNumber());
        model.addAttribute("totalPages", ventas.getTotalPages());
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("sort", sortFormatted);

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
        Venta venta = ventaService.getVentaById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + id));

        List<Cliente> clientes = clienteService.getAllClientesActivos();

        List<MetodoPagoDTO> pagos = venta.getPagos().stream()
                .map(p -> new MetodoPagoDTO(p.getMetodo(), p.getMonto()))
                .collect(Collectors.toList());

        //model.addAttribute("productos", productos);
        model.addAttribute("clientes", clientes);
        model.addAttribute("metodosPago", MetodoPago.values());
        model.addAttribute("pagosSeleccionados", pagos);
        model.addAttribute("venta", venta);
        model.addAttribute("fecha", venta.getFecha().toLocalDate());
        model.addAttribute("hora", venta.getFecha().toLocalTime());

        return "ventas/editar-venta";
    }

    @PostMapping("/actualizar")
    public ResponseEntity<Map<String, String>> actualizarVenta(
            @RequestBody VentaDTO ventaDTO
    ) {

        Map<String, String> response = new HashMap<>();
        try {
            if (ventaDTO.getProductos() == null || ventaDTO.getPagos() == null) {
                response.put("error", "Los datos de productos o pagos son nulos.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (ventaDTO.getFecha() == null || ventaDTO.getHora() == null || ventaDTO.getFecha().isEmpty() || ventaDTO.getHora().isEmpty()) {
                response.put("error", "La fecha y la hora son obligatorias.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            ventaService.updateVenta(ventaDTO);

            response.put("message", "Venta actualizada correctamente");
            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            response.put("error", "Error al actualizar la venta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("error", "Error interno en el servidor.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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

        LocalDate fechaLocal = (fechaStr == null) ? LocalDate.now() : LocalDate.parse(fechaStr);
        LocalDateTime inicioFecha = fechaLocal.atStartOfDay();
        LocalDateTime finFecha = fechaLocal.atTime(23, 59, 59);

        List<Venta> ventas = ventaRepository.findByFechaBetween(inicioFecha, finFecha);

        Map<MetodoPago, Double> totales = new HashMap<>();

        for (MetodoPago metodo : MetodoPago.values()) {
            double total = ventas.stream()
                    .flatMap(v -> v.getPagos().stream())
                    .filter(p -> p.getMetodo() == metodo)
                    .mapToDouble(pago -> {

                        boolean aplicaRecargo = metodo == MetodoPago.CREDITO;
                        return aplicaRecargo ? pago.getMonto() * 1.15 : pago.getMonto();
                    })
                    .sum();
            totales.put(metodo, total);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalEfectivo", totales.getOrDefault(MetodoPago.EFECTIVO, 0.0));
        response.put("totalCredito", totales.getOrDefault(MetodoPago.CREDITO, 0.0));
        response.put("totalDebito", totales.getOrDefault(MetodoPago.DEBITO, 0.0));
        response.put("totalMercadoPagoVale", totales.getOrDefault(MetodoPago.MERCADOPAGO_VAL, 0.0));
        response.put("totalMercadoPagoSacha", totales.getOrDefault(MetodoPago.MERCADOPAGO_SAC, 0.0));
        response.put("totalGeneral", ventas.stream().mapToDouble(Venta::getTotal).sum());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/entrada-salida")
    public String mostrarEntradaSalida() {
        return "ventas/entrada-salida";
    }

    @GetMapping("/ventas-gastos")
    @ResponseBody
    public Map<String, Object> obtenerVentasYGastos(@RequestParam int mes, @RequestParam int anio) {

        Map<String, Object> response = new HashMap<>();
        List<Venta> ventas = ventaService.obtenerVentasPorMesYAnio(mes, anio);
        List<IngresoEgreso> ingresosEgresos = ingresoEgresoService.obtenerIngresosEgresosPorMesYAnio(mes, anio);

        response.put("ventas", ventas);
        response.put("ingresosEgresos", ingresosEgresos);

        return response;
    }

    @GetMapping("/movimientos")
    public ResponseEntity<Map<String, Object>> obtenerMovimientosPorMesYAnio(
            @RequestParam int mes,
            @RequestParam int anio) {

        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        LocalDateTime desde = inicio.atStartOfDay();
        LocalDateTime hasta = fin.atTime(23, 59, 59);

        List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta);
        List<IngresoEgreso> ingresosEgresos = ingresoEgresoRepository.findByFechaBetweenOrderByFechaAsc(desde, hasta);

        LocalDate mesAnterior = inicio.minusMonths(1);
        LocalDateTime desdeMesAnterior = mesAnterior.withDayOfMonth(1).atStartOfDay();
        LocalDateTime hastaMesAnterior = mesAnterior.withDayOfMonth(mesAnterior.lengthOfMonth()).atTime(23, 59, 59);

        List<Venta> ventasMesAnterior = ventaRepository.findByFechaBetween(desdeMesAnterior, hastaMesAnterior);
        List<IngresoEgreso> ingresosEgresosMesAnterior = ingresoEgresoRepository.findByFechaBetweenOrderByFechaAsc(desdeMesAnterior, hastaMesAnterior);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("ventas", ventas);
        respuesta.put("ingresosEgresos", ingresosEgresos);
        respuesta.put("ventasMesAnterior", ventasMesAnterior);
        respuesta.put("ingresosEgresosMesAnterior", ingresosEgresosMesAnterior);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/reporte-pdf")
    public ResponseEntity<byte[]> generarReportePdf(
            @RequestParam int mes,
            @RequestParam int anio) {

        try {
            byte[] pdfBytes = ventaService.generarReportePdf(mes, anio);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_ventas_" + anio + "_" + mes + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
