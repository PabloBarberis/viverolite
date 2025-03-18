
package com.vivero.viveroApp.service;

import com.vivero.viveroApp.Repository.SemillaRepository;
import com.vivero.viveroApp.model.Semilla;
import com.vivero.viveroApp.model.Proveedor;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@RequiredArgsConstructor
@Service
public class SemillaService {
    
    private final SemillaRepository semillaRepository;

    @Transactional(readOnly = true)
    public Page<Semilla> getAllSemillasActivasPaginadas(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return semillaRepository.findByActivoTrue(pageable);
    }
    
    @Transactional(readOnly = true)
    public Optional<Semilla> getSemillaById(Long id) {
        return semillaRepository.findByIdAndActivoTrue(id);
    }
    
    @Transactional
    public Semilla createSemilla(Semilla semilla) {
        semilla.setActivo(true);
        for (Proveedor proveedor : semilla.getProveedores()) {
            proveedor.getProductos().add(semilla);
        }
        return semillaRepository.save(semilla);
    }
    
    @Transactional
    public Semilla updateSemilla(Long id, Semilla semillaDetails) {
        Semilla semilla = semillaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Semilla no encontrada con ID: " + id));

        semilla.setNombre(semillaDetails.getNombre());        
        semilla.setPrecio(semillaDetails.getPrecio());
        semilla.setStock(semillaDetails.getStock());
        semilla.setDescripcion(semillaDetails.getDescripcion());

        // Actualizar los proveedores
        if (semillaDetails.getProveedores() != null) {
            for (Proveedor proveedor : semilla.getProveedores()) {
                proveedor.getProductos().remove(semilla);
            }
            semilla.getProveedores().clear();

            for (Proveedor proveedor : semillaDetails.getProveedores()) {
                semilla.getProveedores().add(proveedor);
                proveedor.getProductos().add(semilla);
            }
        } else {
            for (Proveedor proveedor : semilla.getProveedores()) {
                proveedor.getProductos().remove(semilla);
            }
            semilla.getProveedores().clear();
        }

        return semillaRepository.save(semilla);
    }
    
    @Transactional
    public void darDeBajaSemilla(Long id) {
        Semilla semilla = semillaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Semilla no encontrada con ID: " + id));
        semilla.setActivo(false);
        semillaRepository.save(semilla);
    }
    
    @Transactional(readOnly = true)
    public Page<Semilla> buscarSemillaPaginado(String nombre, String tipo, String proveedor, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return semillaRepository.buscarSemilla(
                nombre != null && !nombre.isEmpty() ? nombre : null,
                tipo != null && !tipo.isEmpty() ? tipo : null,
                proveedor != null && !proveedor.isEmpty() ? proveedor : null,
                pageable
        );
    }
    
    @Transactional(readOnly = true)
    public List<Semilla> getAllSemillas() {
        return semillaRepository.findByActivoTrue();
    }
}
