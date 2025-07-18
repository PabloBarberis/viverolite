package com.vivero.viveroApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdelantoDTO {
    private Long id;
    private LocalDate fecha;
    private String descripcion;
    private double monto;

}