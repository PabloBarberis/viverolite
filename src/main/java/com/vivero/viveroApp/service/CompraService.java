package com.vivero.viveroApp.service;

import com.vivero.viveroApp.Repository.CompraRepository;
import com.vivero.viveroApp.dto.CompraDTO;
import com.vivero.viveroApp.model.Compra;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CompraService {

    private final CompraRepository compraRepository;

    public Page<CompraDTO> obtenerComprasPaginadas(Pageable pageable) {
        // El Pageable ya incluirá la ordenación (por ejemplo: fecha DESC)
        Page<Compra> comprasPage = compraRepository.findAll(pageable);

        List<CompraDTO> dtoList = comprasPage.getContent().stream()
                .map(this::convertirACompraDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, comprasPage.getTotalElements());
    }

    private CompraDTO convertirACompraDTO(Compra compra) {
        CompraDTO dto = new CompraDTO();
        dto.setId(compra.getId());
        dto.setFecha(compra.getFecha());
        dto.setComentario(compra.getComentario());
        // dto.setProductos(null); // opcional, según tu DTO
        return dto;
    }
}
