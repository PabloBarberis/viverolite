package com.vivero.viveroApp.service;

import com.vivero.viveroApp.Repository.PerdidaInventarioRepository;
import com.vivero.viveroApp.model.PerdidaInventario;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PerdidaInventarioService {

    private final PerdidaInventarioRepository perdidaInventarioRepository;
    
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public Page<PerdidaInventario> getAllPerdidas(Pageable pageable) {
        return perdidaInventarioRepository.findAll(pageable);
    }

    @Transactional
    public PerdidaInventario createPerdidaInventario(PerdidaInventario perdidaInventario) {

        perdidaInventario.setFecha(new Date());
        Optional<Producto> prOptional = productoRepository.findById(perdidaInventario.getProducto().getId());
        Producto producto = prOptional.get();
        producto.setStock(producto.getStock() - perdidaInventario.getCantidad());
        productoRepository.save(producto);
        return perdidaInventarioRepository.save(perdidaInventario);
    }

    @Transactional
    public void deletePerdidaInventario(Long id) {
        PerdidaInventario perdida = perdidaInventarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la pérdida de inventario con ID: " + id));

        // Restituir stock del producto afectado
        Optional<Producto> prOptional = productoRepository.findById(perdida.getProducto().getId());
        Producto producto = prOptional.get();
        producto.setStock(producto.getStock() + perdida.getCantidad());
        productoRepository.save(producto);
        // Eliminar la pérdida de inventario
        perdidaInventarioRepository.delete(perdida);
    }

}
