package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.model.Planta;
import com.vivero.viveroApp.model.enums.TipoPlanta;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantaRepository extends JpaRepository<Planta, Long> {

    Page<Planta> findByActivoTrue(Pageable pageable);

    List<Planta> findByActivoTrue();

    Page<Planta> findByNombreContainingIgnoreCaseAndTipoAndActivoTrue(String nombre, TipoPlanta tipo, Pageable pageable);

    Page<Planta> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre, Pageable pageable);

    Page<Planta> findByTipoAndActivoTrue(TipoPlanta tipo, Pageable pageable);
}
