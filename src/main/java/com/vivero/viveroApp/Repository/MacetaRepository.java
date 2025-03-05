package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.model.Maceta;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MacetaRepository extends JpaRepository<Maceta, Long> {

    // Buscar todas las macetas activas con paginación
    Page<Maceta> findByActivoTrue(Pageable pageable);

    // Buscar una maceta por ID si está activa
    Optional<Maceta> findByIdAndActivoTrue(Long id);

    // Búsqueda personalizada para filtrar por nombre, color, marca, modelo y material, y solo traer macetas activas con paginación
    @Query("SELECT m FROM Maceta m LEFT JOIN m.proveedores p WHERE "
            + "m.activo = TRUE AND ("
            + "(:nombre IS NULL OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND "
            + "(:color IS NULL OR LOWER(m.color) LIKE LOWER(CONCAT('%', :color, '%'))) AND "
            + "(:material IS NULL OR LOWER(m.material) LIKE LOWER(CONCAT('%', :material, '%'))))")
    Page<Maceta> buscarMaceta(@Param("nombre") String nombre,
            @Param("color") String color,
            @Param("material") String material,
            Pageable pageable);

    List<Maceta> findByActivoTrue();
}
