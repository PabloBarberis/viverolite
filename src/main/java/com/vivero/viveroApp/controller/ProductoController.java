package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.service.PdfService;
import com.vivero.viveroApp.service.ProductoService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final PdfService pdfService;

    @PostMapping("/producto/actualizar-campo")
    public ResponseEntity<String> actualizarCampo(@RequestBody Map<String, String> payload) {
        Long id = Long.valueOf(payload.get("id"));
        String campo = payload.get("campo");
        String valor = payload.get("valor");

        // Lógica para detectar el tipo (Grow, Vapeador, etc.) si es necesario
        Optional<Producto> prodOptional = productoService.getProductoById(id);
        Producto producto = prodOptional.get();

        switch (campo) {
            case "precio":
                producto.setPrecio(Double.valueOf(valor));
                break;
            case "stock":
                producto.setStock(Integer.valueOf(valor));
                break;
            // Otros campos...
            default:
                return ResponseEntity.badRequest().body("Campo no válido");
        }

        productoService.saveProducto(producto);
        return ResponseEntity.ok("Campo actualizado");
    }

    @GetMapping("/producto/pdf")
    public ResponseEntity<byte[]> generarPDFPorProducto(
            @RequestParam String tipo,
            @RequestParam(required = false) String marca,
            HttpServletResponse response) throws Exception {

        List<Producto> productos = productoService.obtenerProductosPorMarca(tipo, marca);

        if (productos == null || productos.isEmpty()) {
            // Retorna un ResponseEntity con un mensaje de error y código 404 (No encontrado)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontraron productos para el tipo y marca proporcionados.".getBytes());
        }

        String[] headers = new String[]{"ID", "Nombre", "Marca", "Precio", "Stock"};

        Function<Object, String[]> rowMapper = obj -> {
            Producto p = (Producto) obj;
            return new String[]{
                String.valueOf(p.getId()),
                p.getNombre(),
                p.getMarca(),
                String.valueOf(p.getPrecio()),
                String.valueOf(p.getStock())
            };
        };

        try {
            byte[] pdfBytes = pdfService.generarPDF(productos, headers, rowMapper);

            // Retorna el ResponseEntity con el archivo PDF como respuesta
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + tipo.toLowerCase() + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (IOException e) {
            e.printStackTrace();
            // En caso de error, retornar un mensaje de error con código 500 (Internal Server Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el PDF.".getBytes());
        }
    }

}
