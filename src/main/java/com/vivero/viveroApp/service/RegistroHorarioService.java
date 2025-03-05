package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.RegistroHorario;
import com.vivero.viveroApp.model.Usuario;
import com.vivero.viveroApp.repository.RegistroHorarioRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RegistroHorarioService {

    private final RegistroHorarioRepository registroHorarioRepository;

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
}
