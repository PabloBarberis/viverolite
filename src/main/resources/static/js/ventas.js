// ============================
// CONFIGURACIÓN Y CONSTANTES
// ============================

import BASE_URL from '/js/config.js';

// ============================
// DOCUMENT READY (INICIALIZACIÓN)
// ============================

document.addEventListener("DOMContentLoaded", function () {
    const pathname = window.location.pathname; // Ruta de la página actual

    // ===========================
    // CÓDIGO PARA VENTAS/LISTAR
    // ===========================

    if (pathname === "/ventas/listar") {
        console.log("Página: Listar Ventas");

        // Elemento #fecha
        const fechaInput = document.getElementById("fecha");
        if (!fechaInput) {
            console.warn("El elemento #fecha no existe en esta página. Código no ejecutado.");
            return;
        }

        // Cargar la fecha actual al abrir la página
        let hoy = new Date().toISOString().split("T")[0];
        fechaInput.value = hoy;

        // Llamar a la función para obtener las ventas al cargar la página
        obtenerVentas(hoy);

        // Evento para actualizar las ventas al cambiar la fecha
        fechaInput.addEventListener("change", function () {
            obtenerVentas(this.value);
        });

        // Función para obtener ventas según la fecha seleccionada
        function obtenerVentas(fecha) {
            console.log(`Obteniendo ventas para la fecha: ${fecha}`);
            fetch(`${BASE_URL}/ventas/fechas?fecha=${fecha}`)
                    .then(response => response.json())
                    .then(data => {
                        console.log("Datos recibidos:", data);
                        document.getElementById("totalEfectivo").textContent = `$ ${(data.totalEfectivo || 0).toFixed(2)}`;
                        document.getElementById("totalCredito").textContent = `$ ${(data.totalCredito || 0).toFixed(2)}`;
                        document.getElementById("totalDebito").textContent = `$ ${(data.totalDebito || 0).toFixed(2)}`;
                        document.getElementById("totalMercadoPago").textContent = `$ ${(data.totalMercadoPago || 0).toFixed(2)}`;
                        document.getElementById("totalGeneral").textContent = `$ ${(data.totalGeneral || 0).toFixed(2)}`;
                    })
                    .catch(error => console.error("Error al obtener las ventas:", error));
        }

    }

    // ===========================
    // CÓDIGO PARA VENTAS/CREAR Y VENTAS/EDITAR
    // ===========================
    if (pathname === "/ventas/crear" || pathname.startsWith("/ventas/editar")) {
        console.log("Página: Crear o Editar Venta");

        // Inicializar Select2 para los dropdowns de cliente y producto
        if (typeof $.fn.select2 !== 'undefined') {
            $('#cliente').select2({
                placeholder: 'Seleccionar Cliente...',
                allowClear: true,
                width: '50%',
                dropdownCssClass: 'select2-dropdown'
            });

            $('#producto').select2({
                placeholder: 'Buscar Producto...',
                allowClear: true,
                width: '50%',
                dropdownCssClass: 'select2-dropdown'
            });
        } else {
            console.error("Select2 no está disponible.");
        }

        // === SOLUCIÓN: Reasignar Eventos a Productos Precargados ===
        // Al cargar la página, asegúrate de que todos los botones "Eliminar" ya existentes
        // también estén vinculados al evento de eliminación.
        $('#productosSeleccionados .eliminarProducto').each(function () {
            const boton = $(this);
            boton.off('click').on('click', function () {
                eliminarProducto($(this));
            });
        });

        // Delegar el evento "click" para eliminar productos (dinámicos o precargados)
        $('#productosSeleccionados').on('click', '.eliminarProducto', function () {
            eliminarProducto($(this));
        });

        // Función para manejar la eliminación de productos
        function eliminarProducto(boton) {
            const filas = $('#productosSeleccionados tr');

            if (filas.length === 1) {
                alert('No puede eliminar el último producto.');
                return false;
            }

            boton.closest('tr').remove();
            actualizarTotales();
        }

        // Evento para agregar productos a la lista
        $('#agregarProducto').click(function () {
            const productoId = $('#producto').val();
            const productoNombre = $('#producto option:selected').text();
            const precio = $('#producto option:selected').data('precio');
            const stock = $('#producto option:selected').data('stock');

            if (productoId) {
                if ($('#productosSeleccionados input[name="productoIds[]"][value="' + productoId + '"]').length > 0) {
                    alert('Este producto ya está en la lista.');
                    return;
                }

                if (stock <= 0) {
                    alert('Este producto no tiene stock disponible.');
                    return;
                }

                // Agregar fila a la tabla
                $('#productosSeleccionados').append(`
                <tr>
                    <td>${productoNombre}</td>
                    <td class="precio">${precio}</td>
                    <td>${stock}</td>
                    <td><input type="number" name="cantidades[]" class="form-control cantidad" min="1" max="${stock}" value="1"></td>
                    <td><button type="button" class="btn btn-danger eliminarProducto">Eliminar</button></td>
                    <input type="hidden" name="productoIds[]" value="${productoId}">
                </tr>
            `);

                actualizarTotales();
            }
        });

        // Evento para actualizar totales al cambiar cantidad
        $('#productosSeleccionados').on('change', '.cantidad', function () {
            const cantidad = $(this).val();
            const stock = $(this).closest('tr').find('td').eq(2).text();

            if (parseInt(cantidad) > parseInt(stock)) {
                alert('La cantidad no puede ser mayor que el stock disponible.');
                $(this).val(stock); // Restablecer al stock máximo
            }

            actualizarTotales();
        });

        // Evento para recalcular totales al cambiar descuento o método de pago
        $('#metodoPago, #descuento').change(function () {
            actualizarTotales();
        });

        // Validación del formulario al enviarlo
        $('#ventaForm').submit(function (event) {
            if ($('#productosSeleccionados tr').length === 0) {
                alert('Debe agregar al menos un producto antes de crear la venta.');
                event.preventDefault(); // Evitar el envío del formulario
            }
        });

        // Llamar a actualizarTotales al cargar la página para mostrar valores iniciales
        actualizarTotales();

        // Función para actualizar los totales en la tabla
        function actualizarTotales() {
            let totalLista = 0;
            let totalMetodoPago = 0;
            let totalFinal = 0;
            const metodoPago = $('#metodoPago').val();
            const descuentoPorcentaje = parseFloat($('#descuento').val()) || 0;

            // Calcular totales
            $('#productosSeleccionados tr').each(function () {
                const precio = parseFloat($(this).find('.precio').text());
                const cantidad = parseInt($(this).find('.cantidad').val());
                totalLista += precio * cantidad;
            });

            if (metodoPago === 'CREDITO') {
                totalMetodoPago = totalLista * 1.15; // 15% extra
            } else {
                totalMetodoPago = totalLista;
            }

            totalFinal = totalMetodoPago - (totalMetodoPago * (descuentoPorcentaje / 100));

            // Actualizar los campos de total en el DOM
            $('#totalLista').val(totalLista.toFixed(2));
            $('#totalMetodoPago').val(totalMetodoPago.toFixed(2));
            $('#totalFinal').val(totalFinal.toFixed(2));
        }
    }

});
