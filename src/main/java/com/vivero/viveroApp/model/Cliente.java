
package com.vivero.viveroApp.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("Cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente extends Persona{
    
    @OneToMany
    @JoinTable(
            name = "cliente_ventas", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "venta_id")
    )
    private List<Venta> listaVentas;

    public Cliente(List<Venta> listaVentas, Long id, String nombre, String direccion, String telefono, String email, String cuil, boolean activo) {
        super(id, nombre, direccion, telefono, email, cuil, activo);
        this.listaVentas = listaVentas;
    }
    
    
    
}
