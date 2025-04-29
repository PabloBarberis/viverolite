package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.dto.ProductoDTO;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.repository.ProductoRepository;
import com.vivero.viveroApp.service.PdfService;
import com.vivero.viveroApp.service.ProductoService;
import java.util.List;
import java.util.Map;
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
            @RequestParam(required = false) String marca) {
        try {
            // Obtener los productos filtrados por tipo y marca
            List<Producto> productos = productoService.obtenerProductosPorMarca(tipo, marca);

            if (productos == null || productos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Generar el PDF utilizando el servicio
            byte[] pdfBytes = pdfService.generarPDFPorProducto(productos);

            // Configurar los headers para la respuesta HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", tipo.toLowerCase() + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/productos")
    @ResponseBody
    public List<ProductoDTO> buscarProductos(@RequestParam String q) {
        return productoRepository.buscarProductoPorNombre(q);
    }

}
