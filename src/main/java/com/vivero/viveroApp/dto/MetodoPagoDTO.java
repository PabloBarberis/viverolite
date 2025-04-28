package com.vivero.viveroApp.dto;

import com.vivero.viveroApp.model.enums.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoDTO {

    private MetodoPago metodo;  
    private Double monto;  
}
