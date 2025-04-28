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
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RegistroHorarioService {

    private final RegistroHorarioRepository registroHorarioRepository;
    private final IngresoEgresoRepository IngresoEgresoRepository;
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
    System.out.println("INGRESANDO A GENERAR REPORTEPDF EN SERVICE");
    
    String mesStr = String.format("%02d", mes); // Convierte 3 → "03"
    String anioStr = String.valueOf(anio);      // Convierte 2025 → "2025"
    
    List<RegistroHorario> listaRegistrosHorarios = registroHorarioRepository.obtenerIngresosRegistrosHorariosPorMesYAnio(mesStr, anioStr);
    List<IngresoEgreso> ingresosEgresos = IngresoEgresoRepository.obtenerIngresosEgresosPorUsuarioMesAnioYAdelanto(mesStr, anioStr, id, true);

    List<String[]> datos = new ArrayList<>();

    datos.add(new String[]{"Registro Horario", ""});
    datos.add(new String[]{"Usuario", "Fecha"});

    for (RegistroHorario registro : listaRegistrosHorarios) {
        String nombreUsuario = registro.getUsuario() != null ? registro.getUsuario().getNombre() : "Sin Usuario";
        String fecha = registro.getFecha() != null ? registro.getFecha().toString() : "Sin Fecha";
        datos.add(new String[]{nombreUsuario, fecha});
    }

    datos.add(new String[]{"", ""});
    datos.add(new String[]{"Ingreso / Egreso", ""});
    datos.add(new String[]{"Descripción", "Monto"});

   for (IngresoEgreso ingresoEgreso : ingresosEgresos) {
    // Verificar si la descripción es nula o vacía
    String descripcion = (ingresoEgreso.getDescripcion() != null && !ingresoEgreso.getDescripcion().isEmpty()) 
                         ? ingresoEgreso.getDescripcion() 
                         : "Sin Descripción";

    // Verificar si el monto es nulo y asignar valor por defecto si es necesario
    double monto = ingresoEgreso.getMonto();
    String montoStr =String.valueOf(monto);

    datos.add(new String[]{descripcion, montoStr});
}


    String mesAnio = mesStr + "-" + anioStr;

    return pdfService.generarReporteHorarios(datos, mesAnio);
}

}
