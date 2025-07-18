package com.vivero.viveroApp.Repository;

import com.vivero.viveroApp.model.RegistroHorario;
import com.vivero.viveroApp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RegistroHorarioRepository extends JpaRepository<RegistroHorario, Long> {

    List<RegistroHorario> findByUsuarioAndFechaBetweenOrderByFechaAsc(Usuario usuario, LocalDate start, LocalDate end);

    @Query(value = "SELECT * FROM registro_horario "
            + "WHERE strftime('%Y', datetime(fecha / 1000, 'unixepoch')) = :anio "
            + "AND strftime('%m', datetime(fecha / 1000, 'unixepoch')) = :mes",
            nativeQuery = true)
    List<RegistroHorario> obtenerIngresosRegistrosHorariosPorMesYAnio(@Param("mes") String mes, @Param("anio") String anio);

   @Query(value = "SELECT * FROM registro_horario "
            + "WHERE strftime('%Y', datetime(fecha / 1000, 'unixepoch')) = :anio "
            + "AND strftime('%m', datetime(fecha / 1000, 'unixepoch')) = :mes "
            + "AND usuario_id = :id",
            nativeQuery = true)
List<RegistroHorario> obtenerIngresosRegistrosHorariosPorMesAnioYUsuario(@Param("mes") String mes,
                                                                          @Param("anio") String anio,
                                                                          @Param("id") Long id);


    boolean existsByUsuarioIdAndFecha(Long usuarioId, LocalDate fecha);
}
