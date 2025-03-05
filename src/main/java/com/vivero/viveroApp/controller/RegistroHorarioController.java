package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.RegistroHorario;
import com.vivero.viveroApp.model.Usuario;
import com.vivero.viveroApp.service.RegistroHorarioService;
import com.vivero.viveroApp.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/horas")
public class RegistroHorarioController {

    private final RegistroHorarioService registroHorarioService;

    private final UsuarioService usuarioService;

    @GetMapping
    public String mostrarPlantillaHoras(Model model) {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        model.addAttribute("usuarios", usuarios);

        // Establecer el mes y año corrientes
        YearMonth currentYearMonth = YearMonth.now();
        int currentMonth = currentYearMonth.getMonthValue();
        int currentYear = currentYearMonth.getYear();

        model.addAttribute("mes", currentMonth);
        model.addAttribute("año", currentYear);

        return "horas/plantillahoras";
    }

    @GetMapping("/cargarDias")
    @ResponseBody
    public Map<String, Object> cargarDias(
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam("mes") int mes,
            @RequestParam("año") int año) {

        Map<String, Object> response = new HashMap<>();
        Usuario usuario = usuarioService.getUsuarioById(usuarioId).orElse(null);

        if (usuario == null) {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
            return response;
        }

        List<RegistroHorario> registrosHorarios = registroHorarioService.getRegistrosByUsuarioAndMesAndAño(usuario, mes, año);

        response.put("success", true);
        response.put("registros", registrosHorarios);
        return response;
    }

    @PostMapping("/cargarDias")
    public String cargarDias(
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam("mes") int mes,
            @RequestParam("año") int año,
            Model model) {

        Usuario usuario = usuarioService.getUsuarioById(usuarioId).orElse(null);
        if (usuario == null) {
            model.addAttribute("mensajeError", "Usuario no encontrado");
            return "horas/plantillahoras";
        }

        List<RegistroHorario> registrosHorarios = registroHorarioService.getRegistrosByUsuarioAndMesAndAño(usuario, mes, año);

        model.addAttribute("usuarios", usuarioService.getAllUsuarios());
        model.addAttribute("usuarioSeleccionado", usuario);
        model.addAttribute("mes", mes);
        model.addAttribute("año", año);
        model.addAttribute("registrosHorarios", registrosHorarios);

        return "horas/plantillahoras";
    }

    @PostMapping("/guardar")
    @ResponseBody
    public Map<String, Object> guardar(
            @RequestParam(value = "id", required = false) Long id, // Hacemos que el parámetro id no sea requerido
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam("fecha") String fechaStr,
            @RequestParam("entradaTM") String entradaTM,
            @RequestParam("salidaTM") String salidaTM,
            @RequestParam("entradaTT") String entradaTT,
            @RequestParam("salidaTT") String salidaTT,
            @RequestParam("feriado") boolean feriado,
            @RequestParam("precioHora") double precioHora) {

        Usuario usuario = usuarioService.getUsuarioById(usuarioId).orElse(null);
        Map<String, Object> response = new HashMap<>();

        if (usuario == null) {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
            return response;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fecha = LocalDate.parse(fechaStr, formatter);

        RegistroHorario registroHorario = new RegistroHorario();
        if (id != null) {
            registroHorario.setId(id);  // Solo configuramos el ID si no es nulo
        }
        registroHorario.setUsuario(usuario);
        registroHorario.setFecha(fecha);
        registroHorario.setDiaSemana(fecha.getDayOfWeek().toString());
        registroHorario.setEntradaTM(entradaTM);
        registroHorario.setSalidaTM(salidaTM);
        registroHorario.setEntradaTT(entradaTT);
        registroHorario.setSalidaTT(salidaTT);
        registroHorario.setFeriado(feriado);
        registroHorario.setPrecioHora(precioHora);

        double totalHoras = calcularHoras(entradaTM, salidaTM) + calcularHoras(entradaTT, salidaTT);
        registroHorario.setTotalHoras(totalHoras);

        registroHorarioService.saveRegistro(registroHorario);

        response.put("success", true);
        response.put("message", "Datos guardados correctamente.");
        return response;
    }

    private double calcularHoras(String entrada, String salida) {
        if (entrada == null || salida == null || entrada.isEmpty() || salida.isEmpty()) {
            return 0;
        }

        String[] entradaSplit = entrada.split(":");
        String[] salidaSplit = salida.split(":");

        int entradaHoras = Integer.parseInt(entradaSplit[0]);
        int entradaMinutos = Integer.parseInt(entradaSplit[1]);
        int salidaHoras = Integer.parseInt(salidaSplit[0]);
        int salidaMinutos = Integer.parseInt(salidaSplit[1]);

        int totalEntradaMinutos = entradaHoras * 60 + entradaMinutos;
        int totalSalidaMinutos = salidaHoras * 60 + salidaMinutos;

        return (totalSalidaMinutos - totalEntradaMinutos) / 60.0;
    }

}
