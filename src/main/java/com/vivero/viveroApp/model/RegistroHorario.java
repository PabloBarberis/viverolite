package com.vivero.viveroApp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDate fecha;
    private String diaSemana;
    private String entradaTM;
    private String salidaTM;
    private String entradaTT;
    private String salidaTT;
    private double totalHoras;
    private boolean feriado;
    private double precioHora; // Nuevo campo para el precio por hora

    // Métodos adicionales si es necesario
}

