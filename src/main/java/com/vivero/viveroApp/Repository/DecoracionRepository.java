package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.model.Decoracion;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DecoracionRepository extends JpaRepository<Decoracion, Long> {

    Page<Decoracion> findByActivoTrue(Pageable pageable);

    Optional<Decoracion> findByIdAndActivoTrue(Long id);

    @Query("SELECT d FROM Decoracion d LEFT JOIN d.proveedores p WHERE "
            + "d.activo = TRUE AND ("
            + "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(d.marca) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Decoracion> buscarDecoraciones(@Param("keyword") String keyword, Pageable pageable);

    List<Decoracion> findByActivoTrue();
}
