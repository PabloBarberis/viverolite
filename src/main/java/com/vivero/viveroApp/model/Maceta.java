package com.vivero.viveroApp.model;

import com.vivero.viveroApp.model.enums.ColorMaceta;
import com.vivero.viveroApp.model.enums.MaterialMaceta;
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
@DiscriminatorValue("Maceta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Maceta extends Producto {

    @Enumerated(EnumType.STRING) // Almacena el nombre del enum como un String en la base de datos
    @Column(name = "color_maceta")
    private ColorMaceta color; // Color de la maceta usando el Enum

    @Enumerated(EnumType.STRING) // Almacena el nombre del enum como un String en la base de datos
    @Column(name = "material_maceta")
    private MaterialMaceta material; // Material de la maceta

    @Column(name = "tamaño_maceta")
    private String tamaño; // Tamaño de la maceta

   
    // Constructor personalizado para inicializar atributos
    public Maceta(ColorMaceta color, MaterialMaceta material, String tamaño, Long id, String nombre, String marca, Double precio, Integer stock, String descripcion, boolean activo, List<Proveedor> proveedores) {
        super(id, nombre, marca, precio, stock, descripcion, activo, proveedores);
        this.color = color;
        this.material = material;
        this.tamaño = tamaño;
    }
}


