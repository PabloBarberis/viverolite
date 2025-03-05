package com.vivero.viveroApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClienteDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "El CUIL es obligatorio")
    @Size(min = 11, max = 11, message = "El CUIL debe tener 11 caracteres")
    private String cuil;
}
