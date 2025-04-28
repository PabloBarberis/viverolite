package com.vivero.viveroApp.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDTO {

    private Long clienteId;  
    private List<VentaProductoDTO> productos;  
    private List<MetodoPagoDTO> pagos;  
    private Double descuento = 0.0;  
    private String fecha;
    private String hora;
    private Long id; //ID Venta
}
