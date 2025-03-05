package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    // Obtener todos los proveedores activos
    @Transactional(readOnly = true)
    public List<Proveedor> getAllProveedoresActivos() {
        return proveedorRepository.findByActivoTrue();
    }

    // Obtener un proveedor por ID si está activo
    @Transactional(readOnly = true)
    public Optional<Proveedor> getProveedorById(Long id) {
        return proveedorRepository.findByIdAndActivoTrue(id);
    }

    // Obtener una lista de proveedores por sus IDs (solo proveedores activos)
    @Transactional(readOnly = true)
    public List<Proveedor> getProveedoresByIds(List<Long> ids) {
        return proveedorRepository.findAllById(ids).stream()
                .filter(Proveedor::isActivo)
                .collect(Collectors.toList());
    }

    // Crear un nuevo proveedor
    @Transactional
    public Proveedor createProveedor(Proveedor proveedor) {
        proveedor.setActivo(true); // Asegura que el nuevo proveedor esté activo por defecto
        return proveedorRepository.save(proveedor);
    }

    // Actualizar un proveedor existente
    @Transactional
    public Proveedor updateProveedor(Long id, Proveedor proveedorDetails) {
        Optional<Proveedor> optionalProveedor = proveedorRepository.findByIdAndActivoTrue(id);

        if (optionalProveedor.isPresent()) {
            Proveedor proveedor = optionalProveedor.get();

            // Actualizar solo los atributos personales
            proveedor.setNombre(proveedorDetails.getNombre());
            proveedor.setDireccion(proveedorDetails.getDireccion());
            proveedor.setTelefono(proveedorDetails.getTelefono());
            proveedor.setEmail(proveedorDetails.getEmail());
            proveedor.setCuil(proveedorDetails.getCuil());

            // Guardar sin tocar la lista de productos asociados
            return proveedorRepository.save(proveedor);
        } else {
            throw new RuntimeException("Proveedor no encontrado con ID: " + id);
        }
    }

    // Dar de baja un proveedor (marcar como inactivo)
            @Transactional
    public void darDeBajaProveedor(Long id) {
        Optional<Proveedor> optionalProveedor = proveedorRepository.findByIdAndActivoTrue(id);

        if (optionalProveedor.isPresent()) {
            Proveedor proveedor = optionalProveedor.get();
            proveedor.setActivo(false); // Marca el proveedor como inactivo
            proveedorRepository.save(proveedor);
        } else {
            throw new RuntimeException("Proveedor no encontrado con ID: " + id);
        }
    }
}
