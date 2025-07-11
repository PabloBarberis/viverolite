package com.vivero.viveroApp.Repository;

import com.vivero.viveroApp.dto.ProductoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.vivero.viveroApp.model.Producto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE p.tipo = :tipo")
    List<Producto> findByTipo(@Param("tipo") String tipo);

    // Buscar plantas según su tipo (Interior/Exterior) y marca
    @Query("SELECT p FROM Producto p WHERE p.tipo = 'Planta' AND p.marca = :marca AND p.descripcion LIKE %:interiorExterior%")
    List<Producto> findPlantasPorTipo(@Param("interiorExterior") String interiorExterior, @Param("marca") String marca);

    // Buscar productos por tipo y marca (excepto plantas)
    @Query("SELECT p FROM Producto p WHERE p.tipo = :tipo AND p.marca = :marca")
    List<Producto> findByTipoAndMarca(@Param("tipo") String tipo, @Param("marca") String marca);

    List<Producto> findByActivoTrue();

    @Query(value = "SELECT DISTINCT marca FROM producto WHERE tipo = :tipo ORDER BY marca", nativeQuery = true)
    List<String> findDistinctMarcasByDtype(@Param("tipo") String tipo);

    public List<Producto> findByNombreContainingIgnoreCase(String q);

    @Query("SELECT new com.vivero.viveroApp.dto.ProductoDTO(p.id, p.nombre, p.precio, p.stock) FROM Producto p WHERE UPPER(p.nombre) LIKE UPPER(CONCAT('%', :q, '%'))")
    public List<ProductoDTO> buscarProductoPorNombre(String q);

    @Modifying
    @Transactional
    @Query("UPDATE Producto p SET p.precio = :precio WHERE p.id = :id")
    void actualizarPrecio(@Param("id") Long id, @Param("precio") Double precio);

    @Modifying
    @Transactional
    @Query("UPDATE Producto p SET p.stock = :stock WHERE p.id = :id")
    void actualizarStock(@Param("id") Long id, @Param("stock") Integer stock);

    @Query(value = "SELECT * FROM producto WHERE activo = TRUE "
            + "AND (:nombre IS NULL OR LOWER(nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) "
            + "AND (:marca IS NULL OR LOWER(marca) LIKE LOWER(CONCAT('%', :marca, '%'))) "
            + "AND (:tipo IS NULL OR tipo = :tipo)",
            countQuery = "SELECT COUNT(*) FROM producto WHERE activo = TRUE "
            + "AND (:nombre IS NULL OR LOWER(nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) "
            + "AND (:marca IS NULL OR LOWER(marca) LIKE LOWER(CONCAT('%', :marca, '%'))) "
            + "AND (:tipo IS NULL OR tipo = :tipo)",
            nativeQuery = true)
    Page<Producto> buscarProducto(
            @Param("nombre") String nombre,
            @Param("marca") String marca,
            @Param("tipo") String tipo,
            Pageable pageable);

    @Query(value = "SELECT * FROM producto WHERE activo = TRUE "
            + "AND (:nombre IS NULL OR LOWER(nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) "
            + "AND (:marca IS NULL OR LOWER(marca) LIKE LOWER(CONCAT('%', :marca, '%'))) "
            + "AND (:tipo IS NULL OR tipo = :tipo)", nativeQuery = true)
    List<Producto> findByFiltros(@Param("tipo") String tipo,
            @Param("nombre") String nombre,
            @Param("marca") String marca);
}
