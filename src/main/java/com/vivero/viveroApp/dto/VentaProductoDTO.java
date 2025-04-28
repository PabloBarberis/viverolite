package com.vivero.viveroApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaProductoDTO {

    private Long productoId;  
    private Integer cantidad;  
    private Double descuentoProducto;
    private Double precioUnitario;  
}
