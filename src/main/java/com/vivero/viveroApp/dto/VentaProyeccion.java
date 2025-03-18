package com.vivero.viveroApp.dto;

import com.vivero.viveroApp.model.enums.MetodoPago;
import java.time.LocalDateTime;

public interface VentaProyeccion {
    LocalDateTime getFecha(); // Fecha de la venta
    MetodoPago getMetodoPago(); // Método de pago
    Double getTotal(); // Total de la venta
}
