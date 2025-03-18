package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.Usuario;
import com.vivero.viveroApp.model.enums.MetodoPago;
import com.vivero.viveroApp.service.IngresoEgresoService;
import com.vivero.viveroApp.service.UsuarioService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ingresoegreso")
public class IngresoEgresoController {

    private final IngresoEgresoService ingresoEgresoService;

    private final UsuarioService usuarioService;

    // Cargar la vista de ingresos/egresos
    @GetMapping
    public String mostrarVistaIngresoEgreso() {
        return "ventas/entrada-salida"; // Nombre del archivo HTML en templates
    }

    // Obtener lista de métodos de pago para el select
    @GetMapping("/metodos-pago")
    @ResponseBody
    public List<String> getMetodosPago() {
        return Arrays.stream(MetodoPago.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @PostMapping("/guardar")
    @ResponseBody
    public Map<String, Object> guardarIngresoEgreso(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();

        try {

            boolean ingreso = (boolean) requestData.get("ingreso");
            String metodoPagoStr = (String) requestData.get("metodoPago");
            Long usuarioId = Long.valueOf(requestData.get("usuarioId").toString());
            String fechaStr = (String) requestData.get("fecha");
            String descripcion = (String) requestData.get("descripcion");
            double monto = Double.parseDouble(requestData.get("monto").toString());

            Optional<Usuario> usuarioOptional = usuarioService.getUsuarioById(usuarioId);
            if (usuarioOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado.");
                return response;
            }

            MetodoPago metodoPago = MetodoPago.valueOf(metodoPagoStr.toUpperCase());

            // Convertir la fecha de String a LocalDateTime
            LocalDateTime fecha;
            try {
                fecha = LocalDateTime.parse(fechaStr);
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Formato de fecha inválido. Use: 'YYYY-MM-DDTHH:MM:SS'");
                return response;
            }

            IngresoEgreso movimiento = new IngresoEgreso();
            movimiento.setIngreso(ingreso);
            movimiento.setMetodoPago(metodoPago);
            movimiento.setUsuario(usuarioOptional.get());
            movimiento.setFecha(fecha);
            movimiento.setDescripcion(descripcion);
            movimiento.setMonto(monto);
            
            System.out.println("movimiento :" + movimiento);

            ingresoEgresoService.createIngresoEgreso(movimiento); // Llamamos a createIngresoEgreso
            response.put("success", true);
        } catch (Exception e) {
            e.printStackTrace(); // 🔍 Ver el error exacto en la consola
            response.put("success", false);
            response.put("message", "Error guardando movimiento.");
        }

        return response;
    }

}
