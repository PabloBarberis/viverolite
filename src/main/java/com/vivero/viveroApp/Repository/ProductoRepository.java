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

    List<Producto> findByActivoTrue();
        
}
