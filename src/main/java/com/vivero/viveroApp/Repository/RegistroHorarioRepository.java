package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.model.RegistroHorario;
import com.vivero.viveroApp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegistroHorarioRepository extends JpaRepository<RegistroHorario, Long> {
    List<RegistroHorario> findByUsuarioAndFechaBetween(Usuario usuario, LocalDate startDate, LocalDate endDate);
}
