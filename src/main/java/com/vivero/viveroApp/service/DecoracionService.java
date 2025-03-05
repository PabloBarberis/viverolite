package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.Decoracion;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.repository.DecoracionRepository;
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
public class DecoracionService {

    private final DecoracionRepository decoracionRepository;

    @Transactional(readOnly = true)
    public List<Decoracion> getAllDecoraciones() {
        return decoracionRepository.findByActivoTrue();
    }

    // Obtener todas las decoraciones activas con paginación
    @Transactional(readOnly = true)
    public Page<Decoracion> getAllDecoracionesActivasPaginadas(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return decoracionRepository.findByActivoTrue(pageable);
    }

    // Obtener todas las decoraciones con paginación (activas e inactivas)
    @Transactional(readOnly = true)
    public Page<Decoracion> getAllDecoracionesPaginadas(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return decoracionRepository.findAll(pageable);
    }

    // Obtener una decoración por ID si está activa
    @Transactional(readOnly = true)
    public Optional<Decoracion> getDecoracionById(Long id) {
        return decoracionRepository.findByIdAndActivoTrue(id);
    }

    // Obtener una decoración por ID (incluso si está inactiva)
    @Transactional(readOnly = true)
    public Optional<Decoracion> getDecoracionByIdIncluyendoInactivos(Long id) {
        return decoracionRepository.findById(id);
    }

    // Crear una nueva decoración
    @Transactional
    public Decoracion createDecoracion(Decoracion decoracion) {
        decoracion.setActivo(true); // Asegura que la nueva decoración esté activa por defecto
        for (Proveedor proveedor : decoracion.getProveedores()) {
            proveedor.getProductos().add(decoracion); // Agregar la decoración a la lista de productos del proveedor
        }
        return decoracionRepository.save(decoracion);
    }

    // Actualizar decoración existente
    @Transactional
    public Decoracion updateDecoracion(Long id, Decoracion decoracionDetails) {
        Decoracion decoracion = decoracionRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Decoración no encontrada con ID: " + id));

        decoracion.setNombre(decoracionDetails.getNombre());
        decoracion.setMarca(decoracionDetails.getMarca());
        decoracion.setPrecio(decoracionDetails.getPrecio());
        decoracion.setStock(decoracionDetails.getStock());
        decoracion.setDescripcion(decoracionDetails.getDescripcion());
        decoracion.setTamaño(decoracionDetails.getTamaño());

        // Actualizar los proveedores
        if (decoracionDetails.getProveedores() != null) {
            for (Proveedor proveedor : decoracion.getProveedores()) {
                proveedor.getProductos().remove(decoracion);
            }
            decoracion.getProveedores().clear();

            for (Proveedor proveedor : decoracionDetails.getProveedores()) {
                decoracion.getProveedores().add(proveedor);
                proveedor.getProductos().add(decoracion);
            }
        } else {
            for (Proveedor proveedor : decoracion.getProveedores()) {
                proveedor.getProductos().remove(decoracion);
            }
            decoracion.getProveedores().clear();
        }

        return decoracionRepository.save(decoracion);
    }

    // Dar de baja a una decoración (marcar como inactiva)
    @Transactional
    public void darDeBajaDecoracion(Long id) {
        Decoracion decoracion = decoracionRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Decoración no encontrada con ID: " + id));
        decoracion.setActivo(false); // Marca la decoración como inactiva
        decoracionRepository.save(decoracion);
    }

    // Buscar decoraciones por nombre, marca y proveedor, y solo traer las activas
    @Transactional(readOnly = true)
    public Page<Decoracion> buscarDecoracionesPaginado(String keyword, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        if (keyword != null && !keyword.isEmpty()) {
            return decoracionRepository.buscarDecoraciones(keyword, pageable);
        } else {
            return getAllDecoracionesActivasPaginadas(pageNumber, pageSize);
        }
    }

}
