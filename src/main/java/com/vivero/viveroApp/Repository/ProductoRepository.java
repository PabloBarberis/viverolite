package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.dto.ProductoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.vivero.viveroApp.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE TYPE(p) = :tipoProducto")
    List<Producto> findByTipo(@Param("tipoProducto") Class<? extends Producto> tipoProducto);

    // Buscar plantas según su tipo (Interior/Exterior) y marca
    @Query("SELECT p FROM Producto p WHERE TYPE(p) = Planta AND p.marca = :marca AND p.descripcion LIKE %:interiorExterior%")
    List<Producto> findPlantasPorTipo(@Param("interiorExterior") String interiorExterior, @Param("marca") String marca);

    // Buscar productos por tipo y marca (excepto plantas)
    @Query("SELECT p FROM Producto p WHERE TYPE(p) = :tipo AND p.marca = :marca")
    List<Producto> findByTipoAndMarca(@Param("tipo") Class<? extends Producto> tipo, @Param("marca") String marca);
    
    List<Producto> findByActivoTrue();

    @Query(value = "SELECT DISTINCT marca FROM producto WHERE dtype = :tipo ORDER BY marca", nativeQuery = true)
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

        
}
