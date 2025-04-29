package com.vivero.viveroApp.service;

import com.vivero.viveroApp.Repository.FertilizanteRepository;
import com.vivero.viveroApp.model.Fertilizante;
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
public class FertilizanteService {
    
    private final FertilizanteRepository fertilizanteRepository;

  @Transactional(readOnly = true)
    public Page<Fertilizante> getAllFertilizantesActivasPaginadas(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return fertilizanteRepository.findByActivoTrue(pageable);
    }
    
    @Transactional
    public Optional<Fertilizante> getFertilizanteById(Long id){
        return fertilizanteRepository.findByIdAndActivoTrue(id);
    }
    
    // Crear un nuevo Fertilizante
    @Transactional
    public Fertilizante createFertilizante(Fertilizante fertilizante){
        fertilizante.setActivo(true);
        for (Proveedor proveedor : fertilizante.getProveedores()) {
            proveedor.getProductos().add(fertilizante);
        }
        return fertilizanteRepository.save(fertilizante);
    }
    
       @Transactional
    public Fertilizante updateFertilizante(Long id, Fertilizante fertilizanteDetails) {
        Fertilizante fertilizante = fertilizanteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Fertilizante no encontrado con ID: " + id));

        fertilizante.setNombre(fertilizanteDetails.getNombre());
        fertilizante.setMarca(fertilizanteDetails.getMarca());
        fertilizante.setPrecio(fertilizanteDetails.getPrecio());
        fertilizante.setStock(fertilizanteDetails.getStock());
        fertilizante.setDescripcion(fertilizanteDetails.getDescripcion());

        // Actualizar los proveedores
        if (fertilizanteDetails.getProveedores() != null) {
            for (Proveedor proveedor : fertilizante.getProveedores()) {
                proveedor.getProductos().remove(fertilizante);
            }
            fertilizante.getProveedores().clear();

            for (Proveedor proveedor : fertilizanteDetails.getProveedores()) {
                fertilizante.getProveedores().add(proveedor);
                proveedor.getProductos().add(fertilizante);
            }
        } else {
            for (Proveedor proveedor : fertilizante.getProveedores()) {
                proveedor.getProductos().remove(fertilizante);
            }
            fertilizante.getProveedores().clear();
        }

        return fertilizanteRepository.save(fertilizante);
    }

    // Dar de baja un fertilizante (marcar como inactivo)
    @Transactional
    public void darDeBajaFertilizante(Long id) {
        Fertilizante fertilizante = fertilizanteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Fertilizante no encontrado con ID: " + id));
        fertilizante.setActivo(false); // Marca el fertilizante como inactivo
        fertilizanteRepository.save(fertilizante);
    }

    // Buscar fertilizantes por nombre, marca, tipo y proveedor, y solo traer los activos con paginación
    @Transactional(readOnly = true)
    public Page<Fertilizante> buscarFertilizantePaginado(String nombre, String marca, String proveedor, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return fertilizanteRepository.buscarFertilizante(
                nombre != null && !nombre.isEmpty() ? nombre : null,
                marca != null && !marca.isEmpty() ? marca : null,                
                proveedor != null && !proveedor.isEmpty() ? proveedor : null,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<Fertilizante> getAllFertilizantes() {
        return fertilizanteRepository.findByActivoTrue();
    }
}
