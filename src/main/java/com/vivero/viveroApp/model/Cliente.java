package com.vivero.viveroApp.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("Cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente extends Persona {

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Venta> listaVentas = new ArrayList<>();

    public Cliente(List<Venta> listaVentas, Long id, String nombre, String direccion, String telefono, String email, String cuil, boolean activo) {
        super(id, nombre, direccion, telefono, email, cuil, activo);
        this.listaVentas = listaVentas;
    }

}
