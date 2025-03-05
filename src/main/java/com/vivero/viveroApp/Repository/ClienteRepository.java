package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Buscar todos los clientes activos
    List<Cliente> findByActivoTrue();

    // Buscar cliente por ID solo si está activo
    Optional<Cliente> findByIdAndActivoTrue(Long id);
}
