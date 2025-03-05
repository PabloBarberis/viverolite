package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.Venta;
import com.vivero.viveroApp.model.VentaProducto;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.repository.ProductoRepository;
import com.vivero.viveroApp.repository.VentaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class VentaService {

    private final ProductoRepository productoRepository;

    private final VentaRepository ventaRepository;

    private final ProductoService productoService;

    public Optional<Venta> getVentaById(Long id) {
        return ventaRepository.findById(id);
    }

    @Transactional
    public Venta createVenta(Venta venta) {
        validarStock(venta);  // Validación de stock

        for (VentaProducto ventaProducto : venta.getProductos()) {
            Producto producto = ventaProducto.getProducto();
            int nuevaCantidad = producto.getStock() - ventaProducto.getCantidad();
            if (nuevaCantidad < 0) {
                throw new IllegalArgumentException("Cantidad solicitada excede el stock disponible para el producto: " + producto.getNombre());
            }
            producto.setStock(nuevaCantidad);
            productoRepository.save(producto);
        }
        if (venta.getTotal() == null || venta.getTotal() == 0) {
            venta.calcularTotal();
        }
        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta updateVenta(Venta venta) {
        // Buscar la venta original en la base de datos
        Venta ventaExistente = ventaRepository.findById(venta.getId())
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + venta.getId()));

        // Restaurar stock de los productos vendidos anteriormente
        for (VentaProducto ventaProducto : ventaExistente.getProductos()) {
            Producto producto = ventaProducto.getProducto();
            producto.setStock(producto.getStock() + ventaProducto.getCantidad());
            productoRepository.save(producto);
        }

        // Validar y actualizar la nueva cantidad de productos vendidos
        for (VentaProducto ventaProducto : venta.getProductos()) {
            Producto producto = productoRepository.findById(ventaProducto.getProducto().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + ventaProducto.getProducto().getId()));

            int nuevaCantidad = producto.getStock() - ventaProducto.getCantidad();
            if (nuevaCantidad < 0) {
                throw new IllegalArgumentException("Cantidad solicitada excede el stock disponible para el producto: " + producto.getNombre());
            }
            producto.setStock(nuevaCantidad);
            productoRepository.save(producto);
        }

        // Calcular total si no está definido
        if (venta.getTotal() == null || venta.getTotal() == 0) {
            venta.calcularTotal();
        }
        System.out.println("EL ID ES: " + venta.getId());
        // Guardar la venta actualizada
        return ventaRepository.save(venta);
    }

    @Transactional
    public void deleteVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + id));

        // Reponer el stock de los productos asociados a la venta
        for (VentaProducto ventaProducto : venta.getProductos()) {
            Producto producto = ventaProducto.getProducto();

            // Verificar si el producto es una instancia de Producto (o sus subclases)
            if (producto instanceof Producto) {
                // Aquí puedes agregar una lógica específica si deseas manejar alguna subclase en particular,
                // pero en general todas las subclases de Producto deberían manejarse igual (reposicionar stock).
                int cantidadRestante = producto.getStock() + ventaProducto.getCantidad();
                producto.setStock(cantidadRestante);  // Reponer el stock

                // Guardar el producto actualizado
                productoService.saveProducto(producto);
            }
        }

        // Eliminar la venta
        ventaRepository.delete(venta);
    }

    @Transactional(readOnly = true)
    public Page<Venta> getAllVentas(Pageable pageable) {
        return ventaRepository.findAll(pageable);
    }

    // Método para validar el stock de los productos
    private void validarStock(Venta venta) {
        for (VentaProducto ventaProducto : venta.getProductos()) {
            Producto producto = ventaProducto.getProducto();
            if (ventaProducto.getCantidad() > producto.getStock()) {
                throw new IllegalArgumentException("Cantidad solicitada excede el stock disponible para el producto: " + producto.getNombre());
            }
        }
    }
}
