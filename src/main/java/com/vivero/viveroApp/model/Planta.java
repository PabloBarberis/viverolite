

package com.vivero.viveroApp.model;

import com.vivero.viveroApp.model.enums.TipoPlanta;
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
@DiscriminatorValue("Planta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Planta extends Producto {

    @Enumerated(EnumType.STRING) // Almacena el nombre del enum como un String en la base de datos
    @Column(name = "tipo_planta")
    private TipoPlanta tipo; // Tipo de planta usando el Enum

    public Planta(TipoPlanta tipo, Long id, String nombre, String marca, Double precio, Integer stock, String descripcion, boolean activo, List<Proveedor> proveedores) {
        super(id, nombre, marca, precio, stock, descripcion, activo, proveedores);
        this.tipo = tipo;
    }
  
    
}
