package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.Repository.RegistroHorarioRepository;
import com.vivero.viveroApp.dto.AdelantoDTO;
import com.vivero.viveroApp.dto.AdelantoListDTO;
import com.vivero.viveroApp.dto.RegistroHorarioDTO;
import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.RegistroHorario;
import com.vivero.viveroApp.model.Usuario;
import com.vivero.viveroApp.service.IngresoEgresoService;
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
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequiredArgsConstructor
@RequestMapping("/horas")
public class RegistroHorarioController {

    private final RegistroHorarioService registroHorarioService;
    private final RegistroHorarioRepository registroHorarioRepository;
    private final IngresoEgresoService ingresoEgresoService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String mostrarPlantillaHoras() {
        return "horas/plantillahoras";
    }

    @GetMapping("/api/registroshorarios")
    @ResponseBody
    public List<RegistroHorario> getRegistrosHorarios(
            @RequestParam Long usuarioId,
            @RequestParam int mes,
            @RequestParam int anio) {

        Usuario usuario = usuarioService.getUsuarioById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return registroHorarioService.getRegistrosByUsuarioAndMesAndAño(usuario, mes, anio);
    }

    @GetMapping("/api/registroshorarios/{id}")
    @ResponseBody
    public ResponseEntity<RegistroHorario> getRegistroPorId(@PathVariable Long id) {
        return registroHorarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/api/registroshorarios")
    public ResponseEntity<?> crearRegistro(@RequestBody RegistroHorarioDTO dto) {
        try {
            dto.setId(null); // Aseguramos que sea un registro nuevo
            RegistroHorario registro = registroHorarioService.guardarRegistro(dto);
            return ResponseEntity.ok(registro);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/api/registroshorarios/{id}")
    public ResponseEntity<?> actualizarRegistro(@PathVariable Long id, @RequestBody RegistroHorarioDTO dto) {
        try {
            dto.setId(id);
            RegistroHorario registro = registroHorarioService.guardarRegistro(dto);
            return ResponseEntity.ok(registro);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/api/adelantos")
    @ResponseBody
    public ResponseEntity<AdelantoListDTO> getAdelantosPorUsuario(
            @RequestParam Long usuarioId,
            @RequestParam int mes,
            @RequestParam int año) {

        Optional usuarioOptional = usuarioService.getUsuarioById(usuarioId);
        Usuario usuario = (Usuario) usuarioOptional.get();

        List<IngresoEgreso> ingresosEgresos = ingresoEgresoService.getAllAdelantos(usuario, mes, año);

        // Convertir a DTO
        List<AdelantoDTO> dtoList = ingresosEgresos.stream()
                .map(ie -> new AdelantoDTO(ie.getId(), ie.getFecha().toLocalDate(), ie.getDescripcion(), ie.getMonto()))
                .toList();

        // Devolver lista + total
        AdelantoListDTO dto = new AdelantoListDTO(dtoList);

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/api/registroshorarios/{id}")
    public ResponseEntity<Void> eliminarRegistro(@PathVariable Long id) {
        if (!registroHorarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        registroHorarioRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reporte-pdf")
    public ResponseEntity<byte[]> generarReportePdf(
            @RequestParam int mes,
            @RequestParam int anio,
            @RequestParam Long id) {

        System.out.println("CONTROLLER");
        System.out.println("MES :" + mes);
        System.out.println("ANIO :" + anio);
        System.out.println("ID : " + id);
        try {
            byte[] pdfBytes = registroHorarioService.generarReportePdf(mes, anio, id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte_ventas_" + anio + "_" + mes + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
