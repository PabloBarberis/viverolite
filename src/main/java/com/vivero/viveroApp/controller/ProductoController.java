package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.dto.ProductoDTO;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.repository.ProductoRepository;
import com.vivero.viveroApp.service.PdfService;
import com.vivero.viveroApp.service.ProductoService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoRepository productoRepository;
    private final PdfService pdfService;

    @PostMapping("/producto/actualizar-campo")
    public ResponseEntity<String> actualizarCampo(@RequestBody Map<String, String> payload) {
        try {
            Long id = Long.valueOf(payload.get("id"));
            String campo = payload.get("campo");
            String valor = payload.get("valor");

            productoService.actualizarCampo(id, campo, valor);
            return ResponseEntity.ok("Campo actualizado");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
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

    @GetMapping("/api/productos")
    @ResponseBody
    public List<ProductoDTO> buscarProductos(@RequestParam String q) {
        return productoRepository.buscarProductoPorNombre(q);
    }

}
