package com.vivero.viveroApp.model;

import com.vivero.viveroApp.model.enums.TipoGrow;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("Grow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Grow extends Producto {

    @Enumerated(EnumType.STRING) // Almacena el nombre del enum como un String en la base de datos
    @Column(name = "tipo_grow")
    private TipoGrow tipo; // Tipo de producto usando el Enum
    

    // Constructor personalizado

    public Grow(TipoGrow tipo, Long id, String nombre, String marca, Double precio, Integer stock, String descripcion, boolean activo, List<Proveedor> proveedores) {
        super(id, nombre, marca, precio, stock, descripcion, activo, proveedores);
        this.tipo = tipo;
    }
}