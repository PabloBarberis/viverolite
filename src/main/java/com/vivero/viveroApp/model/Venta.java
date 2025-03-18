package com.vivero.viveroApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vivero.viveroApp.model.enums.Descuento;
import com.vivero.viveroApp.model.enums.MetodoPago;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Data
@NoArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true) // Relación con Cliente
    @JsonIgnore
    private Cliente cliente;

    @JsonIgnore
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true) // Relación con VentaProducto
    private List<VentaProducto> productos = new ArrayList<>();

    private Double total; // Atributo para almacenar el total de la venta

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 🛠 Formato de fecha
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING) // Almacena el método de pago como un String en la base de datos
    @Column(name = "tipo_pago")
    private MetodoPago metodoPago; // Tipo de método de pago (efectivo, débito, crédito, Mercadopago)

    @Enumerated(EnumType.STRING) // Almacena el descuento como un String en la base de datos
    @Column(name = "descuento")
    private Descuento descuento; // Enum para el porcentaje de descuento

    // Constructor
    public Venta(Cliente cliente, MetodoPago metodoPago, Descuento descuento) {
        this.cliente = cliente;
        this.fecha = LocalDateTime.now(); // Fecha y hora actual
        this.metodoPago = metodoPago;
        this.descuento = descuento; // Asignamos el descuento al crear la venta
    }

    // Método para agregar producto con cantidad
    public void agregarProducto(Producto producto, int cantidad) {
        if (producto != null && cantidad > 0) {
            VentaProducto ventaProducto = new VentaProducto();
            ventaProducto.setProducto(producto);
            ventaProducto.setCantidad(cantidad);
            ventaProducto.setVenta(this); // Asociamos el producto a la venta
            this.productos.add(ventaProducto);
            calcularTotal(); // Calcula el total después de agregar el producto
        }
    }

    // Método para calcular el total de la venta
    public void calcularTotal() {
        System.out.println("El total que recibe calcularTotal() es de " + this.total);

        // Calcular el total inicial basado en los productos y sus cantidades
        this.total = productos.stream()
                .mapToDouble(ventaProducto -> ventaProducto.getProducto().getPrecio() * ventaProducto.getCantidad())
                .sum();

        // Aplicar el costo adicional si el método de pago es crédito
        if (metodoPago == MetodoPago.CREDITO) {
            this.total *= 1.15; // Aumenta el precio en un 15%
        }

        // Aplicar el descuento si existe
        if (descuento != null) {
            this.total -= (this.total * (descuento.getPorcentaje() / 100.0)); // Aplica el descuento
        }

    }

}
