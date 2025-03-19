package com.vivero.viveroApp.model;

import com.vivero.viveroApp.model.enums.MetodoPago;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class IngresoEgreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    private Boolean ingreso; // true = ingreso, false = egreso (gasto)

    private double monto;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false) // Clave foránea
    private Usuario usuario;
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
private boolean adelanto;


}
