package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.Repository.IngresoEgresoRepository;
import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.RegistroHorario;
import com.vivero.viveroApp.model.Usuario;
import com.vivero.viveroApp.model.enums.MetodoPago;
import com.vivero.viveroApp.Repository.UsuarioRepository;
import com.vivero.viveroApp.service.IngresoEgresoService;
import com.vivero.viveroApp.service.RegistroHorarioService;
import com.vivero.viveroApp.service.UsuarioService;

import java.time.LocalDate;
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

    @GetMapping
    public String mostrarVistaIngresoEgreso() {
        return "ventas/entrada-salida";
    }

    @GetMapping("/metodos-pago")
    @ResponseBody
    public List<String> getMetodosPago() {
        return Arrays.stream(MetodoPago.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @PostMapping("/guardar")
    public ResponseEntity<IngresoEgreso> guardarIngresoEgreso(@RequestBody Map<String, Object> payload) {
        IngresoEgreso ingresoEgreso;

        // Si viene un ID, es edición
        if (payload.containsKey("id") && payload.get("id") != null) {
            Long id = Long.valueOf(payload.get("id").toString());
            ingresoEgreso = ingresoEgresoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("No encontrado"));
        } else {
            ingresoEgreso = new IngresoEgreso();
        }

        // Mapear campos
        ingresoEgreso.setIngreso((Boolean) payload.get("tipoMovimiento"));
        ingresoEgreso.setMetodoPago(MetodoPago.valueOf((String) payload.get("metodoPago")));

        // Usuario
        Map<String, Object> usuarioMap = (Map<String, Object>) payload.get("usuario");
        Long usuarioId = Long.valueOf(usuarioMap.get("id").toString());
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        ingresoEgreso.setUsuario(usuario);

        // Otros campos
        LocalDate fecha = LocalDate.parse((String) payload.get("fecha"));
        LocalDateTime fechaConHora = fecha.atStartOfDay(); // Le pone las 00:00:00
        ingresoEgreso.setFecha(fechaConHora);
        ingresoEgreso.setDescripcion((String) payload.get("descripcion"));
        ingresoEgreso.setMonto(Double.parseDouble(payload.get("monto").toString()));
        ingresoEgreso.setAdelanto((Boolean) payload.get("adelanto"));
        if (ingresoEgreso.isAdelanto()){
            ingresoEgreso.setIngreso(false);
        }

        // Guardar
        IngresoEgreso guardado = ingresoEgresoRepository.save(ingresoEgreso);
        return ResponseEntity.ok(guardado);
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

        List<RegistroHorario> registros = registroHorarioService.getRegistrosByUsuarioAndMesAndAño(usuario, mes, año);
        double totalGanado = registros.stream()
                .mapToDouble(registro -> registro.getTotalHoras() * registro.getPrecioHora() * (registro.isFeriado() ? 2 : 1))
                .sum();

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
    @ResponseBody
    public Map<String, String> eliminarIngresoeGreso(@RequestBody Map<String, Long> payload) {
        Long id = payload.get("id");

        if (id == null || id == 0) {
            Map<String, String> res = new HashMap<>();
            res.put("error", "ID inválido");
            return res;
        }

        try {
            ingresoEgresoRepository.deleteById(id);
            Map<String, String> res = new HashMap<>();
            res.put("mensaje", "Eliminado con éxito");
            return res;

        } catch (Exception e) {
            Map<String, String> res = new HashMap<>();
            res.put("error", "Error al eliminar");
            return res;
        }
    }

    @GetMapping("/editar/{id}")
    public ResponseEntity<?> obtenerIngresoEgreso(@PathVariable Long id) {
        Optional<IngresoEgreso> optional = ingresoEgresoRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IngresoEgreso ie = optional.get();

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", ie.getId());
        dto.put("fecha", ie.getFecha().toString());
        dto.put("descripcion", ie.getDescripcion());
        dto.put("metodoPago", ie.getMetodoPago().name());
        dto.put("tipoMovimiento", ie.getIngreso());
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


            ie.setDescripcion(datos.get("descripcion").toString());
            ie.setMetodoPago(MetodoPago.valueOf(datos.get("metodoPago").toString()));

            ie.setIngreso((Boolean) datos.get("tipoMovimiento"));
            ie.setMonto(Double.parseDouble(datos.get("monto").toString()));
            ie.setAdelanto(Boolean.parseBoolean(datos.get("adelanto").toString()));

            LocalDate fecha = LocalDate.parse((String) datos.get(("fecha")));
            LocalDateTime fechaConHora = fecha.atStartOfDay(); // Le pone las 00:00:00
            ie.setFecha(fechaConHora);
            // Usuario
            Map<String, Object> usuarioMap = (Map<String, Object>) datos.get("usuario");
            Long usuarioId = Long.valueOf(usuarioMap.get("id").toString());
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            ie.setUsuario(usuario);

            if (ie.isAdelanto()){
                ie.setIngreso(false);
            }

            ingresoEgresoRepository.save(ie);

            respuesta.put("success", true);
            respuesta.put("message", "Ingreso/Egreso actualizado correctamente");
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            e.printStackTrace(); // o log error
            respuesta.put("success", false);
            respuesta.put("message", "Error al actualizar: " + (e.getMessage() != null ? e.getMessage() : "Excepción sin mensaje (" + e.getClass().getSimpleName() + ")"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }

    }

}
