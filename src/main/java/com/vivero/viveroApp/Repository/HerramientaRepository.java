/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.vivero.viveroApp.Repository;

import com.vivero.viveroApp.model.Herramienta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author pablo
 */
@Repository
public interface HerramientaRepository extends JpaRepository<Herramienta, Long> {

    public Page<Herramienta> findByActivoTrue(Pageable pageable);

    public Optional<Herramienta> findByIdAndActivoTrue(Long id);

    public List<Herramienta> findByActivoTrue();

    @Query("SELECT h FROM Herramienta h LEFT JOIN h.proveedores p WHERE "
            + "h.activo = TRUE AND ("
            + "(:nombre IS NULL OR LOWER(h.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND "
            + "(:marca IS NULL OR LOWER(h.marca) LIKE LOWER(CONCAT('%', :marca, '%'))) AND "
            + "(:proveedor IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :proveedor, '%'))))")
    Page<Herramienta> buscarHerramienta(
            @Param("nombre") String nombre,
            @Param("marca") String marca,
            @Param("proveedor") String proveedor,
            Pageable pageable);

}
