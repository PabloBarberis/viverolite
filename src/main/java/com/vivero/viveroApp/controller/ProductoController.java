package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.dto.ProductoDTO;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.repository.ProductoRepository;
import com.vivero.viveroApp.repository.ProveedorRepository;
import com.vivero.viveroApp.service.PdfService;
import com.vivero.viveroApp.service.ProductoService;
import com.vivero.viveroApp.service.ProveedorService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoRepository productoRepository;
    private final ProveedorService proveedorService;
    private final ProveedorRepository proveedorRepository;
    private final PdfService pdfService;

    @GetMapping("/productos/listar")
    public String listarProductos() {
        return "producto/listar-producto";
    }

    @GetMapping("/productos/crear")
    public String mostrarFormularioCrearProducto(Model model) {

        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();
        model.addAttribute("proveedores", proveedores);
        return "producto/crear-producto";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(
            @RequestParam String nombre,
            @RequestParam String tipo,
            @RequestParam double precio,
            @RequestParam int stock,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String marcaNueva,
            @RequestParam(name = "proveedoresId", required = false) List<Long> proveedoresId
    ) {

        if (tipo.isBlank() || precio <= 0 || stock < 0) {
            return "redirect:/productos/crear?error=Campos+obligatorios+faltantes";
        }

        String marcaFinal = (marcaNueva != null && !marcaNueva.isBlank()) ? marcaNueva : marca;
        Producto producto = new Producto();

        producto.setActivo(true);
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setDescripcion(descripcion != null ? descripcion : "");
        producto.setMarca(marcaFinal != null ? marcaFinal : "");
        producto.setTipo(tipo);

        if (proveedoresId != null && !proveedoresId.isEmpty()) {
            List<Proveedor> proveedores = proveedorRepository.findAllById(proveedoresId);
            producto.setProveedores(proveedores);
        }

        productoService.createProducto(producto);

        return "redirect:/productos/listar";
    }

    @GetMapping("/productos/listar-datos")
    public ResponseEntity<Page<Producto>> obtenerProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String tipo) {

        Page<Producto> productoPage = productoService.buscarProductoPaginado(nombre, marca, tipo, page, size);

        return ResponseEntity.ok(productoPage);
    }

    @GetMapping("/productos/editar/{id}")
    public String mostrarFormularioEditarProducto(@PathVariable Long id, Model model) {

        Producto producto = productoService.getProductoById(id).orElse(null);

        if (producto == null) {
            return "redirect:/productos/listar";
        }

        List<Proveedor> proveedores = proveedorService.getAllProveedoresActivos();

        model.addAttribute("producto", producto);
        model.addAttribute("dtype", producto.getTipo());
        model.addAttribute("proveedores", proveedores);

        return "producto/crear-producto";
    }

    @PutMapping("/productos/editar/{id}")
    public ResponseEntity<String> actualizarProducto(@PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String tipo,
            @RequestParam double precio,
            @RequestParam int stock,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String marcaNueva,
            @RequestParam(name = "proveedoresId", required = false) List<Long> proveedoresId) {

        if (!productoService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Producto productoExistente = productoService.getProductoById(id).orElse(null);
        if (productoExistente == null) {
            return ResponseEntity.notFound().build();
        }

        productoExistente.setTipo(tipo);
        productoExistente.setActivo(true);
        productoExistente.setNombre(nombre);
        productoExistente.setPrecio(precio);
        productoExistente.setStock(stock);
        productoExistente.setDescripcion(descripcion);
        productoExistente.setMarca(marca);

if (proveedoresId != null) {
    if (proveedoresId.size() == 1 && proveedoresId.get(0) == 0L) {
        // Si la lista tiene un solo elemento igual a 0 (o algún valor "vacío" que uses), la tratamos como vacía
        productoExistente.setProveedores(Collections.emptyList());
    } else {
        List<Proveedor> proveedores = proveedorRepository.findAllById(proveedoresId);
        productoExistente.setProveedores(proveedores);
    }
}

        productoService.saveProducto(productoExistente);
        return ResponseEntity.ok("Producto actualizado exitosamente");
    }

    @GetMapping("/productos/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerProducto(@PathVariable Long id) {
        return productoService.getProductoById(id)
                .map(producto -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", producto.getId());
                    response.put("nombre", producto.getNombre());
                    response.put("marca", producto.getMarca());
                    response.put("dtype", producto.getTipo());
                    response.put("precio", producto.getPrecio());
                    response.put("stock", producto.getStock());
                    response.put("descripcion", producto.getDescripcion());
                    response.put("proveedores", producto.getProveedores());

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/productos/dar-de-baja")
    public String darDeBajaProducto(@RequestParam Long id) {
        productoService.darDeBaja(id);
        return "redirect:/productos/listar";
    }

    @PostMapping("/productos/actualizar-campo")
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

    @GetMapping("/api/productos")
    @ResponseBody
    public List<ProductoDTO> buscarProductos(@RequestParam String q) {
        return productoRepository.buscarProductoPorNombre(q);
    }

    @GetMapping("/api/marcas")
    @ResponseBody
    public List<String> obtenerMarcasPorTipo(@RequestParam String tipo) {
        return productoRepository.findDistinctMarcasByDtype(tipo);
    }

    @GetMapping("/productos/pdf")
    public ResponseEntity<byte[]> generarPDFPorProducto(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca) {

        try {
            System.out.println("TIPO: " + tipo);
            System.out.println("NOMBRE: " + nombre);
            System.out.println("MARCA: " + marca);

            List<Producto> productos = productoService.obtenerProductosFiltrados(tipo, nombre, marca);

            if (productos == null || productos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            byte[] pdfBytes = pdfService.generarPDFPorProducto(productos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            String filename = (tipo != null) ? tipo.toLowerCase() + ".pdf" : "productos.pdf";
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
