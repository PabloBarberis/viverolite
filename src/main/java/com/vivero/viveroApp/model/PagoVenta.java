package com.vivero.viveroApp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.vivero.viveroApp.model.enums.MetodoPago;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class PagoVenta {

    @Id   
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodo;
    
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    private Double monto;
}
