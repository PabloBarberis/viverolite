package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.Decoracion;
import com.vivero.viveroApp.model.Fertilizante;
import com.vivero.viveroApp.model.Grow;
import com.vivero.viveroApp.model.Herramienta;
import com.vivero.viveroApp.model.Insecticida;
import com.vivero.viveroApp.model.Maceta;
import com.vivero.viveroApp.model.Planta;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.model.Semilla;
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
    public void actualizarPrecios(String accion, String productoStr, double porcentaje, String marca, String interiorExterior) {
        Class<? extends Producto> claseProducto = obtenerClaseProducto(productoStr);
        List<Producto> productos;

        // Si es planta, buscar por interior/exterior
        if ("Planta".equalsIgnoreCase(productoStr)) {
            productos = productoRepository.findPlantasPorTipo(interiorExterior, marca);
        } else {
            // Para otros productos, buscar por tipo y marca
            productos = productoRepository.findByTipoAndMarca(claseProducto, marca);
        }

        // Aplicar la lógica de actualización de precios
        for (Producto producto : productos) {
            double nuevoPrecio;

            if ("Aumento".equalsIgnoreCase(accion)) {
                nuevoPrecio = producto.getPrecio() * (1 + porcentaje / 100);
            } else {
                nuevoPrecio = producto.getPrecio() * (1 - porcentaje / 100);
            }

            // Redondear el precio
            long precioRedondeado = redondearPrecio(productoStr, nuevoPrecio);
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
        switch (tipoProducto) {
            case "Planta" -> {
                return Planta.class;
            }
            case "Maceta" -> {
                return Maceta.class;
            }
            case "Decoracion" -> {
                return Decoracion.class;
            }
            case "Grow" -> {
                return Grow.class;
            }
            case "Tierra" -> {
                return Tierra.class;
            }
            case "Herramienta" -> {
                return Herramienta.class;
            }
            case "Fertilizante" -> {
                return Fertilizante.class;
            }
            case "Semilla" -> {
                return Semilla.class;
            }
            case "Insecticida" -> {
                return Insecticida.class;
            }
            default ->
                throw new IllegalArgumentException("Tipo de producto no válido: " + tipoProducto);
        }
    }

    @Transactional(readOnly = true)
    public List<String> mostrarMarcaPorProducto(String tipo) {
        return productoRepository.findDistinctMarcasByDtype(tipo);
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosPorMarca(String tipoProducto, String marca) {
        Class<? extends Producto> clase = obtenerClaseProducto(tipoProducto);
        
        if (marca == null || marca.isEmpty()) {
            return productoRepository.findByTipo(clase);
        } else {
            return productoRepository.findByTipoAndMarca(clase, marca);
        }
    }

}
