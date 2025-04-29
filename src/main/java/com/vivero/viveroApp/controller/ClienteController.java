package com.vivero.viveroApp.controller;

import com.vivero.viveroApp.model.Cliente;
import com.vivero.viveroApp.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    // Obtener todos los clientes activos para mostrar en la vista
    @GetMapping("/listar")
    public String vistaClientesActivos(Model model) {
        List<Cliente> clientes = clienteService.getAllClientesActivos();
        model.addAttribute("clientes", clientes);
        return "clientes/listar-cliente"; // Vista para listar clientes activos
    }

    // Crear un nuevo cliente (formulario)
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/crear-cliente"; // Vista para crear cliente
    }

    // Crear un nuevo cliente desde el formulario
    @PostMapping
    public String crearClienteDesdeVista(@ModelAttribute Cliente cliente) {
        clienteService.createCliente(cliente);
        return "redirect:/clientes/listar"; // Redirige a la lista de clientes activos
    }

    // Actualizar un cliente existente (formulario)
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Cliente> clienteOpt = clienteService.getClienteById(id);
        if (clienteOpt.isPresent()) {
            model.addAttribute("cliente", clienteOpt.get());
            return "clientes/editar-cliente"; // Vista para editar cliente
        } else {
            return "redirect:/clientes/listar"; // Redirige si no se encuentra el cliente
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizarClienteDesdeVista(@PathVariable Long id, @ModelAttribute Cliente clienteDetails) {
        clienteService.updateCliente(id, clienteDetails);
        return "redirect:/clientes/listar"; // Redirige a la lista de clientes activos
    }

    // Dar de baja a un cliente desde la vista
    @PostMapping("/dar-de-baja")
    public String darDeBajaClienteDesdeVista(@RequestParam("clienteSeleccionado") Long id) {
        clienteService.darDeBajaCliente(id);
        return "redirect:/clientes/listar"; // Redirige a la lista de clientes activos
    }

}
