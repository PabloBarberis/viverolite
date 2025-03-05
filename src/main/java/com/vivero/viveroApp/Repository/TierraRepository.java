package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.model.Tierra;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TierraRepository extends JpaRepository<Tierra, Long> {

    // Buscar todas las tierras activas con paginación
    Page<Tierra> findByActivoTrue(Pageable pageable);

    // Buscar una tierra por ID si está activa
    Optional<Tierra> findByIdAndActivoTrue(Long id);

    // Búsqueda personalizada para filtrar por nombre, marca, tipo y proveedor, y solo traer tierras activas con paginación
    @Query("SELECT t FROM Tierra t LEFT JOIN t.proveedores p WHERE "
            + "t.activo = TRUE AND ("
            + "(:nombre IS NULL OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND "
            + "(:marca IS NULL OR LOWER(t.marca) LIKE LOWER(CONCAT('%', :marca, '%'))) AND "
            + "(:tipo IS NULL OR LOWER(t.tipo) LIKE LOWER(CONCAT('%', :tipo, '%'))) AND "
            + "(:proveedor IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :proveedor, '%'))))")
    Page<Tierra> buscarTierra(@Param("nombre") String nombre,
            @Param("marca") String marca,
            @Param("tipo") String tipo,
            @Param("proveedor") String proveedor,
            Pageable pageable);

    List<Tierra> findByActivoTrue();
}
