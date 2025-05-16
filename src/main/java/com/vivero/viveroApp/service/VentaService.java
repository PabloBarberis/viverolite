package com.vivero.viveroApp.service;

import com.vivero.viveroApp.dto.MetodoPagoDTO;
import com.vivero.viveroApp.dto.VentaDTO;
import com.vivero.viveroApp.dto.VentaProductoDTO;
import com.vivero.viveroApp.model.Cliente;
import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.PagoVenta;
import com.vivero.viveroApp.model.Venta;
import com.vivero.viveroApp.model.VentaProducto;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.model.enums.MetodoPago;
import com.vivero.viveroApp.repository.ClienteRepository;
import com.vivero.viveroApp.repository.ProductoRepository;
import com.vivero.viveroApp.repository.VentaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class VentaService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final ProductoService productoService;
    private final ClienteRepository clienteRepository;
    private final IngresoEgresoService ingresoEgresoService;
    private final PdfService pdfService;

    public Optional<Venta> getVentaById(Long id) {
        return ventaRepository.findById(id);
    }

    @Transactional
    public Venta createVenta(VentaDTO ventaDTO) {
        Cliente cliente = null;
        if (ventaDTO.getClienteId() != null) {
            cliente = clienteRepository.findById(ventaDTO.getClienteId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
        }

        Venta nuevaVenta = new Venta(cliente, new ArrayList<>(), 0.0);

        List<PagoVenta> pagos = new ArrayList<>();
        for (MetodoPagoDTO metodoPagoDTO : ventaDTO.getPagos()) {
            PagoVenta pago = new PagoVenta();
            pago.setMetodo(metodoPagoDTO.getMetodo());
            pago.setMonto(metodoPagoDTO.getMonto());
            pago.setVenta(nuevaVenta);
            pagos.add(pago);
        }

        nuevaVenta.setPagos(pagos);

        for (VentaProductoDTO ventaProductoDTO : ventaDTO.getProductos()) {
            Producto producto = productoRepository.findById(ventaProductoDTO.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

            VentaProducto ventaProducto = new VentaProducto();
            ventaProducto.setProducto(producto);
            producto.setStock(producto.getStock() - ventaProductoDTO.getCantidad());
            ventaProducto.setCantidad(ventaProductoDTO.getCantidad());
            ventaProducto.setPrecioOriginal(producto.getPrecio());
            ventaProducto.setPorcentajeDescuento(ventaProductoDTO.getDescuentoProducto());
            ventaProducto.setSubtotal(ventaProducto.getPrecioOriginal() * ventaProducto.getCantidad() * (1 - (ventaProducto.getPorcentajeDescuento() != null
                    ? ventaProducto.getPorcentajeDescuento() : 0) / 100.0));
            nuevaVenta.agregarVentaProducto(ventaProducto);
        }

        nuevaVenta.calcularTotal();

        return ventaRepository.save(nuevaVenta);
    }

    @Transactional
    public Venta updateVenta(VentaDTO ventaDTO) {

        // 1. Buscar la venta original
        Venta ventaExistente = ventaRepository.findById(ventaDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + ventaDTO.getId()));

        if (ventaDTO.getClienteId() != null) {
            try {
                Optional<Cliente> clienteOpt = clienteRepository.findByIdAndActivoTrue(ventaDTO.getClienteId());
                Cliente cliente = clienteOpt.orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
                ventaExistente.setCliente(cliente);
            } catch (Exception e) {

            }
        }

        // 2. Restaurar stock de productos anteriores
        for (VentaProducto ventaProd : ventaExistente.getProductos()) {
            Long productoId = ventaProd.getProducto().getId();
            int cantidadVendida = ventaProd.getCantidad();
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + productoId));
            int stockAntes = producto.getStock();
            producto.setStock(stockAntes + cantidadVendida);
            productoRepository.save(producto);

        }

        // 3. Limpiar listas
        ventaExistente.getProductos().clear();
        ventaExistente.getPagos().clear();

        // 4. Actualizar cliente y fecha/hora
        LocalDateTime fechaHora = LocalDateTime.of(LocalDate.parse(ventaDTO.getFecha()), LocalTime.parse(ventaDTO.getHora()));
        ventaExistente.setFecha(fechaHora);

        // 5. Validar y agregar nuevos productos
        for (VentaProductoDTO ventaProductoDTO : ventaDTO.getProductos()) {
            Long productoDTOId = ventaProductoDTO.getProductoId();
            Integer cantidadDTO = ventaProductoDTO.getCantidad();

            Optional<Producto> productoOpt = productoRepository.findById(productoDTOId);
            if (productoOpt.isEmpty()) {

                throw new EntityNotFoundException("Producto no encontrado con id: " + productoDTOId);
            }
            Producto producto = productoOpt.get();

            if (productoDTOId == null || cantidadDTO == null) {
                throw new IllegalArgumentException("Producto ID o cantidad no pueden ser nulos");
            }

            int stockAntesDescuento = producto.getStock();
            int nuevoStock = stockAntesDescuento - cantidadDTO;

            if (nuevoStock < 0) {
                throw new IllegalArgumentException("Cantidad solicitada excede el stock disponible para el producto: " + producto.getNombre());
            }

            // Descontar stock
            producto.setStock(nuevoStock);
            productoRepository.save(producto);

            VentaProducto ventaProducto = new VentaProducto();
            ventaProducto.setProducto(producto);
            ventaProducto.setCantidad(cantidadDTO);
            ventaProducto.setPrecioOriginal(producto.getPrecio());
            Double descuento = ventaProductoDTO.getDescuentoProducto() != null ? ventaProductoDTO.getDescuentoProducto() : 0;
            ventaProducto.setPorcentajeDescuento(descuento);
            double subtotal = ventaProducto.getPrecioOriginal() * cantidadDTO * (1 - descuento / 100.0);
            ventaProducto.setSubtotal(subtotal);
            ventaExistente.agregarVentaProducto(ventaProducto);

        }

        // 6. Agregar pagos
        for (MetodoPagoDTO pagoDTO : ventaDTO.getPagos()) {
            if (pagoDTO.getMetodo() == null || pagoDTO.getMonto() == null) {
                throw new IllegalArgumentException("Método de pago o monto no pueden ser nulos");
            }
            PagoVenta pago = new PagoVenta();
            pago.setMetodo(pagoDTO.getMetodo());
            pago.setMonto(pagoDTO.getMonto());
            pago.setVenta(ventaExistente);
            ventaExistente.getPagos().add(pago);
        }

        // 7. Calcular total si no está definido
        if (ventaExistente.getTotal() != null || ventaExistente.getTotal() != 0) {
            ventaExistente.calcularTotal();
        } else {
            System.out.println("Total de la venta ya estaba definido: " + ventaExistente.getTotal());
        }

        // 8. Guardar venta actualizada
        Venta ventaGuardada = ventaRepository.save(ventaExistente);

        return ventaGuardada;
    }

    @Transactional
    public void deleteVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + id));

        // Reponer stock de cada producto en la venta
        for (VentaProducto ventaProducto : venta.getProductos()) {
            Producto producto = ventaProducto.getProducto();
            producto.setStock(producto.getStock() + ventaProducto.getCantidad());
            productoService.saveProducto(producto);
        }

        // Eliminar la venta
        ventaRepository.delete(venta);
    }

    @Transactional(readOnly = true)
    public Page<Venta> getAllVentas(Pageable pageable) {
        return ventaRepository.findAll(pageable);
    }

    public List<Venta> obtenerVentasPorMesYAnio(int mes, int anio) {
        String mesStr = String.format("%02d", mes); // Convierte 3 → "03"
        String anioStr = String.valueOf(anio); // Convierte 2025 → "2025"
        return ventaRepository.obtenerVentasPorMesYAnio(mesStr, anioStr);
    }

    public byte[] generarReportePdf(int mes, int anio) throws Exception {
        List<Venta> ventasDelMes = obtenerVentasPorMesYAnio(mes, anio);
        List<IngresoEgreso> ingresosEgresos = ingresoEgresoService.obtenerIngresosEgresosPorMesYAnio(mes, anio);

        Map<String, Integer> productosVendidos = new TreeMap<>();
        Map<DayOfWeek, Integer> ventasPorDia = new HashMap<>();
        Map<String, Integer> comprasPorCliente = new HashMap<>();
        Map<String, Double> gastoPorCliente = new HashMap<>();
        double totalVentas = 0.0;
        double totalGastos = 0.0;

        for (Venta venta : ventasDelMes) {
            for (VentaProducto vp : venta.getProductos()) {
                String nombreProducto = vp.getProducto().getNombre();
                productosVendidos.put(nombreProducto, productosVendidos.getOrDefault(nombreProducto, 0) + vp.getCantidad());
            }

            DayOfWeek dia = venta.getFecha().getDayOfWeek();
            ventasPorDia.put(dia, ventasPorDia.getOrDefault(dia, 0) + 1);

            if (venta.getCliente() != null) {
                String nombreCliente = venta.getCliente().getNombre();
                comprasPorCliente.put(nombreCliente, comprasPorCliente.getOrDefault(nombreCliente, 0) + 1);
                gastoPorCliente.put(nombreCliente, gastoPorCliente.getOrDefault(nombreCliente, 0.0) + venta.getTotal());
            }

            totalVentas += venta.getTotal();
        }

        for (IngresoEgreso movimiento : ingresosEgresos) {
            if (Boolean.FALSE.equals(movimiento.getIngreso())) {
                totalGastos += movimiento.getMonto();
            }
        }

        double granTotal = totalVentas - totalGastos;       
                
        Map<MetodoPago, Double> totales = new HashMap<>();
        
        for (MetodoPago metodo : MetodoPago.values()) {
            double total = ventasDelMes.stream()
                    .flatMap(v -> v.getPagos().stream())
                    .filter(p -> p.getMetodo() == metodo)
                    .mapToDouble(pago -> {
                        boolean aplicaRecargo = metodo == MetodoPago.CREDITO;
                        return aplicaRecargo ? pago.getMonto() * 1.15 : pago.getMonto();
                    })
                    .sum();
            totales.put(metodo, total);
        }
        
        return pdfService.generarReporteVentas(productosVendidos, ventasPorDia, comprasPorCliente,
                gastoPorCliente, totalVentas, totalGastos, granTotal, mes, anio, totales);
    }
    
   public byte[] generarReporteProductosVendidos(int diaInicio, int mesInicio, int anioInicio,
                                              int diaFin, int mesFin, int anioFin) throws Exception {

    LocalDateTime inicio = LocalDateTime.of(anioInicio, mesInicio, diaInicio, 0, 0);
    LocalDateTime fin = LocalDateTime.of(anioFin, mesFin, diaFin, 23, 59, 59);
    long fechaInicioEpoch = inicio.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    long fechaFinEpoch = fin.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    List<Venta> ventas = ventaRepository.obtenerVentasEntreFechas(fechaInicioEpoch, fechaFinEpoch);

    Map<String, Map<String, Integer>> productosPorProveedor = new TreeMap<>();

    for (Venta venta : ventas) {
        for (VentaProducto vp : venta.getProductos()) {
            Producto producto = vp.getProducto();
            String nombreProducto = producto.getNombre();
            int cantidad = vp.getCantidad();

            List<Proveedor> proveedores = producto.getProveedores();
            String nombreProveedor;

            if (proveedores == null || proveedores.isEmpty()) {
                nombreProveedor = "Sin proveedor";
            } else {
                nombreProveedor = proveedores.get(0).getNombre();
            }

            Map<String, Integer> productosDelProveedor = productosPorProveedor
                    .computeIfAbsent(nombreProveedor, k -> new TreeMap<>());

            productosDelProveedor.put(
                nombreProducto,
                productosDelProveedor.getOrDefault(nombreProducto, 0) + cantidad
            );
        }
    }

    return pdfService.generarReporteProductosVendidos(productosPorProveedor, inicio, fin);
}



}
