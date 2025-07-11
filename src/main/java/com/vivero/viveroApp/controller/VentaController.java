package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.Repository.IngresoEgresoRepository;
import com.vivero.viveroApp.dto.MetodoPagoDTO;
import com.vivero.viveroApp.dto.VentaDTO;
import com.vivero.viveroApp.model.Cliente;
import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.PagoVenta;
import com.vivero.viveroApp.model.Venta;
import com.vivero.viveroApp.model.enums.MetodoPago;
import com.vivero.viveroApp.Repository.VentaRepository;
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
import java.util.*;
import java.util.function.Function;
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

    @GetMapping("/reporte-productos")
    public ResponseEntity<byte[]> generarReporteProductos(
            @RequestParam int diaInicio,
            @RequestParam int mesInicio,
            @RequestParam int anioInicio,
            @RequestParam int diaFin,
            @RequestParam int mesFin,
            @RequestParam int anioFin) {
        try {
            byte[] pdf = ventaService.generarReporteProductosVendidos(
                    diaInicio, mesInicio, anioInicio,
                    diaFin, mesFin, anioFin);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_productos_vendidos.pdf");

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    ///////////////////////////////////////////////////
    @GetMapping("/movimientos")
    public ResponseEntity<Map<String, Object>> obtenerMovimientos(
            @RequestParam int mes,
            @RequestParam int anio,
            @RequestParam(defaultValue = "todos") String metodoPago) {

        LocalDate inicioMesActual = LocalDate.of(anio, mes, 1);
        LocalDate finMesActual = inicioMesActual.withDayOfMonth(inicioMesActual.lengthOfMonth());
        LocalDateTime desdeMesActual = inicioMesActual.atStartOfDay();
        LocalDateTime hastaMesActual = finMesActual.atTime(23, 59, 59);

        List<Venta> ventas = ventaRepository.findByFechaBetween(desdeMesActual, hastaMesActual);
        List<IngresoEgreso> movimientos = ingresoEgresoRepository.findByFechaBetweenOrderByFechaAsc(desdeMesActual, hastaMesActual);

        boolean todosLosMetodos = "todos".equalsIgnoreCase(metodoPago);

        // Filtrar movimientos según método de pago
        List<IngresoEgreso> movimientosFiltrados = movimientos.stream()
                .filter(ie -> todosLosMetodos || ie.getMetodoPago().name().equals(metodoPago))
                .toList();

        // Calcular ventas por semana
        Map<String, Double> ventasPorSemana = inicializarMapSemana();
        double totalVentas = 0.0;

        for (Venta venta : ventas) {
            for (PagoVenta pago : venta.getPagos()) {
                if (todosLosMetodos || pago.getMetodo().name().equals(metodoPago)) {
                    int dia = venta.getFecha().getDayOfMonth();
                    sumarAlPeriodo(ventasPorSemana, dia, pago.getMonto());
                    totalVentas += pago.getMonto();
                }
            }
        }

        // Calcular ingresos y egresos
        double totalIngresos = movimientosFiltrados.stream()
                .filter(IngresoEgreso::getIngreso)
                .mapToDouble(IngresoEgreso::getMonto)
                .sum();

        double totalEgresos = movimientosFiltrados.stream()
                .filter(ie -> !ie.getIngreso())
                .mapToDouble(IngresoEgreso::getMonto)
                .sum();

        // Saldo del mes anterior
        LocalDate inicioMesAnterior = inicioMesActual.minusMonths(1);
        LocalDateTime desdeMesAnterior = inicioMesAnterior.withDayOfMonth(1).atStartOfDay();
        LocalDateTime hastaMesAnteriorFin = inicioMesAnterior.withDayOfMonth(inicioMesAnterior.lengthOfMonth()).atTime(23, 59, 59);

        List<Venta> ventasMesAnterior = ventaRepository.findByFechaBetween(desdeMesAnterior, hastaMesAnteriorFin);
        List<IngresoEgreso> movimientosMesAnterior = ingresoEgresoRepository.findByFechaBetweenOrderByFechaAsc(desdeMesAnterior, hastaMesAnteriorFin);

        double totalVentasMesAnterior = ventasMesAnterior.stream()
                .flatMap(v -> v.getPagos().stream())
                .filter(p -> todosLosMetodos || p.getMetodo().name().equals(metodoPago))
                .mapToDouble(PagoVenta::getMonto)
                .sum();

        double totalIngresosMesAnterior = movimientosMesAnterior.stream()
                .filter(ie -> ie.getIngreso() && (todosLosMetodos || ie.getMetodoPago().name().equals(metodoPago)))
                .mapToDouble(IngresoEgreso::getMonto)
                .sum();

        double totalEgresosMesAnterior = movimientosMesAnterior.stream()
                .filter(ie -> !ie.getIngreso() && (todosLosMetodos || ie.getMetodoPago().name().equals(metodoPago)))
                .mapToDouble(IngresoEgreso::getMonto)
                .sum();

        double saldoMesAnterior = totalVentasMesAnterior + totalIngresosMesAnterior - totalEgresosMesAnterior;

        // Saldo de meses anteriores (todo antes del mes seleccionado)
        LocalDateTime hastaMesAnterior = inicioMesActual.minusDays(1).atTime(23, 59, 59);
        List<Venta> todasLasVentasAnteriores = ventaRepository.findByFechaBefore(hastaMesAnterior);
        List<IngresoEgreso> todosLosIngresosEgresosAnteriores = ingresoEgresoRepository.findByFechaBefore(hastaMesAnterior);

        double totalVentasAcumuladas = todasLasVentasAnteriores.stream()
                .flatMap(v -> v.getPagos().stream())
                .filter(p -> todosLosMetodos || p.getMetodo().name().equals(metodoPago))
                .mapToDouble(PagoVenta::getMonto)
                .sum();

        double totalIngresosAcumulados = todosLosIngresosEgresosAnteriores.stream()
                .filter(ie -> ie.getIngreso() && (todosLosMetodos || ie.getMetodoPago().name().equals(metodoPago)))
                .mapToDouble(IngresoEgreso::getMonto)
                .sum();

        double totalEgresosAcumulados = todosLosIngresosEgresosAnteriores.stream()
                .filter(ie -> !ie.getIngreso() && (todosLosMetodos || ie.getMetodoPago().name().equals(metodoPago)))
                .mapToDouble(IngresoEgreso::getMonto)
                .sum();

        double saldoMesesAnteriores = totalVentasAcumuladas + totalIngresosAcumulados - totalEgresosAcumulados;

        // Totales generales
        double totalDelMes = totalVentas + totalIngresos - totalEgresos;
        double totalGeneral = totalDelMes + saldoMesesAnteriores;

        // Devolver solo datos necesarios
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("saldoMesesAnteriores", saldoMesesAnteriores);
        resumen.put("saldoMesAnterior", saldoMesAnterior);
        resumen.put("ventasPorSemana", ventasPorSemana);
        resumen.put("totalVentas", totalVentas);
        resumen.put("totalIngresos", totalIngresos);
        resumen.put("totalEgresos", totalEgresos);
        resumen.put("totalDelMes", totalDelMes);
        resumen.put("totalGeneral", totalGeneral);

        // Movimientos listos para tabla
        List<Map<String, Object>> movimientosDto = movimientosFiltrados.stream()
                .map(this::toDto)
                .toList();

        // Respuesta final
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("resumen", resumen);
        respuesta.put("movimientos", movimientosDto);

        return ResponseEntity.ok(respuesta);
    }

    private Map<String, Double> inicializarMapSemana() {
        Map<String, Double> map = new HashMap<>();
        map.put("1 al 7", 0.0);
        map.put("8 al 14", 0.0);
        map.put("15 al 21", 0.0);
        map.put("22 al 28", 0.0);
        map.put("29 al fin de mes", 0.0);
        return map;
    }

    private void sumarAlPeriodo(Map<String, Double> map, int dia, double monto) {
        if (dia <= 7) {
            map.compute("1 al 7", (k, v) -> v + monto);
        } else if (dia <= 14) {
            map.compute("8 al 14", (k, v) -> v + monto);
        } else if (dia <= 21) {
            map.compute("15 al 21", (k, v) -> v + monto);
        } else if (dia <= 28) {
            map.compute("22 al 28", (k, v) -> v + monto);
        } else {
            map.compute("29 al fin de mes", (k, v) -> v + monto);
        }
    }

    private Map<String, Object> toDto(IngresoEgreso ie) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", ie.getId());
        dto.put("fecha", ie.getFecha().toLocalDate().toString());
        dto.put("descripcion", ie.getDescripcion());
        dto.put("tipoMovimiento", ie.getIngreso());
        dto.put("metodoPago", ie.getMetodoPago());
        dto.put("monto", ie.getMonto());
        dto.put("usuario", Map.of("nombre", ie.getUsuario().getNombre()));
        return dto;
    }
}
