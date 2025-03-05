package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Adelanto;
import com.vivero.viveroApp.model.RegistroHorario;
import com.vivero.viveroApp.model.Usuario;
import com.vivero.viveroApp.service.AdelantoService;
import com.vivero.viveroApp.service.RegistroHorarioService;
import com.vivero.viveroApp.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Controller
@RequiredArgsConstructor
@RequestMapping("/adelantos")
public class AdelantoController {

    private final AdelantoService adelantoService;
    
    private final UsuarioService usuarioService;
    
    private final RegistroHorarioService registroHorarioService;


    @GetMapping
    public String mostrarPlantillaAdelantos(Model model) {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        model.addAttribute("usuarios", usuarios);

        // Establecer el mes y año corrientes
        YearMonth currentYearMonth = YearMonth.now();
        int currentMonth = currentYearMonth.getMonthValue();
        int currentYear = currentYearMonth.getYear();

        model.addAttribute("mes", currentMonth);
        model.addAttribute("año", currentYear);

        return "adelantos/plantillaadelantos";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Adelanto> getAdelantoById(@PathVariable Long id) {
        Optional<Adelanto> adelanto = adelantoService.findById(id);
        if (adelanto.isPresent()) {
            return ResponseEntity.ok(adelanto.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/cargarAdelantos")
    @ResponseBody
    public Map<String, Object> cargarAdelantos(@RequestParam("usuarioId") Long usuarioId, @RequestParam("mes") int mes, @RequestParam("año") int año) {
        Map<String, Object> response = new HashMap<>();
        Usuario usuario = usuarioService.getUsuarioById(usuarioId).orElse(null);

        if (usuario == null) {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
            return response;
        }

        List<Adelanto> adelantos = adelantoService.getAdelantosByUsuarioAndMesAndAño(usuario, mes, año);
        response.put("success", true);
        response.put("adelantos", adelantos);
        return response;
    }

    @PostMapping("/guardar")
    @ResponseBody
    public Map<String, Object> guardarAdelanto(@RequestBody Adelanto adelanto) {
        Map<String, Object> response = new HashMap<>();
        Adelanto savedAdelanto = adelantoService.saveAdelanto(adelanto);
        response.put("success", true);
        response.put("adelanto", savedAdelanto);
        return response;
    }

    @PutMapping("/editar")
    @ResponseBody
    public Map<String, Object> editarAdelanto(@RequestBody Adelanto adelanto) {
        Map<String, Object> response = new HashMap<>();
        if (adelanto.getId() == null || !adelantoService.existsById(adelanto.getId())) {
            response.put("success", false);
            response.put("message", "Adelanto no encontrado");
            return response;
        }

        Adelanto updatedAdelanto = adelantoService.saveAdelanto(adelanto);
        response.put("success", true);
        response.put("adelanto", updatedAdelanto);
        return response;
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public Map<String, Object> eliminarAdelanto(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Adelanto> adOptional = adelantoService.findById(id);
        
        Adelanto adelanto = adOptional.get();

        if (adelanto == null) {
            response.put("success", false);
            response.put("message", "Adelanto no encontrado");
            return response;
        }

        adelantoService.deleteAdelanto(id);
        response.put("success", true);
        response.put("message", "Adelanto eliminado correctamente");
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
                .mapToDouble(registro -> registro.getTotalHoras() * registro.getPrecioHora())
                .sum();

        
        

        // Obtener total de adelantos
        List<Adelanto> adelantos = adelantoService.getAdelantosByUsuarioAndMesAndAño(usuario, mes, año);
        double totalAdelantos = adelantos.stream()
                .mapToDouble(Adelanto::getCantidad)
                .sum();

        double totalNeto = totalGanado  - totalAdelantos;

        response.put("success", true);
        response.put("totalGanado", totalGanado);
        response.put("totalAdelantos", totalAdelantos);
        response.put("totalNeto", totalNeto);

        return response;
    }

}
