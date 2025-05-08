package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.Repository.IngresoEgresoRepository;
import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.RegistroHorario;
import com.vivero.viveroApp.model.Usuario;
import com.vivero.viveroApp.model.enums.MetodoPago;
import com.vivero.viveroApp.repository.UsuarioRepository;
import com.vivero.viveroApp.service.IngresoEgresoService;
import com.vivero.viveroApp.service.RegistroHorarioService;
import com.vivero.viveroApp.service.UsuarioService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ingresoegreso")
public class IngresoEgresoController {

    private final IngresoEgresoService ingresoEgresoService;

    private final IngresoEgresoRepository ingresoEgresoRepository;

    private final UsuarioService usuarioService;

    private final RegistroHorarioService registroHorarioService;

    private final UsuarioRepository usuarioRepository;
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
            // Capturar datos del request
            boolean ingreso = (boolean) requestData.get("ingreso");
            String metodoPagoStr = (String) requestData.get("metodoPago");
            Long usuarioId = Long.valueOf(requestData.get("usuarioId").toString());
            String fechaStr = (String) requestData.get("fecha");
            String descripcion = (String) requestData.get("descripcion");
            double monto = Double.parseDouble(requestData.get("monto").toString());
            boolean esAdelanto = (boolean) requestData.get("adelanto"); // Capturar el estado de 'adelanto'

            // Validar usuario
            Optional<Usuario> usuarioOptional = usuarioService.getUsuarioById(usuarioId);
            if (usuarioOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado.");
                return response;
            }

            // Validar método de pago
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

            // Crear entidad y asignar valores
            IngresoEgreso movimiento = new IngresoEgreso();

            movimiento.setIngreso(ingreso); // Primero, establecer el valor real de ingreso
            if (esAdelanto) {
                movimiento.setIngreso(false); // Si es adelanto, forzar ingreso a false
            }
            movimiento.setMetodoPago(metodoPago);
            movimiento.setUsuario(usuarioOptional.get());
            movimiento.setFecha(fecha);
            movimiento.setDescripcion(descripcion);
            movimiento.setMonto(monto);
            movimiento.setAdelanto(esAdelanto); // Asignar el valor del checkbox

            // Guardar movimiento en la base de datos
            ingresoEgresoService.createIngresoEgreso(movimiento);
            response.put("success", true);
        } catch (Exception e) {
            e.printStackTrace(); // 🔍 Ver el error exacto en la consola
            response.put("success", false);
            response.put("message", "Error guardando movimiento.");
        }

        return response;
    }

    @GetMapping("/totales")
    @ResponseBody
    public Map<String, Object> obtenerTotales(@RequestParam("usuarioId") Long usuarioId, @RequestParam("mes") int mes, @RequestParam("año") int año) {
        Map<String, Object> response = new HashMap<>();
        Usuario usuario = usuarioService.getUsuarioById(usuarioId).orElse(null);

        if (usuario == null) {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
            return response;
        }

        // Obtener total ganado
        List<RegistroHorario> registros = registroHorarioService.getRegistrosByUsuarioAndMesAndAño(usuario, mes, año);
        double totalGanado = registros.stream()
                .mapToDouble(registro -> registro.getTotalHoras() * registro.getPrecioHora() * (registro.isFeriado() ? 2 : 1))
                .sum();

        // Obtener total de adelantos
        List<IngresoEgreso> adelantos = ingresoEgresoService.getAllAdelantos(usuario, mes, año);
        double totalAdelantos = adelantos.stream()
                .mapToDouble(IngresoEgreso::getMonto)
                .sum();

        double totalNeto = totalGanado - totalAdelantos;

        response.put("success", true);
        response.put("totalGanado", totalGanado);
        response.put("totalAdelantos", totalAdelantos);
        response.put("totalNeto", totalNeto);
        response.put("adelantos", adelantos);

        return response;
    }

    @PostMapping("/eliminar")
    @Transactional
    @ResponseBody
    public void eliminarIngresoeGreso(@RequestBody Map<String, Long> payload) throws Exception {
        Long id = payload.get("id");
        System.out.println("ID RECIBIDO EN CONTROLLER: " + id);
        try {
            if (id != null || id != 0) {
                ingresoEgresoRepository.deleteById(id);
            } else {
                throw new Exception(" IngresoEgresoiD null");
            }
        } catch (Exception e) {
        }

    }

    @GetMapping("/editar/{id}")
    public ResponseEntity<?> obtenerIngresoEgreso(@PathVariable Long id) {
        Optional<IngresoEgreso> optional = ingresoEgresoRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IngresoEgreso ie = optional.get();

        // DTO para mandar solo los datos necesarios al frontend
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", ie.getId());
        dto.put("fecha", ie.getFecha().toString());
        dto.put("descripcion", ie.getDescripcion());
        dto.put("metodoPago", ie.getMetodoPago().name());
        dto.put("ingreso", ie.getIngreso());
        dto.put("monto", ie.getMonto());
        dto.put("usuarioId", ie.getUsuario().getId());
        dto.put("adelanto", ie.isAdelanto());

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/actualizar")
    public ResponseEntity<Map<String, Object>> actualizarIngresoEgreso(@RequestBody Map<String, Object> datos) {
        Map<String, Object> respuesta = new HashMap<>();

        try {
            Long id = Long.valueOf(datos.get("id").toString());
            Optional<IngresoEgreso> optional = ingresoEgresoRepository.findById(id);

            if (optional.isEmpty()) {
                respuesta.put("success", false);
                respuesta.put("message", "Ingreso/Egreso no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
            }

            IngresoEgreso ie = optional.get();

            // Actualizar campos
            ie.setDescripcion(datos.get("descripcion").toString());
            ie.setMetodoPago(MetodoPago.valueOf(datos.get("metodoPago").toString()));
            ie.setIngreso(Boolean.parseBoolean(datos.get("ingreso").toString()));
            ie.setMonto(Double.parseDouble(datos.get("monto").toString()));
            ie.setAdelanto(Boolean.parseBoolean(datos.get("adelanto").toString()));

            // Convertir fecha correctamente desde String tipo "yyyy-MM-ddTHH:mm:ss"
            String fechaStr = datos.get("fecha").toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime fechaLocal = LocalDateTime.parse(fechaStr, formatter);
            ie.setFecha(fechaLocal);

            // Cargar usuario
            Long usuarioId = Long.valueOf(datos.get("usuarioId").toString());
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            ie.setUsuario(usuario);

            ingresoEgresoRepository.save(ie);

            respuesta.put("success", true);
            respuesta.put("message", "Ingreso/Egreso actualizado correctamente");
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            respuesta.put("success", false);
            respuesta.put("message", "Error al actualizar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

}
