package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.Repository.IngresoEgresoRepository;
import com.vivero.viveroApp.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class IngresoEgresoService {

    private final IngresoEgresoRepository ingresoEgresoRepository;

    // Obtener todos los ingresos y egresos
    @Transactional(readOnly = true)
    public List<IngresoEgreso> getAllIngresoEgreso() {
        return ingresoEgresoRepository.findAll();
    }

    // Obtener un ingreso o egreso por su ID
    @Transactional(readOnly = true)
    public Optional<IngresoEgreso> getIngresoEgresoById(Long id) {
        return ingresoEgresoRepository.findById(id);
    }

    // Crear un nuevo ingreso o egreso
    @Transactional
    public IngresoEgreso createIngresoEgreso(IngresoEgreso ingresoEgreso) {
        return ingresoEgresoRepository.save(ingresoEgreso);
    }

    // Actualizar un ingreso o egreso existente
    @Transactional
    public IngresoEgreso updateIngresoEgreso(Long id, IngresoEgreso ingresoEgreso) {
        if (!ingresoEgresoRepository.existsById(id)) {
            throw new RuntimeException("IngresoEgreso no encontrado con ID: " + id);
        }
        ingresoEgreso.setId(id);
        return ingresoEgresoRepository.save(ingresoEgreso);
    }

    // Eliminar un ingreso o egreso por ID
    @Transactional
    public void deleteIngresoEgreso(Long id) {
        ingresoEgresoRepository.deleteById(id);
    }

    // Obtener ingresos y egresos por mes y año
    @Transactional(readOnly = true)
    public List<IngresoEgreso> getIngresoEgresoByMonth(int mes, int año) {
        LocalDateTime inicioMes = LocalDateTime.of(año, mes, 1, 0, 0);
        LocalDateTime finMes = inicioMes.withDayOfMonth(inicioMes.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        return ingresoEgresoRepository.findByFechaBetweenOrderByFechaAsc(inicioMes, finMes);
    }

    public List<IngresoEgreso> obtenerIngresosEgresosPorMesYAnio(int mes, int anio) {
        String mesStr = String.format("%02d", mes); // Convierte 3 → "03"
        String anioStr = String.valueOf(anio); // Convierte 2025 → "2025"
        return ingresoEgresoRepository.obtenerIngresosEgresosPorMesYAnio(mesStr, anioStr);
    }

    public List<IngresoEgreso> getAllAdelantos(Usuario usuario, int mes, int anio) {
        String mesStr = String.format("%02d", mes); // Convierte 3 → "03"
        String anioStr = String.valueOf(anio); // Convierte 2025 → "2025"
        
        return ingresoEgresoRepository.obtenerIngresosEgresosPorUsuarioMesAnioYAdelanto(mesStr, anioStr, usuario.getId(), true);
    }



}
