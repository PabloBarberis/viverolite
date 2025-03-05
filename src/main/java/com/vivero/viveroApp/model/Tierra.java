package com.vivero.viveroApp.model;

import com.vivero.viveroApp.model.enums.TipoTierra;
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
@DiscriminatorValue("Tierra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tierra extends Producto {

    @Enumerated(EnumType.STRING) // Almacena el nombre del enum como un String en la base de datos
    @Column(name = "tipo_tierra")
    private TipoTierra tipo; // Tipo de tierra usando el Enum
    private Double volumen; // Volumen en litros o kg
    private String composicion; // Composición de la tierra

    // Constructor personalizado

    public Tierra(TipoTierra tipo, Double volumen, String composicion, Long id, String nombre, String marca, Double precio, Integer stock, String descripcion, boolean activo, List<Proveedor> proveedores) {
        super(id, nombre, marca, precio, stock, descripcion, activo, proveedores);
        this.tipo = tipo;
        this.volumen = volumen;
        this.composicion = composicion;
    }

    

}
