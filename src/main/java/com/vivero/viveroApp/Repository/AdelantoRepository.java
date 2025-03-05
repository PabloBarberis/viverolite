package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.model.Adelanto;
import com.vivero.viveroApp.model.Usuario;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdelantoRepository extends JpaRepository<Adelanto, Long> {

    List<Adelanto> findByUsuarioAndFechaBetween(Usuario usuario, LocalDate startDate, LocalDate endDate);
}
