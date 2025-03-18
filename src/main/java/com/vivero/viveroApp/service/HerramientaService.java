
package com.vivero.viveroApp.service;

import com.vivero.viveroApp.Repository.HerramientaRepository;
import com.vivero.viveroApp.model.Herramienta;
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
public class HerramientaService {
    
    private final HerramientaRepository herramientaRepository;

    @Transactional(readOnly = true)
    public Page<Herramienta> getAllHerramientasActivasPaginadas(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return herramientaRepository.findByActivoTrue(pageable);
    }
    
    @Transactional
    public Optional<Herramienta> getHerramientaById(Long id){
        return herramientaRepository.findByIdAndActivoTrue(id);
    }
    
    // Crear una nueva Herramienta
    @Transactional
    public Herramienta createHerramienta(Herramienta herramienta){
        herramienta.setActivo(true);
        for (Proveedor proveedor : herramienta.getProveedores()) {
            proveedor.getProductos().add(herramienta);
        }
        return herramientaRepository.save(herramienta);
    }
    
    @Transactional
    public Herramienta updateHerramienta(Long id, Herramienta herramientaDetails) {
        Herramienta herramienta = herramientaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Herramienta no encontrada con ID: " + id));

        herramienta.setNombre(herramientaDetails.getNombre());
        herramienta.setMarca(herramientaDetails.getMarca());
        herramienta.setPrecio(herramientaDetails.getPrecio());
        herramienta.setStock(herramientaDetails.getStock());
        herramienta.setDescripcion(herramientaDetails.getDescripcion());

        // Actualizar los proveedores
        if (herramientaDetails.getProveedores() != null) {
            for (Proveedor proveedor : herramienta.getProveedores()) {
                proveedor.getProductos().remove(herramienta);
            }
            herramienta.getProveedores().clear();

            for (Proveedor proveedor : herramientaDetails.getProveedores()) {
                herramienta.getProveedores().add(proveedor);
                proveedor.getProductos().add(herramienta);
            }
        } else {
            for (Proveedor proveedor : herramienta.getProveedores()) {
                proveedor.getProductos().remove(herramienta);
            }
            herramienta.getProveedores().clear();
        }

        return herramientaRepository.save(herramienta);
    }

    // Dar de baja una herramienta (marcar como inactiva)
    @Transactional
    public void darDeBajaHerramienta(Long id) {
        Herramienta herramienta = herramientaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Herramienta no encontrada con ID: " + id));
        herramienta.setActivo(false); // Marca la herramienta como inactiva
        herramientaRepository.save(herramienta);
    }

    // Buscar herramientas por nombre, marca y proveedor, y solo traer las activas con paginación
    @Transactional(readOnly = true)
    public Page<Herramienta> buscarHerramientaPaginada(String nombre, String marca, String proveedor, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return herramientaRepository.buscarHerramienta(
                nombre != null && !nombre.isEmpty() ? nombre : null,
                marca != null && !marca.isEmpty() ? marca : null,                
                proveedor != null && !proveedor.isEmpty() ? proveedor : null,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<Herramienta> getAllHerramientas() {
        return herramientaRepository.findByActivoTrue();
    }
}
