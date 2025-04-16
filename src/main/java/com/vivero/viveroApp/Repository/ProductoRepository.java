package com.vivero.viveroApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.vivero.viveroApp.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

        
}
