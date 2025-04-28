package com.vivero.viveroApp.dto;

import com.vivero.viveroApp.model.enums.MetodoPago;
import java.time.LocalDateTime;

public interface VentaProyeccion {
    LocalDateTime getFecha(); 
    MetodoPago getMetodoPago(); 
    Double getTotal(); 
}
