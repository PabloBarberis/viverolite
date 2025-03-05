package com.vivero.viveroApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@DiscriminatorValue("Proveedor")
@Getter
@Setter
@NoArgsConstructor
public class Proveedor extends Persona {

    @ManyToMany
    @JoinTable(
            name = "proveedor_producto", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "proveedor_id"),
            inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    @JsonIgnore
    private List<Producto> productos = new ArrayList<>(); // Relación Many-to-Many con productos

    public Proveedor(List<Producto> productos, Long id, String nombre, String direccion, String telefono, String email, String cuil, boolean activo) {
        super(id, nombre, direccion, telefono, email, cuil, activo);
        this.productos = productos;
    }

}
