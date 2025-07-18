package com.vivero.viveroApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RegistroHorarioDTO {
    private Long id;
    private Long usuarioId;
    private LocalDate fecha;
    private LocalTime entradaTM;
    private LocalTime salidaTM;
    private LocalTime entradaTT;
    private LocalTime salidaTT;
    private boolean feriado;
    private double precioHora;
}
