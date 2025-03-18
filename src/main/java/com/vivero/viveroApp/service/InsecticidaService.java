package com.vivero.viveroApp.service;

import com.vivero.viveroApp.Repository.InsecticidaRepository;
import com.vivero.viveroApp.model.Insecticida;
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
public class InsecticidaService {

    private final InsecticidaRepository insecticidaRepository;

    @Transactional(readOnly = true)
    public Page<Insecticida> getAllInsecticidasActivosPaginados(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return insecticidaRepository.findByActivoTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Insecticida> getInsecticidaById(Long id) {
        return insecticidaRepository.findByIdAndActivoTrue(id);
    }

    @Transactional
    public Insecticida createInsecticida(Insecticida insecticida) {
        insecticida.setActivo(true);
        for (Proveedor proveedor : insecticida.getProveedores()) {
            proveedor.getProductos().add(insecticida);
        }
        return insecticidaRepository.save(insecticida);
    }

    @Transactional
    public Insecticida updateInsecticida(Long id, Insecticida insecticidaDetails) {
        Insecticida insecticida = insecticidaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Insecticida no encontrado con ID: " + id));

        insecticida.setNombre(insecticidaDetails.getNombre());
        insecticida.setMarca(insecticidaDetails.getMarca());
        insecticida.setPrecio(insecticidaDetails.getPrecio());
        insecticida.setStock(insecticidaDetails.getStock());
        insecticida.setDescripcion(insecticidaDetails.getDescripcion());

        // Actualizar los proveedores
        if (insecticidaDetails.getProveedores() != null) {
            for (Proveedor proveedor : insecticida.getProveedores()) {
                proveedor.getProductos().remove(insecticida);
            }
            insecticida.getProveedores().clear();

            for (Proveedor proveedor : insecticidaDetails.getProveedores()) {
                insecticida.getProveedores().add(proveedor);
                proveedor.getProductos().add(insecticida);
            }
        } else {
            for (Proveedor proveedor : insecticida.getProveedores()) {
                proveedor.getProductos().remove(insecticida);
            }
            insecticida.getProveedores().clear();
        }

        return insecticidaRepository.save(insecticida);
    }

    @Transactional
    public void darDeBajaInsecticida(Long id) {
        Insecticida insecticida = insecticidaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Insecticida no encontrado con ID: " + id));
        insecticida.setActivo(false);
        insecticidaRepository.save(insecticida);
    }

    @Transactional(readOnly = true)
    public Page<Insecticida> buscarInsecticidaPaginado(String nombre, String marca, String proveedor, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return insecticidaRepository.buscarInsecticida(
                nombre != null && !nombre.isEmpty() ? nombre : null,
                marca != null && !marca.isEmpty() ? marca : null,
                proveedor != null && !proveedor.isEmpty() ? proveedor : null,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<Insecticida> getAllInsecticidas() {
        return insecticidaRepository.findByActivoTrue();
    }
}
