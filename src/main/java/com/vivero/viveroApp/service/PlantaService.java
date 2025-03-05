package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.Planta;
import com.vivero.viveroApp.model.Proveedor;
import com.vivero.viveroApp.model.enums.TipoPlanta;
import com.vivero.viveroApp.repository.PlantaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PlantaService {

    private final PlantaRepository plantaRepository;

    // Obtener todas las plantas activas con paginación
    @Transactional(readOnly = true)
    public Page<Planta> getAllPlantasPaginadas(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return plantaRepository.findByActivoTrue(pageable);
    }

    @Transactional(readOnly = true)
    public List<Planta> getAllPlantasSinPaginacion() {
        return plantaRepository.findByActivoTrue();
    }

    // Buscar plantas por nombre con paginación
    @Transactional(readOnly = true)
    public Page<Planta> buscarPorNombrePaginado(String nombre, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return plantaRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre, pageable);
    }

    // Buscar plantas por tipo con paginación
    @Transactional(readOnly = true)
    public Page<Planta> buscarPorTipoPaginado(TipoPlanta tipo, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return plantaRepository.findByTipoAndActivoTrue(tipo, pageable);
    }

    // Buscar plantas por nombre y tipo con paginación
    @Transactional(readOnly = true)
    public Page<Planta> buscarPorNombreYTipoPaginado(String nombre, TipoPlanta tipo, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return plantaRepository.findByNombreContainingIgnoreCaseAndTipoAndActivoTrue(nombre, tipo, pageable);
    }

    // Obtener planta por ID incluyendo inactivas
    @Transactional(readOnly = true)
    public Optional<Planta> getPlantaByIdIncluyendoInactivas(Long id) {
        return plantaRepository.findById(id);
    }

    // Crear nueva planta
    @Transactional
    public Planta createPlanta(Planta planta) {
        planta.setActivo(true);
        Planta nuevaPlanta = plantaRepository.save(planta);

        // Añadir la planta a la lista de productos de cada proveedor
        for (Proveedor proveedor : planta.getProveedores()) {
            proveedor.getProductos().add(nuevaPlanta);
        }
        return nuevaPlanta;
    }

    @Transactional
    public Planta updatePlanta(Long id, Planta plantaDetails) {
        Planta planta = plantaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planta no encontrada con id: " + id));

        planta.setNombre(plantaDetails.getNombre());
        planta.setTipo(plantaDetails.getTipo());
        planta.setPrecio(plantaDetails.getPrecio());
        planta.setStock(plantaDetails.getStock());
        planta.setDescripcion(plantaDetails.getDescripcion());

        // Actualizar los proveedores
        if (plantaDetails.getProveedores() != null) {
            for (Proveedor proveedor : planta.getProveedores()) {
                proveedor.getProductos().remove(planta);
            }
            planta.getProveedores().clear();

            for (Proveedor proveedor : plantaDetails.getProveedores()) {
                planta.getProveedores().add(proveedor);
                proveedor.getProductos().add(planta);
            }
        } else {
            for (Proveedor proveedor : planta.getProveedores()) {
                proveedor.getProductos().remove(planta);
            }
            planta.getProveedores().clear();
        }
        return plantaRepository.save(planta);
    }

    // Dar de baja planta
    @Transactional
    public void darDeBajaPlanta(Long id) {
        Planta planta = plantaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planta no encontrada con id: " + id));
        planta.setActivo(false);
        plantaRepository.save(planta);
    }

}
