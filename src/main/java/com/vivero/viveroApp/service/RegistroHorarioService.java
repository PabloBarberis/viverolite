package com.vivero.viveroApp.service;

import com.vivero.viveroApp.Repository.IngresoEgresoRepository;
import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.RegistroHorario;
import com.vivero.viveroApp.model.Usuario;
import com.vivero.viveroApp.repository.RegistroHorarioRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
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
    public List<RegistroHorario> getRegistrosByUsuarioAndMesAndAño(Usuario usuario, int mes, int año) {
        LocalDate startDate = LocalDate.of(año, mes, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return registroHorarioRepository.findByUsuarioAndFechaBetween(usuario, startDate, endDate);
    }

    @Transactional
    public RegistroHorario saveRegistro(RegistroHorario registroHorario) {
        return registroHorarioRepository.save(registroHorario);
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
