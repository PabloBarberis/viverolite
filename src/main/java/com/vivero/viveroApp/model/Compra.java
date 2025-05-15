package com.vivero.viveroApp.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;
    private String comentario;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    private List<ProductoCompra> productos;

    @Override
    public String toString() {
        return "Compra{id=" + id + ", fecha=" + fecha + ", comentario=" + comentario + "}";
    }

}
