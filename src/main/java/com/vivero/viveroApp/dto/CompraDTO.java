package com.vivero.viveroApp.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class CompraDTO {

    private String comentario;
    private List<ProductoDTO> productos;
    private Long id;
    private LocalDateTime fecha;
}
