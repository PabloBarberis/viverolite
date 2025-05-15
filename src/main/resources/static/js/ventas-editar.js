import BASE_URL from '/js/config.js';



document.addEventListener("DOMContentLoaded", function () {
    const pathname = window.location.pathname; // Ruta de la página actual

    //Se asignan y limpian las tablas    

    const tablaProductos = document.getElementById("productosSeleccionados");
    tablaProductos.innerHTML = ""; // 🔥 Limpiar la tabla antes de insertar datos

    const tablaMetodosPago = document.getElementById("metodosPagoSeleccionados");
    tablaMetodosPago.innerHTML = ""; // 🔥 Limpiar la tabla antes de insertar datos

    //Se Asignan los valores de Venta, Productos y Pagos
    const venta = window.venta || {};
    const productos = Array.isArray(window.productos) ? window.productos : [];
    const pagos = Array.isArray(window.pagos) ? window.pagos : [];
    let totalProductos = 0;
    let totalMontos = 0;
    let totalParaMetodoPago = 0;

    // Inicializar Select2 para los dropdowns de cliente y producto


    $(document).ready(function () {
        $('#producto').select2({
            placeholder: "Buscar producto...",
            allowClear: true,
            minimumInputLength: 2,
            language: {
                inputTooShort: function () {
                    return 'Ingresá al menos 2 caracteres';
                },
                noResults: function () {
                    return 'No se encontraron productos';
                },
                searching: function () {
                    return 'Buscando...';
                }
            },
            ajax: {
                url: '/api/productos',
                dataType: 'json',
                delay: 250,
                data: function (params) {
                    return {
                        q: params.term || ''
                    };
                },
                processResults: function (data) {
                    return {
                        results: data.map(function (producto) {
                            return {
                                id: producto.id,
                                text: producto.nombre,
                                precio: producto.precio,
                                stock: producto.stock
                            };
                        })
                    };
                },
                cache: true
            }
        });

        // 🧠 Auto-foco en campo de búsqueda cuando se abre el dropdown
        $('#producto').on('select2:open', function () {
            setTimeout(() => {
                document.querySelector('.select2-container--open .select2-search__field').focus();
            }, 100);
        });
    });


    //Se llenan las tablas

    //LLenado tabla Productos
    productos.forEach(productoVenta => {
        const nuevoRow = document.createElement("tr");
        nuevoRow.dataset.productoId = productoVenta.productoId;
        nuevoRow.innerHTML = `
            <td>${productoVenta.producto.nombre}</td>
            <td class="precio">${productoVenta.precioOriginal}</td>        
            <td>${productoVenta.producto.stock + productoVenta.cantidad}</td>
            <td><input type="number" class="cantidad" value="${productoVenta.cantidad}" min="1" max="${productoVenta.stock}"></td>
            <td><input type="number" class="descuento" value="${productoVenta.porcentajeDescuento}" min="0" max="100"></td>
            <td><span class="precio-final">${productoVenta.subtotal}</span></td>
            <td><button type="button" class="btn btn-danger eliminarProducto">Eliminar</button></td>
            <input type="hidden" name="productoIds[]" value="${productoVenta.producto.id}">`;

        tablaProductos.appendChild(nuevoRow);
        recalcularPrecioFinal($(nuevoRow)); // Mostrar precio final correcto apenas se añade

    });

    //LLenado tabla Metodos Pago
    pagos.forEach(pago => {
        const monto = Number(pago.monto);

        const montoFinal = pago.metodo === "CREDITO" ? monto * 1.15 : monto;
        totalMontos += monto;
        const nuevoRow = document.createElement("tr");
        nuevoRow.innerHTML = `
            <td>${pago.metodo}</td>
            <td><input type="number" class="montoPago" value="${monto}" min="0"></td>
            <td><span class="montoTotalPago">${montoFinal.toFixed(2)}</span></td>
            <td><button type="button" class="btn btn-danger eliminarMetodoPago">Eliminar</button></td>
            <input type="hidden" name="metodos[]" value="${pago.metodo}">`;

        tablaMetodosPago.appendChild(nuevoRow);
    });

    //  actualizarTotales();

    //INICIO LOGICA DE AGREGAR PRODUCTOS Y METODOS

    //Logica para agregar Productos a la fila
    $('#agregarProducto').click(function () {
        const producto = $('#producto').select2('data')[0]; // Obtener el primer (y único) elemento seleccionado

        const productoId = producto.id;
        const productoNombre = producto.text;
        const precio = producto.precio;
        const stock = producto.stock;

        if (productoId) {
            // Verificar si el producto ya está en la lista
            if ($('#productosSeleccionados input[name="productoIds[]"][value="' + productoId + '"]').length > 0) {
                alert('Este producto ya está en la lista.');
                return;
            }

            // Verificar si hay stock disponible
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
                    <td>
                        <input type="number" name="cantidades[]" class="form-control cantidad" min="1" max="${stock}" value="1">
                    </td>
                    <td>
                        <input type="number" name="descuentos[]" class="form-control descuento" min="0" max="100" value="0">
                    </td>
                    <td>
                        <span class="precio-final">${precio}</span>
                        <input type="hidden" name="preciosFinales[]" class="precioFinalInput" value="${precio}">
                    </td>
                    <td>
                        <button type="button" class="btn btn-danger eliminarProducto">Eliminar</button>
                    </td>
                    <input type="hidden" name="productoIds[]" value="${productoId}">
                </tr>
            `);
        }

        // Seleccionar la última fila añadida
        const nuevaFila = $('#productosSeleccionados tr').last();

        // Calcular precio final para esa fila
        recalcularPrecioFinal(nuevaFila);

    });


    //Logica para agregar metodos a la fila

    // Maneja el evento para agregar un nuevo método de pago
    document.getElementById("agregarMetodoPago").addEventListener("click", function () {
        const metodoPago = document.getElementById("metodoPago").value;
        if (!metodoPago) {
            alert("Por favor, selecciona un método de pago.");
            return;
        }

        const yaExiste = document.querySelector(`#metodosPagoSeleccionados input[name='metodos[]'][value='${metodoPago}']`);
        if (yaExiste) {
            alert("Este método de pago ya ha sido agregado.");
            return;
        }

        // Calcular el monto pendiente
        const totalFinal = Math.round(totalParaMetodoPago / 100) * 100;
        let totalAsignado = 0;

        document.querySelectorAll(".montoPago").forEach(input => {
            totalAsignado += parseFloat(input.value) || 0;
        });

        let montoPendiente = totalFinal - totalAsignado;
        if (document.querySelectorAll("#metodosPagoSeleccionados tr").length === 0) {
            montoPendiente = totalFinal;
        }
        if (montoPendiente < 0) montoPendiente = 0;

        // Crear nueva fila de forma segura
        const nuevaFila = document.createElement("tr");
        const montoFinal = metodoPago === "CREDITO" ? montoPendiente * 1.15 : montoPendiente;

        nuevaFila.innerHTML = `
        <td>${metodoPago}</td>
        <td>
            <input type="number" name="montos[]" class="form-control montoPago" value="${montoPendiente}" min="0">
        </td>
        <td>
            <span class="montoTotalPago">${montoFinal.toFixed(2)}</span>
        </td>
        <td>
            <button type="button" class="btn btn-danger eliminarMetodoPago">Eliminar</button>
        </td>
        <input type="hidden" name="metodos[]" value="${metodoPago}">
    `;

        document.getElementById("metodosPagoSeleccionados").appendChild(nuevaFila);
        actualizarTotalesGlobales();
    });


    //FIN LOGICA DE AGREGAR PRODUCTOS Y METODOS



    //INICIO LOGICA PARA ELIMINAR PRODUCTOS Y METODOS

    $('#productosSeleccionados').on('click', '.eliminarProducto', function () {
        eliminarProducto($(this));
    });

    //elimina el producto y recalcula la suma de los productos
    function eliminarProducto(boton) {
        const filas = $('#productosSeleccionados tr');

        if (filas.length === 1) {
            alert('No puede eliminar el último producto.');
            return false;
        }

        boton.closest('tr').remove();
        recalcularTotalFinal();

    }

    $('#metodosPagoSeleccionados').on('click', '.eliminarMetodoPago', function () {
        const filas = $('#metodosPagoSeleccionados tr');

        $(this).closest('tr').remove();
        actualizarTotalesGlobales();

    });

    //FIN LOGICA PARA ELIMINAR PRODUCTOS Y METODOS


    //INICIO LOGICA PARA RECALCULAR PRECIOS


    //recalcula el precio de cada fila de producto
    function recalcularPrecioFinal(fila) {
        const precioOriginal = parseFloat(fila.find('.precio').text());
        const cantidad = parseInt(fila.find('.cantidad').val());
        const descuento = parseFloat(fila.find('.descuento').val());

        const precioConDescuento = precioOriginal * cantidad * (1 - descuento / 100);
        fila.find('.precio-final').text(precioConDescuento.toFixed(2));

        recalcularTotalFinal();
    }

    function recalcularTotalFinal() {
        let totalReal = 0;
        $('#productosSeleccionados .precio-final').each(function () {
            totalReal += parseFloat($(this).text());
        });

        let totalRedondeado = Math.round(totalReal / 100) * 100;

        // Mostrar el total redondeado y el real en el footer
        $('#totalProductosFinal').text(`${totalRedondeado} (${totalReal.toFixed(2)})`);

        // Guardar los valores para otras funciones
        totalProductos = totalRedondeado;
        totalParaMetodoPago = totalProductos;
    }

    //recalcula todo al agregar un nuevo producto
    $('#productosSeleccionados').on('input', '.cantidad, .descuento', function () {
        const fila = $(this).closest('tr');
        recalcularPrecioFinal(fila);
    });

    //FIN LOGICA PARA RECALCULAR PRECIOS PRODUCTOS

    //INICIO LOGICA PARA RECALCULAR PRECIOS METODOS

    // Cada vez que se modifica un monto en un método de pago, recalcula el total (con o sin recargo)
    $('#metodosPagoSeleccionados').on('input', '.montoPago', function () {
        const fila = $(this).closest('tr');
        recalcularMontoPago(fila);
    });



    // Recalcula el total de un método de pago, aplicando un 15% de recargo si el método es CREDITO
    function recalcularMontoPago(fila) {
        const metodo = fila.find('input[name="metodos[]"]').val();
        const monto = parseFloat(fila.find('.montoPago').val()) || 0;

        const montoFinal = metodo === 'CREDITO' ? monto * 1.15 : monto;
        fila.find('.montoTotalPago').text(montoFinal.toFixed(2));

        // Si usás un total general de métodos de pago, podés llamarlo acá
        // recalcularTotalPagos();
    }


    //MOSTRAR TOTALES AL PIE DE PAGINA

    function actualizarTotalesGlobales() {
        // Total productos (asumimos que ya lo calculaste antes)

        document.getElementById("totalProductos").value = totalProductos;

        // Total métodos de pago (sumando monto con recargo)
        let totalPago = 0;
        document.querySelectorAll(".montoTotalPago").forEach(span => {
            totalPago += parseFloat(span.textContent) || 0;
        });
        document.getElementById("totalMetodoPago").value = totalPago.toFixed(2);

        // Descuento global
        //const descuento = parseFloat(document.getElementById("descuentoGlobal").value) || 0;
        //const totalConDescuento = totalPago * (1 - (descuento / 100));
        //document.getElementById("totalFinal").value = totalConDescuento.toFixed(2);
    }

    actualizarTotalesGlobales();

    // Al cambiar el descuento global
    //document.getElementById("descuentoGlobal").addEventListener("input", actualizarTotalesGlobales);

    // Delegación para detectar cambios en montos
    document.getElementById("metodosPagoSeleccionados").addEventListener("input", function (e) {
        if (e.target.classList.contains("montoPago")) {
            const fila = e.target.closest("tr");
            const metodo = fila.querySelector("input[name='metodos[]']").value;
            const nuevoMonto = parseFloat(e.target.value) || 0;

            const nuevoTotal = metodo === "CREDITO" ? nuevoMonto * 1.15 : nuevoMonto;
            fila.querySelector(".montoTotalPago").textContent = nuevoTotal.toFixed(2);

            actualizarTotalesGlobales();
        }
    });




    function validarAntesDeEnviar() {
        let cantidadMetodos = 0;


        document.querySelectorAll(".montoPago").forEach(input => {
            cantidadMetodos++;
        });

        if (cantidadMetodos === 0) {
            alert("Debe agregar al menos un método de pago.");
            return false;
        }

        if (totalMontos.toFixed(2) !== totalProductos.toFixed(2)) {
            alert("Revise los montos en los métodos de pago. Deben coincidir con el total de productos.");
            return false;
        }

        return true;
    }




    //Envio de datos

    function enviarVentaEditada(event) {

        event.preventDefault();
        // 🔥 Obtener el CSRF token y el nombre del header desde el HTML
        const csrfMeta = document.querySelector("meta[name='_csrf']");
        const csrfHeaderMeta = document.querySelector("meta[name='_csrf_header']");

        if (!csrfMeta || !csrfHeaderMeta) {
            console.error("CSRF meta tags no encontrados, revisa la configuración de seguridad.");
            return; // 🔥 Evita que `fetch` falle
        }

        const csrfToken = csrfMeta.getAttribute("content");
        const csrfHeader = csrfHeaderMeta.getAttribute("content");

        // 🔥 Obtener la fecha y hora
        const fecha = document.getElementById("fecha").value;
        const hora = document.getElementById("hora").value;
        const id = document.getElementById("ventaId").value;

        const clienteId = document.getElementById("cliente").value;
        const productos = [];
        const pagos = [];
        //const descuento = parseFloat(document.getElementById("descuentoGlobal").value) || 0;


        // 🔥 Recopilar los productos seleccionados
        document.querySelectorAll("#productosSeleccionados tr").forEach(row => {
            const productoId = row.querySelector("input[name='productoIds[]']").value;
            const cantidad = parseInt(row.querySelector(".cantidad").value);
            const descuentoProducto = parseFloat(row.querySelector(".descuento").value) || 0;
            const precioUnitario = parseFloat(row.querySelector(".precio").textContent);

            productos.push({ productoId, cantidad, descuentoProducto, precioUnitario });
        });

        // 🔥 Recopilar métodos de pago
        document.querySelectorAll("#metodosPagoSeleccionados tr").forEach(row => {
            const metodo = row.querySelector("input[name='metodos[]']").value;
            const monto = parseFloat(row.querySelector(".montoPago").value);

            pagos.push({ metodo, monto });

        });

        const ventaDTO = { clienteId, productos, pagos, fecha, hora, id };

        // 🚨 Validar que haya al menos un método de pago
        if (pagos.length === 0) {
            alert("Debes agregar al menos un método de pago.");
            return; // 🔥 Cancelar el envío
        }

        for (const pago of pagos) {
            if (!pago.metodo || isNaN(pago.monto) || pago.monto <= 0) {
                alert("Verifica que cada método de pago tenga un monto válido.");
                return;
            }
        }


        if (totalMontos.toFixed(2) != totalProductos.toFixed(2)) {
            alert("El total de los métodos de pago no coincide con el total de los productos.");
            return; // 🛑 Detiene el flujo de la función y evita el envío
        }


        // 🔥 Enviar la venta editada al backend con el token CSRF correcto
        fetch("/ventas/actualizar", {
            method: "POST",
            headers: {
                "Content-Type": "application/json", // 👈 Asegúrate de incluir esta línea
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(ventaDTO)
        })
            .then(response => response.json()) // 🔥 Ahora la respuesta sí será JSON
            .then(data => {
                alert(data.message);
                window.location.href = "/ventas/listar";
            })
            .catch(error => {
                console.error("Error al enviar la venta:", error);
                alert(`Hubo un problema al procesar la venta: ${error.message}`);
            });
    }

    // 🔥 Llamar a `enviarVentaEditada()` cuando el usuario envíe el formulario
    document.getElementById("ventaForm").addEventListener("submit", function (event) {

        totalMontos = 0;
        document.querySelectorAll("#metodosPagoSeleccionados tr").forEach(row => {
            const monto = parseFloat(row.querySelector(".montoPago").value);
            totalMontos += monto;
        });
        event.preventDefault(); // Evita la recarga de la página
        enviarVentaEditada(event);
    });


});
