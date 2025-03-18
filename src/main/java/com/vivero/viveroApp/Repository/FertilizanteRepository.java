package com.vivero.viveroApp.Repository;

import com.vivero.viveroApp.model.Fertilizante;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FertilizanteRepository extends JpaRepository<Fertilizante, Long> {

    Page<Fertilizante> findByActivoTrue(Pageable pageable);

    Optional<Fertilizante> findByIdAndActivoTrue(Long id);
    
    List<Fertilizante> findByActivoTrue();
    

@Query("SELECT f FROM Fertilizante f LEFT JOIN f.proveedores p WHERE "
        + "f.activo = TRUE AND ("
        + "(:nombre IS NULL OR LOWER(f.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND "
        + "(:marca IS NULL OR LOWER(f.marca) LIKE LOWER(CONCAT('%', :marca, '%'))) AND "
        + "(:proveedor IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :proveedor, '%'))))")
Page<Fertilizante> buscarFertilizante(
        @Param("nombre") String nombre,
        @Param("marca") String marca,
        @Param("proveedor") String proveedor,
        Pageable pageable);




}
