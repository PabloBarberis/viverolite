package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.service.ProductoService;
import java.util.List;
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

    @PostMapping("/aumentoDescuento")
    public ResponseEntity<String> aumentoDescuento(@RequestBody Map<String, Object> datos) {
        try {
            
            String accion = (String) datos.get("tipoAccion");
            String productoStr = (String) datos.get("tipoProducto");
            double porcentaje = Double.parseDouble(datos.get("porcentaje").toString());
            String marca = (String) datos.get("marca");
            String interiorExterior =(String) datos.get("interiorExterior");
            
            productoService.actualizarPrecios(accion, productoStr, porcentaje, marca, interiorExterior);

            return ResponseEntity.ok("Precios actualizados con éxito");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud: " + e.getMessage());
        }
    }
    
    @GetMapping("/marcas")
    @ResponseBody
    public List<String> obtenerMarcas(@RequestParam String tipo) {

        return productoService.mostrarMarcaPorProducto(tipo);
    }

}
