package com.vivero.viveroApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class VentaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    private Double precioOriginal;  // Precio del producto en la venta (puede ser con descuento)
    private Integer cantidad;  // Cantidad del producto que se vende
    private Double porcentajeDescuento;  // Descuento aplicado a este producto
    private Double subtotal;  // Subtotal para este producto (precio * cantidad)
}
