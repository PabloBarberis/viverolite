package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.model.Grow;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrowRepository extends JpaRepository<Grow, Long> {

    // Buscar todas las productos Grow activos con paginación
    Page<Grow> findByActivoTrue(Pageable pageable);

    // Buscar un producto Grow por ID si está activo
    Optional<Grow> findByIdAndActivoTrue(Long id);

    // Búsqueda personalizada con paginación
    @Query("SELECT g FROM Grow g LEFT JOIN g.proveedores p WHERE " +
           "g.activo = TRUE AND (" +
           "LOWER(g.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.marca) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "(p IS NOT NULL AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<Grow> buscarGrow(@Param("keyword") String keyword, Pageable pageable);

    List<Grow> findByActivoTrue();
}
