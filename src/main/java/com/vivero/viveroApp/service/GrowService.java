package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.Grow;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.repository.GrowRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GrowService {

    private final GrowRepository growRepository;

    // Obtener todos los productos Grow activos con paginación
    @Transactional(readOnly = true)
    public Page<Grow> getAllGrowActivosPaginados(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return growRepository.findByActivoTrue(pageable);
    }

    // Obtener todos los productos Grow con paginación (activos e inactivos)
    @Transactional(readOnly = true)
    public Page<Grow> getAllGrowPaginados(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return growRepository.findAll(pageable);
    }

    // Obtener un producto Grow por ID si está activo
    @Transactional(readOnly = true)
    public Optional<Grow> getGrowById(Long id) {
        return growRepository.findByIdAndActivoTrue(id);
    }

    // Obtener un producto Grow por ID (incluso si está inactivo)
    @Transactional(readOnly = true)
    public Optional<Grow> getGrowByIdIncluyendoInactivos(Long id) {
        return growRepository.findById(id);
    }

    // Crear un nuevo producto Grow
    @Transactional
    public Grow createGrow(Grow grow) {
        grow.setActivo(true); // Asegura que el nuevo producto Grow esté activo por defecto
        for (Proveedor proveedor : grow.getProveedores()) {
            proveedor.getProductos().add(grow); // Agregar el producto Grow a la lista de productos del proveedor
        }
        return growRepository.save(grow);
    }

    // Actualizar un producto Grow existente
    @Transactional
    public Grow updateGrow(Long id, Grow growDetails) {
        Grow grow = growRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto Grow no encontrado con ID: " + id));

        grow.setNombre(growDetails.getNombre());
        grow.setMarca(growDetails.getMarca());
        grow.setPrecio(growDetails.getPrecio());
        grow.setStock(growDetails.getStock());
        grow.setDescripcion(growDetails.getDescripcion());

        // Actualizar los proveedores
        if (growDetails.getProveedores() != null) {
            for (Proveedor proveedor : grow.getProveedores()) {
                proveedor.getProductos().remove(grow);
            }
            grow.getProveedores().clear();

            for (Proveedor proveedor : growDetails.getProveedores()) {
                grow.getProveedores().add(proveedor);
                proveedor.getProductos().add(grow);
            }
        } else {
            for (Proveedor proveedor : grow.getProveedores()) {
                proveedor.getProductos().remove(grow);
            }
            grow.getProveedores().clear();
        }

        return growRepository.save(grow);
    }

    // Dar de baja un producto Grow (marcar como inactivo)
    @Transactional
    public void darDeBajaGrow(Long id) {
        Grow grow = growRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto Grow no encontrado con ID: " + id));
        grow.setActivo(false); // Marca el producto Grow como inactivo
        growRepository.save(grow);
    }

    // Buscar productos Grow por nombre, marca y proveedor, y solo traer los activos
    @Transactional(readOnly = true)
    public Page<Grow> buscarGrowPaginado(String keyword, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        if (keyword != null && !keyword.isEmpty()) {
            return growRepository.buscarGrow(keyword, pageable);
        } else {
            return getAllGrowActivosPaginados(pageNumber, pageSize);
        }
    }

    @Transactional(readOnly = true)
    public List<Grow> getAllGrowActivos() {
        return growRepository.findByActivoTrue();
    }
}
