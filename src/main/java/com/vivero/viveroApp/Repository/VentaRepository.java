package com.vivero.viveroApp.repository;

import com.vivero.viveroApp.model.Venta;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByFechaBetween(Date inicioFecha, Date finFecha);



    
    
}
