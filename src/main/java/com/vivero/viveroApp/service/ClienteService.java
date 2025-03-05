package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.Cliente;
import com.vivero.viveroApp.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    // Obtener todos los clientes activos
    @Transactional(readOnly = true)
    public List<Cliente> getAllClientesActivos() {
        return clienteRepository.findByActivoTrue(); // Solo obtiene clientes activos
    }

    // Obtener todos los clientes (activos e inactivos)
    @Transactional(readOnly = true)
    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll(); // Obtiene todos los clientes, sin importar si están activos o no
    }

    // Obtener un cliente por ID si está activo
    @Transactional(readOnly = true)
    public Optional<Cliente> getClienteById(Long id) {
        return clienteRepository.findByIdAndActivoTrue(id); // Solo busca clientes activos
    }

    // Crear un nuevo cliente
    @Transactional
    public Cliente createCliente(Cliente cliente) {
        cliente.setActivo(true); // Se asegura que el cliente nuevo esté activo
        return clienteRepository.save(cliente);
    }

    // Actualizar un cliente existente
    @Transactional
    public Cliente updateCliente(Long id, Cliente clienteDetails) {
        Optional<Cliente> optionalCliente = clienteRepository.findByIdAndActivoTrue(id);

        if (optionalCliente.isPresent()) {
            Cliente cliente = optionalCliente.get();
            cliente.setNombre(clienteDetails.getNombre());
            cliente.setDireccion(clienteDetails.getDireccion());
            cliente.setEmail(clienteDetails.getEmail());
            cliente.setTelefono(clienteDetails.getTelefono());
            // Actualiza cualquier otro atributo necesario
            return clienteRepository.save(cliente);
        } else {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }
    }

    // Dar de baja a un cliente (marcar como inactivo)
    @Transactional
    public void darDeBajaCliente(Long id) {
        Optional<Cliente> optionalCliente = clienteRepository.findByIdAndActivoTrue(id);

        if (optionalCliente.isPresent()) {
            Cliente cliente = optionalCliente.get();
            cliente.setActivo(false); // Marca al cliente como inactivo
            clienteRepository.save(cliente);
        } else {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }
    }
}
