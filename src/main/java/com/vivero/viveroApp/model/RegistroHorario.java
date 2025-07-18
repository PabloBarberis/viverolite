package com.vivero.viveroApp.model;

import com.vivero.viveroApp.converter.LocalTimeAttributeConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

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

    @Column(name = "entradatm")
    @Convert(converter = LocalTimeAttributeConverter.class)
    private LocalTime entradaTM;

    @Column(name = "salidatm")
    @Convert(converter = LocalTimeAttributeConverter.class)
    private LocalTime salidaTM;

    @Column(name = "entradatt")
    @Convert(converter = LocalTimeAttributeConverter.class)
    private LocalTime entradaTT;

    @Column(name = "salidatt")
    @Convert(converter = LocalTimeAttributeConverter.class)
    private LocalTime salidaTT;

    private double totalHoras;
    private boolean feriado;
    private double precioHora;

}
