package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.Maceta;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.repository.MacetaRepository;
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
public class MacetaService {

    private final MacetaRepository macetaRepository;

    @Transactional(readOnly = true)
    public List<Maceta> getAllMacetas() {
        return macetaRepository.findByActivoTrue();
    }

    // Obtener todas las macetas activas con paginación
    @Transactional(readOnly = true)
    public Page<Maceta> getAllMacetasActivasPaginadas(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return macetaRepository.findByActivoTrue(pageable);
    }

    // Obtener todas las macetas con paginación (activas e inactivas)
    @Transactional(readOnly = true)
    public Page<Maceta> getAllMacetasPaginadas(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return macetaRepository.findAll(pageable);
    }

    // Obtener una maceta por ID si está activa
    @Transactional(readOnly = true)
    public Optional<Maceta> getMacetaById(Long id) {
        return macetaRepository.findByIdAndActivoTrue(id);
    }

    // Obtener una maceta por ID (incluso si está inactiva)
    @Transactional(readOnly = true)
    public Optional<Maceta> getMacetaByIdIncluyendoInactivas(Long id) {
        return macetaRepository.findById(id);
    }

    // Crear una nueva maceta
    @Transactional
    public Maceta createMaceta(Maceta maceta) {
        maceta.setActivo(true); // Asegura que la nueva maceta esté activa por defecto

        // Manejar la relación con proveedores
        for (Proveedor proveedor : maceta.getProveedores()) {
            proveedor.getProductos().add(maceta); // Agregar la maceta a la lista de productos del proveedor
        }
        return macetaRepository.save(maceta);
    }

    // Actualizar una maceta existente
    @Transactional
    public Maceta updateMaceta(Long id, Maceta macetaDetails) {
        Maceta maceta = macetaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Maceta no encontrada con ID: " + id));

        maceta.setNombre(macetaDetails.getNombre());
        maceta.setPrecio(macetaDetails.getPrecio());
        maceta.setStock(macetaDetails.getStock());
        maceta.setDescripcion(macetaDetails.getDescripcion());
        maceta.setColor(macetaDetails.getColor());
        maceta.setMaterial(macetaDetails.getMaterial());
        maceta.setTamaño(macetaDetails.getTamaño());

        // Manejar la relación con proveedores
        if (macetaDetails.getProveedores() != null) {
            // Limpiar la relación actual de ambos lados (Maceta y Proveedor)
            for (Proveedor proveedor : maceta.getProveedores()) {
                proveedor.getProductos().remove(maceta); // Remover la maceta de la lista de productos del proveedor
            }
            maceta.getProveedores().clear(); // Limpiar la lista de proveedores actual

            // Agregar nuevos proveedores y actualizar ambos lados de la relación
            for (Proveedor proveedor : macetaDetails.getProveedores()) {
                maceta.getProveedores().add(proveedor);
                proveedor.getProductos().add(maceta); // Agregar la maceta a la lista de productos del proveedor
            }
        } else {
            // Si no hay proveedores en macetaDetails, limpiar la lista de proveedores
            for (Proveedor proveedor : maceta.getProveedores()) {
                proveedor.getProductos().remove(maceta);
            }
            maceta.getProveedores().clear();
        }
        return macetaRepository.save(maceta);
    }

    // Dar de baja una maceta (marcar como inactiva)
    @Transactional
    public void darDeBajaMaceta(Long id) {
        Maceta maceta = macetaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Maceta no encontrada con ID: " + id));
        maceta.setActivo(false); // Marca la maceta como inactiva
        macetaRepository.save(maceta);
    }

    // Buscar macetas por nombre, color, marca, modelo y material, y solo traer las activas con paginación
    @Transactional(readOnly = true)
    public Page<Maceta> buscarMacetaPaginado(String nombre, String color, String marca, String material, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return macetaRepository.buscarMaceta(
                nombre != null && !nombre.isEmpty() ? nombre : null,
                color != null && !color.isEmpty() ? color : null,
                material != null && !material.isEmpty() ? material : null,
                pageable
        );
    }

}
