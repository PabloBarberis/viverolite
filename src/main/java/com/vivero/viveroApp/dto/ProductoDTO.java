
package com.vivero.viveroApp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductoDTO {
    private Long id;
    private String nombre;
    private Double precio; //precio de venta al publico
    private Integer stock;
    private Integer cantidad;
    private Double precioCompra; //precio al que se compra el prod

    public ProductoDTO(Long id, String nombre, Double precio, Integer stock) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }   
    
    
}

