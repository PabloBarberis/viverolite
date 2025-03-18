package com.vivero.viveroApp.Repository;

import com.vivero.viveroApp.model.Semilla;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SemillaRepository extends JpaRepository<Semilla, Long> {

    Page<Semilla> findByActivoTrue(Pageable pageable);

    Optional<Semilla> findByIdAndActivoTrue(Long id);
    
    List<Semilla> findByActivoTrue();
    
    @Query("SELECT s FROM Semilla s LEFT JOIN s.proveedores p WHERE "
        + "s.activo = TRUE AND ("
        + "(:nombre IS NULL OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND "
        + "(:marca IS NULL OR LOWER(s.marca) LIKE LOWER(CONCAT('%', :marca, '%'))) AND "
        + "(:proveedor IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :proveedor, '%'))))")
    Page<Semilla> buscarSemilla(
        @Param("nombre") String nombre,
        @Param("marca") String marca,
        @Param("proveedor") String proveedor,
        Pageable pageable);
}
