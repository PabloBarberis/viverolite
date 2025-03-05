package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    
    // Buscar todos los proveedores activos
    List<Proveedor> findByActivoTrue();

    // Buscar un proveedor por ID si está activo
    Optional<Proveedor> findByIdAndActivoTrue(Long id);
}
