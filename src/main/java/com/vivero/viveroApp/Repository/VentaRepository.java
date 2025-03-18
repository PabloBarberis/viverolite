package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.dto.VentaProyeccion;
import com.vivero.viveroApp.model.Venta;
import com.vivero.viveroApp.model.enums.MetodoPago;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<VentaProyeccion> findByMetodoPago(MetodoPago metodoPago); // Método para obtener ventas por tipo de pago

    @Query(value = "SELECT * FROM venta "
            + "WHERE strftime('%Y', datetime(fecha / 1000, 'unixepoch')) = :anio "
            + "AND strftime('%m', datetime(fecha / 1000, 'unixepoch')) = :mes",
            nativeQuery = true)
    List<Venta> obtenerVentasPorMesYAnio(@Param("mes") String mes, @Param("anio") String anio);

}
