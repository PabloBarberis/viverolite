package com.vivero.viveroApp.Repository;

import com.vivero.viveroApp.model.IngresoEgreso;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IngresoEgresoRepository extends JpaRepository<IngresoEgreso, Long> {

    public List<IngresoEgreso> findByFechaBetween(LocalDateTime inicioMes, LocalDateTime finMes);

    @Query(value = "SELECT * FROM ingreso_egreso "
            + "WHERE strftime('%Y', datetime(fecha / 1000, 'unixepoch')) = :anio "
            + "AND strftime('%m', datetime(fecha / 1000, 'unixepoch')) = :mes",
            nativeQuery = true)
    List<IngresoEgreso> obtenerIngresosEgresosPorMesYAnio(@Param("mes") String mes, @Param("anio") String anio);

}
