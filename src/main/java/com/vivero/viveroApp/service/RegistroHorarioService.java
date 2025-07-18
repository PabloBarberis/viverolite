package com.vivero.viveroApp.service;

import com.vivero.viveroApp.Repository.IngresoEgresoRepository;
import com.vivero.viveroApp.Repository.UsuarioRepository;
import com.vivero.viveroApp.dto.RegistroHorarioDTO;
import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.RegistroHorario;
import com.vivero.viveroApp.model.Usuario;
import com.vivero.viveroApp.Repository.RegistroHorarioRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RegistroHorarioService {

    private final RegistroHorarioRepository registroHorarioRepository;
    private final IngresoEgresoRepository ingresoEgresoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PdfService pdfService;

    @Transactional(readOnly = true)
    public List<RegistroHorario> getAllRegistros() {
        return registroHorarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<RegistroHorario> getRegistroById(Long id) {
        return registroHorarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<RegistroHorario> getRegistrosByUsuarioAndMesAndAño(Usuario usuario, int mes, int anio) {
        LocalDate startDate = LocalDate.of(anio, mes, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return registroHorarioRepository.findByUsuarioAndFechaBetweenOrderByFechaAsc(usuario, startDate, endDate);
    }

    public RegistroHorario guardarRegistro(RegistroHorarioDTO dto) {
        RegistroHorario registro;

        if (dto.getId() != null && dto.getId() > 0) {
            registro = registroHorarioRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Registro no encontrado"));

            // ✅ Si ya existe otro registro con esa fecha y usuario (y no es el mismo)
            if (!registro.getUsuario().getId().equals(dto.getUsuarioId())) {
                // Si el usuario cambia, validamos que no haya duplicado
                if (registroHorarioRepository.existsByUsuarioIdAndFecha(dto.getUsuarioId(), dto.getFecha())) {
                    throw new IllegalArgumentException("Ya existe un registro para este usuario en la fecha seleccionada");
                }
            } else {
                // Mismo usuario, pero ¿fecha diferente?
                if (!registro.getFecha().equals(dto.getFecha())) {
                    if (registroHorarioRepository.existsByUsuarioIdAndFecha(dto.getUsuarioId(), dto.getFecha())) {
                        throw new IllegalArgumentException("Ya existe un registro para este usuario en la fecha seleccionada");
                    }
                }
            }
        } else {
            // ✅ Validación al crear
            if (registroHorarioRepository.existsByUsuarioIdAndFecha(dto.getUsuarioId(), dto.getFecha())) {
                throw new IllegalArgumentException("Ya existe un registro para este usuario en la fecha seleccionada");
            }
            registro = new RegistroHorario();
        }

        // Asignar datos comunes (usuario, fecha, horarios, etc.)
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        registro.setUsuario(usuario);
        registro.setFecha(dto.getFecha());
        registro.setDiaSemana(registro.getFecha().getDayOfWeek().toString());
        registro.setEntradaTM(dto.getEntradaTM());
        registro.setSalidaTM(dto.getSalidaTM());
        registro.setEntradaTT(dto.getEntradaTT());
        registro.setSalidaTT(dto.getSalidaTT());
        registro.setFeriado(dto.isFeriado());
        registro.setPrecioHora(dto.getPrecioHora());

        registro.setTotalHoras(calcularTotalHoras(
                dto.getEntradaTM(), dto.getSalidaTM(), dto.getEntradaTT(), dto.getSalidaTT()
        ));

        return registroHorarioRepository.save(registro);
    }

    public static double calcularTotalHoras(LocalTime entradaTM, LocalTime salidaTM, LocalTime entradaTT, LocalTime salidaTT) {
        double total = 0.0;

        if (entradaTM != null && salidaTM != null && !salidaTM.isBefore(entradaTM)) {
            total += entradaTM.until(salidaTM, ChronoUnit.MINUTES) / 60.0;
        }

        if (entradaTT != null && salidaTT != null && !salidaTT.isBefore(entradaTT)) {
            total += entradaTT.until(salidaTT, ChronoUnit.MINUTES) / 60.0;
        }

        return Math.round(total * 100.0) / 100.0; // 2 decimales
    }

    @Transactional
    public void deleteRegistroById(Long id) {
        registroHorarioRepository.deleteById(id);
    }


    public byte[] generarReportePdf(int mes, int anio, Long id) throws Exception {
        String mesStr = String.format("%02d", mes);
        String anioStr = String.valueOf(anio);

        List<RegistroHorario> listaRegistrosHorarios = new ArrayList<>();
        List<IngresoEgreso> ingresosEgresos = new ArrayList<>();

        try {
            listaRegistrosHorarios = registroHorarioRepository.obtenerIngresosRegistrosHorariosPorMesAnioYUsuario(mesStr, anioStr, id);
        } catch (Exception e) {
            System.err.println("Error al obtener registros horarios: " + e.getMessage());
        }
        try {
            ingresosEgresos = ingresoEgresoRepository.obtenerIngresosEgresosPorUsuarioMesAnioYAdelanto(mesStr, anioStr, id, true);
        } catch (Exception e) {
            System.err.println("Error al obtener ingresos/egresos: " + e.getMessage());
        }
        if (listaRegistrosHorarios.isEmpty() && ingresosEgresos.isEmpty()) {
            return new byte[0];
        }
        return pdfService.generarPdf(listaRegistrosHorarios, ingresosEgresos);
    }
}
