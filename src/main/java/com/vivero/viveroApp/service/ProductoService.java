package com.vivero.viveroApp.service;

import com.vivero.viveroApp.Repository.CompraRepository;
import com.vivero.viveroApp.Repository.ProductoCompraRepository;
import com.vivero.viveroApp.dto.ProductoDTO;
import com.vivero.viveroApp.model.Compra;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.model.ProductoCompra;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.repository.ProductoRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoCompraRepository productoCompraRepository;
    private final CompraRepository compraRepository;

    @Transactional
    public Producto saveProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public List<Producto> getAllProductosActivos() {
        return productoRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> getProductoById(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return productoRepository.existsById(id);
    }

    @Transactional
    public void actualizarPrecios(String accion, String productoStr, double porcentaje, String marca, String interiorExterior) {

        List<Producto> productos;

        if ("Planta".equalsIgnoreCase(productoStr)) {
            productos = productoRepository.findPlantasPorTipo(interiorExterior, marca);
        } else {
            productos = productoRepository.findByTipoAndMarca(productoStr, marca);
        }

        for (Producto producto : productos) {
            double nuevoPrecio;

            if ("Aumento".equalsIgnoreCase(accion)) {
                nuevoPrecio = producto.getPrecio() * (1 + porcentaje / 100);
            } else {
                nuevoPrecio = producto.getPrecio() * (1 - porcentaje / 100);
            }

            long precioRedondeado = redondearPrecio(productoStr, nuevoPrecio);
            producto.setPrecio((double) precioRedondeado);
            productoRepository.save(producto);
        }
    }

    @Transactional
    private long redondearPrecio(String tipoProducto, double precio) {
        long residuo = Math.round(precio) % 100;
        if (residuo <= 30) {
            return Math.round(precio) - residuo;
        } else {
            return Math.round(precio) + (100 - residuo); 
        }
    }

    @Transactional(readOnly = true)
    public List<String> mostrarMarcaPorProducto(String tipo) {
        return productoRepository.findDistinctMarcasByDtype(tipo);
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosPorMarca(String tipoProducto, String marca) {

        if (marca == null || marca.isEmpty()) {
            return productoRepository.findByTipo(tipoProducto);
        } else {
            return productoRepository.findByTipoAndMarca(tipoProducto, marca);
        }
    }

    public void actualizarCampo(Long id, String campo, String valor) {
        switch (campo) {
            case "precio":
                productoRepository.actualizarPrecio(id, Double.valueOf(valor));
                break;
            case "stock":
                productoRepository.actualizarStock(id, Integer.valueOf(valor));
                break;
            default:
                throw new IllegalArgumentException("Campo no válido");
        }
    }

    @Transactional
    public Producto createProducto(Producto producto) {
        for (Proveedor proveedor : producto.getProveedores()) {
            proveedor.getProductos().add(producto);
        }
        return productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public Page<Producto> buscarProductoPaginado(String nombre, String marca, String tipo, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return productoRepository.buscarProducto(
                nombre != null && !nombre.isEmpty() ? nombre : null,
                marca != null && !marca.isEmpty() ? marca : null,
                tipo != null && !tipo.isEmpty() ? tipo : null,
                pageable
        );
    }

    public List<Producto> obtenerProductosFiltrados(String tipo, String nombre, String marca) {

        return productoRepository.findByFiltros(tipo, nombre, marca);
    }

    public void darDeBaja(Long id) {
        Producto producto = getProductoById(id).orElse(null);
        producto.setActivo(false);
        productoRepository.save(producto);
    }
    
    public void ingresarCompra(String comentario, List<ProductoDTO> productos) {
    Compra nuevaCompra = new Compra();
    nuevaCompra.setFecha(LocalDateTime.now());
    nuevaCompra.setComentario(comentario);
    compraRepository.save(nuevaCompra);

    for (ProductoDTO dto : productos) {
        Producto producto = productoRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + dto.getId()));

        // 🔥 Crear el registro en ProductoCompra
        ProductoCompra productoCompra = new ProductoCompra();
        productoCompra.setCompra(nuevaCompra);
        productoCompra.setProducto(producto);
        productoCompra.setCantidad(dto.getCantidad());
        productoCompra.setPrecioCompra(dto.getPrecioCompra() != null ? dto.getPrecioCompra() : (Double) 0.0);

        productoCompra.setPrecioVenta(dto.getPrecio()); 

        productoCompraRepository.save(productoCompra);

        // 🔥 Actualizar stock y precio de venta en el producto
        producto.setStock(producto.getStock() + dto.getCantidad());
        producto.setPrecio(dto.getPrecio());

        productoRepository.save(producto);
    }
}


}
