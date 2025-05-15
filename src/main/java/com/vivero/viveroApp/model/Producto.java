
package com.vivero.viveroApp.model;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Data
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String marca;
    private Double precio;
    private Integer stock;
    private String descripcion;
    private boolean activo;
    private String tipo;
        
@ManyToMany
@JoinTable(
    name = "proveedor_producto", // ✅ nombre correcto de la tabla intermedia
    joinColumns = @JoinColumn(name = "producto_id"),
    inverseJoinColumns = @JoinColumn(name = "proveedor_id")

)
    private List<Proveedor> proveedores = new ArrayList<>(); // Relación Many-to-Many con proveedores

        public Producto(Long id, String nombre, String marca, Double precio, Integer stock, String descripcion, boolean activo, String tipo, List<Proveedor> proveedores) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.precio = precio;
        this.stock = stock;
        this.descripcion = descripcion;
        this.activo = activo;
        this.tipo = tipo;
        this.proveedores = (proveedores != null) ? proveedores : new ArrayList<>(); // Inicializa con lista vacía si proveedores es null
    }
    
    
}
