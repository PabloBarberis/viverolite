package com.vivero.viveroApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdelantoListDTO {
    private List<AdelantoDTO> adelantos;
    private double total;

    public AdelantoListDTO(List<AdelantoDTO> adelantos) {
        this.adelantos = adelantos;
        this.total = adelantos.stream()
                .mapToDouble(AdelantoDTO::getMonto)
                .sum();
    }

    // Getters
}