package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.Repository.CompraRepository;
import com.vivero.viveroApp.dto.CompraDTO;
import com.vivero.viveroApp.dto.ProductoDTO;
import com.vivero.viveroApp.model.Compra;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.model.ProductoCompra;
import com.vivero.viveroApp.Repository.ProductoRepository;
import com.vivero.viveroApp.service.PdfService;
import com.vivero.viveroApp.service.ProductoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class CompraController {

    private final PdfService pdfService;
    private final ProductoService productoService;
    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;

    @GetMapping("/pedido")
    public String crearPedido() {
        return "compras/pedido";
    }

    @PostMapping("/pedido/generarPedido")
    public ResponseEntity<byte[]> generarPedido(@RequestBody List<ProductoDTO> productos) throws Exception {

        byte[] pdfBytes = pdfService.generarPedidoPDF(productos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "pedido.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/compra")
    public String crearCompra() {
        return "compras/ingresarcompra";
    }

    @PostMapping("/ingresarcompra")
    public ResponseEntity<String> ingresarCompra(@RequestBody CompraDTO compra) {
        if (compra.getProductos() == null || compra.getProductos().isEmpty()) {
            return ResponseEntity.badRequest().body("Debe agregar al menos un producto a la compra.");
        }

        try {
            productoService.ingresarCompra(compra.getComentario(), compra.getProductos());
            return ResponseEntity.ok("Compra ingresada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al procesar la compra: " + e.getMessage());
        }
    }

    @GetMapping("/compras/listar")
    public String listarCompras() {
        return "compras/listar-compras";
    }

    @GetMapping("/compras/mostrar")
    @ResponseBody
    public List<CompraDTO> mostrarCompras() {
        List<Compra> compras = compraRepository.findAll();

        System.out.println("Compras encontradas: " + compras); // 🔥 Ver qué devuelve el backend

        return compras.stream().map(compra -> {
            CompraDTO dto = new CompraDTO();
            dto.setId(compra.getId());
            dto.setFecha(compra.getFecha());
            dto.setComentario(compra.getComentario());
            dto.setProductos(null);
            return dto;
        }).toList();
    }

    @GetMapping("/compras/detalle/{id}")
    @ResponseBody
    public ResponseEntity<CompraDTO> obtenerCompraPorId(@PathVariable Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada: " + id));

        CompraDTO dto = new CompraDTO();
        dto.setId(compra.getId());
        dto.setFecha(compra.getFecha());
        dto.setComentario(compra.getComentario());

        // 🔥 Obtener productos vinculados a esta compra
        List<ProductoDTO> productos = compra.getProductos().stream().map(productoCompra -> {
            ProductoDTO productoDTO = new ProductoDTO();
            productoDTO.setId(productoCompra.getProducto().getId());
            productoDTO.setNombre(productoCompra.getProducto().getNombre());
            productoDTO.setPrecioCompra(productoCompra.getPrecioCompra());
            productoDTO.setPrecio(productoCompra.getPrecioVenta());
            productoDTO.setCantidad(productoCompra.getCantidad());
            return productoDTO;
        }).toList();

        dto.setProductos(productos);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/compras/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<String> eliminarCompra(@PathVariable Long id) {
        try {
            Compra compra = compraRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("La compra no existe."));

            // 🔥 Restituir stock de los productos
            for (ProductoCompra productoCompra : compra.getProductos()) {
                Producto producto = productoCompra.getProducto();
                producto.setStock(producto.getStock() - productoCompra.getCantidad()); // 🔥 Restar el stock
                productoRepository.save(producto);
            }

            // 🔥 Ahora sí eliminar la compra
            compraRepository.delete(compra);

            return ResponseEntity.ok("Compra eliminada y stock restituido correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la compra: " + e.getMessage());
        }
    }

}
