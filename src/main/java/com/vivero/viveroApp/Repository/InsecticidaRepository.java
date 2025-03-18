package com.vivero.viveroApp.Repository;

import com.vivero.viveroApp.model.Insecticida;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InsecticidaRepository extends JpaRepository<Insecticida, Long> {

    Page<Insecticida> findByActivoTrue(Pageable pageable);

    Optional<Insecticida> findByIdAndActivoTrue(Long id);

    List<Insecticida> findByActivoTrue();

    @Query("SELECT i FROM Insecticida i LEFT JOIN i.proveedores p WHERE "
            + "i.activo = TRUE AND ("
            + "(:nombre IS NULL OR LOWER(i.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND "
            + "(:marca IS NULL OR LOWER(i.marca) LIKE LOWER(CONCAT('%', :marca, '%'))) AND "
            + "(:proveedor IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :proveedor, '%'))))")
    Page<Insecticida> buscarInsecticida(
            @Param("nombre") String nombre,
            @Param("marca") String marca,
            @Param("proveedor") String proveedor,
            Pageable pageable);
}
