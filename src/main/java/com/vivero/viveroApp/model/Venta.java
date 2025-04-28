package com.vivero.viveroApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.vivero.viveroApp.model.enums.MetodoPago;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Data
@NoArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true)
    @JsonIgnore
    private Cliente cliente;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaProducto> productos = new ArrayList<>();

    private Double total;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha;

    @JsonManagedReference
    @ToString.Exclude
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PagoVenta> pagos;

    private Double descuento;

    // Constructor con parámetros
    public Venta(Cliente cliente, List<PagoVenta> pagos, Double descuento) {
        this.cliente = cliente;
        this.fecha = LocalDateTime.now();
        this.pagos = pagos;
        this.descuento = descuento;
    }

    // Nuevo método: agregar un VentaProducto ya preparado
    public void agregarVentaProducto(VentaProducto ventaProducto) {
        if (ventaProducto != null) {
            ventaProducto.setVenta(this);
            this.productos.add(ventaProducto);
        }
    }

    // Calcular total una vez agregados productos y pagos
    public void calcularTotal() {
        // 🔥 Sumar los subtotales de cada `VentaProducto` en lugar de recalcular los descuentos
        this.total = productos.stream()
                .mapToDouble(VentaProducto::getSubtotal)
                .sum();

        double monto = 0;
        for (PagoVenta pago : pagos) {
            if (pago.getMetodo() == MetodoPago.CREDITO) {
                monto += pago.getMonto() * 1.15;
            } else {
                monto += pago.getMonto();
            }
        }
        this.total = monto;

        // 🔥 Aplicar descuento global a la venta (en porcentaje)
        if (descuento != null && descuento > 0) {
            this.total -= (this.total * (descuento / 100.0));
        }
    }

}
