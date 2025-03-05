package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.Tierra;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.repository.TierraRepository;
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
public class TierraService {

    private final TierraRepository tierraRepository;

    // Obtener todas las tierras activas con paginación
    @Transactional(readOnly = true)
    public Page<Tierra> getAllTierrasActivasPaginadas(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return tierraRepository.findByActivoTrue(pageable);
    }

    // Obtener todas las tierras con paginación (activas e inactivas)
    @Transactional(readOnly = true)
    public Page<Tierra> getAllTierrasPaginadas(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return tierraRepository.findAll(pageable);
    }

    // Obtener una tierra por ID si está activa
    @Transactional(readOnly = true)
    public Optional<Tierra> getTierraById(Long id) {
        return tierraRepository.findByIdAndActivoTrue(id);
    }

    // Obtener una tierra por ID (incluso si está inactiva)
    @Transactional(readOnly = true)
    public Optional<Tierra> getTierraByIdIncluyendoInactivas(Long id) {
        return tierraRepository.findById(id);
    }

    // Crear una nueva tierra
    @Transactional
    public Tierra createTierra(Tierra tierra) {
        tierra.setActivo(true); // Asegura que la nueva tierra esté activa por defecto
        for (Proveedor proveedor : tierra.getProveedores()) {
            proveedor.getProductos().add(tierra); // Agregar la tierra a la lista de productos del proveedor
        }
        return tierraRepository.save(tierra);
    }

    // Actualizar una tierra existente
    @Transactional
    public Tierra updateTierra(Long id, Tierra tierraDetails) {
        Tierra tierra = tierraRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Tierra no encontrada con ID: " + id));

        tierra.setNombre(tierraDetails.getNombre());
        tierra.setMarca(tierraDetails.getMarca());
        tierra.setPrecio(tierraDetails.getPrecio());
        tierra.setStock(tierraDetails.getStock());
        tierra.setDescripcion(tierraDetails.getDescripcion());
        tierra.setTipo(tierraDetails.getTipo());
        tierra.setVolumen(tierraDetails.getVolumen());
        tierra.setComposicion(tierraDetails.getComposicion());

        // Actualizar los proveedores
        if (tierraDetails.getProveedores() != null) {
            for (Proveedor proveedor : tierra.getProveedores()) {
                proveedor.getProductos().remove(tierra);
            }
            tierra.getProveedores().clear();

            for (Proveedor proveedor : tierraDetails.getProveedores()) {
                tierra.getProveedores().add(proveedor);
                proveedor.getProductos().add(tierra);
            }
        } else {
            for (Proveedor proveedor : tierra.getProveedores()) {
                proveedor.getProductos().remove(tierra);
            }
            tierra.getProveedores().clear();
        }

        return tierraRepository.save(tierra);
    }

    // Dar de baja una tierra (marcar como inactiva)
    @Transactional
    public void darDeBajaTierra(Long id) {
        Tierra tierra = tierraRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Tierra no encontrada con ID: " + id));
        tierra.setActivo(false); // Marca la tierra como inactiva
        tierraRepository.save(tierra);
    }

    // Buscar tierras por nombre, marca, tipo y proveedor, y solo traer las activas con paginación
    @Transactional(readOnly = true)
    public Page<Tierra> buscarTierraPaginado(String nombre, String marca, String tipo, String proveedor, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return tierraRepository.buscarTierra(
                nombre != null && !nombre.isEmpty() ? nombre : null,
                marca != null && !marca.isEmpty() ? marca : null,
                tipo != null && !tipo.isEmpty() ? tipo : null,
                proveedor != null && !proveedor.isEmpty() ? proveedor : null,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<Tierra> getAllTierras() {
        return tierraRepository.findByActivoTrue();
    }

}
