package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.Decoracion;
import com.vivero.viveroApp.model.Grow;
import com.vivero.viveroApp.model.Maceta;
import com.vivero.viveroApp.model.Planta;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.model.Tierra;
import com.vivero.viveroApp.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
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
        @Transactional
    public void aumentarPrecios(String tipoProducto, double porcentaje) {
        Class<? extends Producto> claseProducto = obtenerClaseProducto(tipoProducto);
        List<Producto> productos = productoRepository.findByTipo(claseProducto);

        for (Producto producto : productos) {
            // Calcular el nuevo precio
            double nuevoPrecio = producto.getPrecio() * (1 + porcentaje / 100);

            // Redondear el precio según el tipo de producto
            long precioRedondeado = redondearPrecio(tipoProducto, nuevoPrecio);

            // Actualizar el precio
            producto.setPrecio((double) precioRedondeado);
            productoRepository.save(producto);
        }
    }
    @Transactional
    public void aplicarDescuento(String tipoProducto, double porcentaje) {
        Class<? extends Producto> claseProducto = obtenerClaseProducto(tipoProducto);
        List<Producto> productos = productoRepository.findByTipo(claseProducto);

        for (Producto producto : productos) {
            // Calcular el nuevo precio con el descuento
            double nuevoPrecio = producto.getPrecio() * (1 - porcentaje / 100);

            // Redondear el precio según el tipo de producto
            long precioRedondeado = redondearPrecio(tipoProducto, nuevoPrecio);

            // Actualizar el precio
            producto.setPrecio((double) precioRedondeado);
            productoRepository.save(producto);
        }
    }

        @Transactional
    private long redondearPrecio(String tipoProducto, double precio) {
        if ("planta".equalsIgnoreCase(tipoProducto) || "tierra".equalsIgnoreCase(tipoProducto)) {
            // Redondeo especial para tierras: 30 hacia abajo, 31 hacia arriba
            long residuo = Math.round(precio) % 100; // Obtener los decimales de la centena
            if (residuo <= 30) {
                return Math.round(precio) - residuo; // Redondear hacia abajo
            } else {
                return Math.round(precio) + (100 - residuo); // Redondear hacia arriba
            }
        } else {
            // Redondeo normal a la decena más cercana
            long residuo = Math.round(precio) % 10; // Obtener los decimales de la decena
            if (residuo <= 3) {
                return Math.round(precio) - residuo; // Redondear hacia abajo
            } else {
                return Math.round(precio) + (10 - residuo); // Redondear hacia arriba
            }
        }
    }
    @Transactional(readOnly = true)
    private Class<? extends Producto> obtenerClaseProducto(String tipoProducto) {
        switch (tipoProducto.toLowerCase()) {
            case "planta":
                return Planta.class;
            case "maceta":
                return Maceta.class;
            case "decoracion":
                return Decoracion.class;
            case "grow":
                return Grow.class;
            case "tierra":
                return Tierra.class;
            default:
                throw new IllegalArgumentException("Tipo de producto no válido: " + tipoProducto);
        }
    }

}
