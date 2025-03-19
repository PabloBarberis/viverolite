package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.service.ProductoService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/precios")
public class PrecioController {

    
    private final ProductoService productoService;

    @GetMapping
    public String actualizarPrecios() {
        return "precios/precios";
    }

    @PostMapping("/aumentar")
    public ResponseEntity<String> aumentarPrecios(@RequestBody Map<String, Object> datos) {
        try {
            String tipoProducto = (String) datos.get("tipoProducto");
            double porcentaje = Double.parseDouble(datos.get("porcentaje").toString());
            productoService.aumentarPrecios(tipoProducto, porcentaje);
            return ResponseEntity.ok("Precios actualizados con éxito");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    @PostMapping("/descuento")
    public ResponseEntity<String> aplicarDescuento(@RequestBody Map<String, Object> datos) {

        try {
            String tipoProducto = (String) datos.get("tipoProducto");
            double porcentaje = Double.parseDouble(datos.get("porcentaje").toString());

            productoService.aplicarDescuento(tipoProducto, porcentaje);
            return ResponseEntity.ok("Descuento aplicado con éxito");
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud: " + e.getMessage());
        }

    }
}
