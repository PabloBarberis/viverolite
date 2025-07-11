// Variables globales
import BASE_URL from '/js/config.js';

$(document).ready(function () {
    // Llenar selectores de mes y año
    llenarMeses();
    llenarAnios();

    // Cargar usuarios en el modal
    cargarUsuarios();

    // Cargar datos iniciales
    const mesActual = new Date().getMonth() + 1;
    const anioActual = new Date().getFullYear();

    $('#mes').val(mesActual);
    $('#anio').val(anioActual);

    fetchDatos(mesActual, anioActual, "todos");

    // Escuchar cambios
    $('#mes, #anio, #metodoPago').on('change', function () {
        const mes = $('#mes').val();
        const anio = $('#anio').val();
        const metodo = $('#metodoPago').val();
        if (mes && anio) {
            fetchDatos(mes, anio, metodo);
        }
    });

    // En fetchDatos(), justo después de limpiar tablas:
    function fetchDatos(mes, anio, metodo) {
        const url = `/ventas/movimientos?mes=${mes}&anio=${anio}&metodoPago=${metodo}`;

        fetch(url)
            .then(response => {
                if (!response.ok) throw new Error("Error al cargar datos");
                return response.json();
            })
            .then(data => {
                actualizarTituloMetodo(metodo); // <- NUEVO

                actualizarTablaResumen(data.resumen);
                actualizarTablaMovimientos(data.movimientos);
            })
            .catch(error => {
                console.error("Error:", error);
                alert("No se pudieron cargar los datos.");
            });
    }

    // Actualizar tabla resumen
    function actualizarTablaResumen(resumen) {
        const tbody = $('#tablaResumen');
        tbody.empty();

        if (!resumen) return;

        const filas = [
            { periodo: "SALDO MESES ANTERIORES", total: resumen.saldoMesesAnteriores },
            { periodo: "SALDO MES ANTERIOR", total: resumen.saldoMesAnterior },
            { periodo: "1 al 7", total: resumen.ventasPorSemana?.["1 al 7"] ?? 0 },
            { periodo: "8 al 14", total: resumen.ventasPorSemana?.["8 al 14"] ?? 0 },
            { periodo: "15 al 21", total: resumen.ventasPorSemana?.["15 al 21"] ?? 0 },
            { periodo: "22 al 28", total: resumen.ventasPorSemana?.["22 al 28"] ?? 0 },
            { periodo: "29 al fin de mes", total: resumen.ventasPorSemana?.["29 al fin de mes"] ?? 0 },
            { periodo: "TOTAL VENTAS", total: resumen.totalVentas },
            { periodo: "TOTAL INGRESOS", total: resumen.totalIngresos },
            { periodo: "TOTAL EGRESOS", total: -Math.abs(resumen.totalEgresos) },
            { periodo: "TOTAL DEL MES", total: resumen.totalDelMes },
            { periodo: "TOTAL FINAL MES + SALDO ANTERIOR", total: resumen.totalGeneral }
        ];

        filas.forEach(fila => {
            const tr = document.createElement("tr");
            const tdPeriodo = document.createElement("td");
            const tdTotal = document.createElement("td");

            tdPeriodo.textContent = fila.periodo;

            // Formatear número
            let valorFormateado = formatearNumero(fila.total);
            tdTotal.textContent = valorFormateado;

            // Si es negativo, color rojo
            if (fila.total < 0) {
                tdTotal.classList.add('text-danger');
            }

            tr.appendChild(tdPeriodo);
            tr.appendChild(tdTotal);
            tbody.append(tr);
        });
    }

    // Actualizar tabla de movimientos
    function actualizarTablaMovimientos(movimientos) {
        const tbody = $('#tablaMovimientos');
        tbody.empty();

        if (!movimientos || movimientos.length === 0) {
            const tr = document.createElement("tr");
            const td = document.createElement("td");
            td.setAttribute("colspan", "7");
            td.textContent = "No hay movimientos";
            td.classList.add("text-center");
            tr.appendChild(td);
            tbody.append(tr);
            return;
        }

        movimientos.forEach(mov => {
            const fecha = new Date(mov.fecha);
            const dia = fecha.getUTCDate();
            const mes = fecha.getUTCMonth() + 1;
            const anio = fecha.getUTCFullYear();
            const fechaFormateada = `${dia}/${mes}/${anio}`;

            const tr = document.createElement("tr");

            tr.innerHTML = `
            <td>${fechaFormateada}</td>
            <td>${mov.descripcion}</td>
            <td>${mov.usuario.nombre}</td>
            <td>${mov.tipoMovimiento ? 'Ingreso' : 'Egreso'}</td>
            <td>${mov.metodoPago}</td>
            <td>$${parseFloat(mov.monto).toFixed(2)}</td>
            <td>
                <button class="btn btn-warning btn-sm btn-editar" data-id="${mov.id}">Editar</button>
                <button class="btn btn-danger btn-sm btn-eliminar" data-id="${mov.id}">Eliminar</button>
            </td>
        `;
            tbody.append(tr);
        });
    }

    function formatearNumero(valor) {
        const numero = parseFloat(valor).toFixed(2);
        return numero.replace(/\B(?=(\d{3})+(?!\d))/g, ".")
            .replace(/\.(\d\d)$/g, ",$1"); // 1.000.000,00
    }

    function actualizarTituloMetodo(metodo) {
        const titulo = document.getElementById("tituloResumen");
        titulo.className = ""; // Limpiar clases anteriores

        switch (metodo) {
            case "EFECTIVO":
                titulo.textContent = "Resumen: Efectivo";
                titulo.classList.add("efectivo");
                break;
            case "DEBITO":
            case "CREDITO":
                titulo.textContent = "Resumen: Tarjetas";
                titulo.classList.add("tarjeta");
                break;
            case "MERCADOPAGO_VAL":
                titulo.textContent = "Resumen: MercadoPago Valeria";
                titulo.classList.add("mpvale");
                break;
            case "MERCADOPAGO_SAC":
                titulo.textContent = "Resumen: MercadoPago Sacha";
                titulo.classList.add("mpsacha");
                break;
            default:
                titulo.textContent = "Resumen: Todos los métodos";
        }
    }

    // Llenar meses
    function llenarMeses() {
        const meses = [
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ];
        const select = $('#mes');

        for (let i = 0; i < meses.length; i++) {
            select.append(`<option value="${i + 1}">${meses[i]}</option>`);
        }
    }

    // Llenar años
    function llenarAnios() {
        const select = $('#anio');
        const currentYear = new Date().getFullYear();

        for (let year = currentYear - 5; year <= currentYear + 5; year++) {
            select.append(`<option value="${year}">${year}</option>`);
        }
    }

    // Cargar usuarios en modal
    function cargarUsuarios() {
        fetch('/usuarios/listar')
            .then(response => response.json())
            .then(data => {
                const select = $('#usuario');
                select.empty();
                data.forEach(usuario => {
                    select.append(`<option value="${usuario.id}">${usuario.nombre}</option>`);
                });
            });
    }


    // Abrir modal para crear nuevo ingreso/egreso
    $('#btnCrearIngreso').on('click', function () {
        $('#formIngresoEgreso')[0].reset(); // Limpiar formulario
        $('#idIngresoEgreso').val(''); // Limpiar ID
        $('.modal-title').text('Nuevo Ingreso/Egreso');
        $('#modalIngresoEgreso').modal('show');
    });

    // Enviar formulario
    $('#formIngresoEgreso').on('submit', function (e) {
        e.preventDefault();

        const csrfMeta = document.querySelector("meta[name='_csrf']");
        const csrfHeaderMeta = document.querySelector("meta[name='_csrf_header']");

        const id = $('#idIngresoEgreso').val(); // Verificamos si es edición

        const ingresoEgreso = {
            id: id || null,
            descripcion: $('#descripcion').val(),
            metodoPago: $('#metodoPagoModal').val(),
            tipoMovimiento: $('#tipoMovimiento').val() === 'true',
            monto: parseFloat($('#monto').val()),
            usuario: {
                id: parseInt($('#usuario').val())
            },
            fecha: $('#fecha').val(),
            adelanto: $('#esAdelanto').is(':checked')
        };

        const options = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeaderMeta.content]: csrfMeta.content
            },
            body: JSON.stringify(ingresoEgreso)
        };

        const url = id ? '/ingresoegreso/actualizar' : '/ingresoegreso/guardar';

        fetch(url, options)
            .then(response => {
                if (!response.ok) throw new Error('Error al guardar');
                return response.json();
            })
            .then(data => {
                if (data.success === false) {
                    alert("Error: " + data.message);
                    return;
                }

                alert('Guardado con éxito');
                $('#modalIngresoEgreso').modal('hide');

                // Recargar datos
                const mes = $('#mes').val();
                const anio = $('#anio').val();
                const metodo = $('#metodoPago').val();
                fetchDatos(mes, anio, metodo);
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Hubo un error al guardar');
            });
    });

    // Cargar usuarios cuando se abre la página
    cargarUsuarios();

    $(document).on('click', '.btn-eliminar', function () {
        const boton = $(this);
        const id = boton.data('id'); // Obtenemos el data-id

        if (!id) {
            alert("ID no encontrado");
            return;
        }

        if (!confirm("¿Estás seguro de eliminar este movimiento?")) {
            return;
        }

        const csrfMeta = document.querySelector("meta[name='_csrf']");
        const csrfHeaderMeta = document.querySelector("meta[name='_csrf_header']");

        fetch('/ingresoegreso/eliminar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeaderMeta.content]: csrfMeta.content
            },
            body: JSON.stringify({ id: id })
        })
            .then(response => {
                if (!response.ok) throw new Error("Error HTTP");
                return response.json(); // Ahora sí hay JSON
            })
            .then(data => {
                if (data.error) {
                    alert("Error: " + data.error);
                    return;
                }

                alert("✅ Eliminado con éxito");
                const mes = $('#mes').val();
                const anio = $('#anio').val();
                const metodo = $('#metodoPago').val();
                fetchDatos(mes, anio, metodo); // Recargar datos
            })
            .catch(error => {
                console.error("Error:", error);
                alert("Hubo un error al eliminar");
            });
    });

    $(document).on('click', '.btn-editar', function () {
        const id = $(this).data('id');

        // Cargar datos del servidor
        fetch(`/ingresoegreso/editar/${id}`)
            .then(response => {
                if (!response.ok) throw new Error("Error al obtener datos");
                return response.json();
            })
            .then(data => {
                // Rellenar campos del modal
                $('#idIngresoEgreso').val(data.id);
                $('#descripcion').val(data.descripcion);
                $('#monto').val(data.monto);
                $('#metodoPagoModal').val(data.metodoPago);
                $('#tipoMovimiento').val(String (data.tipoMovimiento)); // "true" o "false"
                $('#usuario').val(data.usuarioId);
                $('#esAdelanto').prop('checked', data.adelanto);

                // Convertir fecha desde String ISO a formato "YYYY-MM-DD"
                const fechaLocal = new Date(data.fecha);
                const fechaInput = new Date(
                    fechaLocal.getTime() - fechaLocal.getTimezoneOffset() * 60000
                ).toISOString().split('T')[0];

                $('#fecha').val(fechaInput);

                // Cambiar título del modal
                $('.modal-title').text('Editar Ingreso/Egreso');
                $('#modalIngresoEgreso').modal('show');
            })
            .catch(error => {
                console.error("Error:", error);
                alert("No se pudo cargar el movimiento para editar.");
            });
    });


});