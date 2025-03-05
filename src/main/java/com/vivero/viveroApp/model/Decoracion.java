package com.vivero.viveroApp.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("Decoracion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Decoracion extends Producto {

    private String tamaño; // Tamaño de la decoración

    public Decoracion(String tamaño, Long id, String nombre, String marca, Double precio, Integer stock, String descripcion, boolean activo, List<Proveedor> proveedores) {
        super(id, nombre, marca, precio, stock, descripcion, activo, proveedores);
        this.tamaño = tamaño;
    }

}
