const BASE_URL = "http://localhost:8080";
//const BASE_URL = "http://149.50.139.89:8080";




///////////////////////////////////////-HORAS-////////////////////////////////////



document.addEventListener('DOMContentLoaded', function () {
    var selectUsuario = document.getElementById('usuario');
    selectUsuario.addEventListener('change', cargarUsuario);

    // Cargar el mes y año corrientes por defecto
    var currentDate = new Date();
    document.getElementById('mes').value = currentDate.getMonth() + 1;
    document.getElementById('año').value = currentDate.getFullYear();

    // Configurar la fecha del día actual en el campo de fecha de adelantos
    var fechaAdelantoInput = document.getElementById('fechaAdelanto');
    if (fechaAdelantoInput) {
        var today = new Date();
        var day = String(today.getDate()).padStart(2, '0');
        var month = String(today.getMonth() + 1).padStart(2, '0'); // Enero es 0
        var year = today.getFullYear();
        var todayString = year + '-' + month + '-' + day;
        fechaAdelantoInput.value = todayString;
    }
});


function cargarUsuario() {

    var usuarioId = document.getElementById('usuario').value;
    var configuracion = document.getElementById('configuracion');
    configuracion.style.display = 'block';

    // Evitar llamadas duplicadas
    if (usuarioId !== configuracion.dataset.lastUserId) {
        configuracion.dataset.lastUserId = usuarioId;

        // Limpiar la tabla antes de cargar nuevos datos
        var tablaRegistros = document.getElementById('tablaRegistros');
        tablaRegistros.innerHTML = '';

        saludarUsuario(usuarioId);
    }
}


function saludarUsuario(usuarioId) {
    var usuario = document.querySelector('#usuario option[value="' + usuarioId + '"]').text;
    document.getElementById('saludo').innerText = 'Hola ' + usuario + '!';
    cargarDias(); // Solo llamamos a cargarDias aquí    
}


function cargarDias() {
    var usuarioId = document.getElementById('usuario').value;
    var mes = document.getElementById('mes').value;
    var año = document.getElementById('año').value;
    cargarDiasParaUsuario(usuarioId, mes, año);
    cargarAdelantosParaUsuario(usuarioId, mes, año); // Llama a cargarAdelantosParaUsuario para cargar los adelantos del mes y año seleccionados
    obtenerTotales();
}


function cargarDiasParaUsuario(usuarioId, mes, año) {

    var tablaRegistros = document.getElementById('tablaRegistros');
    tablaRegistros.innerHTML = ''; // Limpiar tabla antes de cargar nuevos datos

    fetch(`${BASE_URL}/horas/cargarDias?usuarioId=${usuarioId}&mes=${mes}&año=${año}`)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    var diasEnMes = new Date(año, mes, 0).getDate();
                    for (var dia = 1; dia <= diasEnMes; dia++) {
                        var fecha = new Date(año, mes - 1, dia);
                        var diaSemana = fecha.toLocaleDateString('es-ES', {weekday: 'short'});
                        var registro = data.registros.find(r => r.fecha === `${año}-${String(mes).padStart(2, '0')}-${String(dia).padStart(2, '0')}`) || {};

                        var fila = `
                    <tr>
                        <td>${dia}/${mes}/${año}</td>
                        <td>${diaSemana}</td>
                        <td><input type="time" class="form-control" name="entradaTM_${dia}" value="${registro.entradaTM || ''}" onchange="calcularTotalHoras(${dia})"></td>
                        <td><input type="time" class="form-control" name="salidaTM_${dia}" value="${registro.salidaTM || ''}" onchange="calcularTotalHoras(${dia})"></td>
                        <td><input type="time" class="form-control" name="entradaTT_${dia}" value="${registro.entradaTT || ''}" onchange="calcularTotalHoras(${dia})"></td>
                        <td><input type="time" class="form-control" name="salidaTT_${dia}" value="${registro.salidaTT || ''}" onchange="calcularTotalHoras(${dia})"></td>
                        <td><input type="number" class="form-control" name="totalHoras_${dia}" value="${registro.totalHoras || ''}" readonly></td>
                        <td><input type="checkbox" class="form-control" name="feriado_${dia}" ${registro.feriado ? 'checked' : ''} onchange="calcularTotal(${dia})"></td>
                        <td><input type="number" class="form-control" name="precioHora_${dia}" value="${registro.precioHora || ''}" onchange="calcularTotal(${dia})"></td>
                        <td><input type="number" class="form-control" name="total_${dia}" value="${registro.total || 0}" readonly></td>
                        <td><button class="btn btn-success" onclick="guardar(${dia}, ${registro.id !== undefined ? registro.id : 'null'})">Guardar</button></td>
                    </tr>
                `;

                        tablaRegistros.insertAdjacentHTML('beforeend', fila);
                        calcularTotal(dia); // Asegurarse de que el total se calcule y muestre al cargar los datos
                    }
                } else {
                    alert('Error al cargar los días.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al cargar los días.');
            });
}



function calcularTotalHoras(dia) {
    var entradaTM = document.querySelector(`[name="entradaTM_${dia}"]`).value;
    var salidaTM = document.querySelector(`[name="salidaTM_${dia}"]`).value;
    var entradaTT = document.querySelector(`[name="entradaTT_${dia}"]`).value;
    var salidaTT = document.querySelector(`[name="salidaTT_${dia}"]`).value;

    var totalHorasTM = calcularHoras(entradaTM, salidaTM);
    var totalHorasTT = calcularHoras(entradaTT, salidaTT);
    var totalHoras = totalHorasTM + totalHorasTT;

    document.querySelector(`[name="totalHoras_${dia}"]`).value = totalHoras.toFixed(2);

    // Calcular el total a pagar para el día
    calcularTotal(dia);
}

function calcularHoras(entrada, salida) {
    if (!entrada || !salida)
        return 0;

    var [entradaH, entradaM] = entrada.split(':').map(Number);
    var [salidaH, salidaM] = salida.split(':').map(Number);

    var entradaTotalMinutos = (entradaH * 60) + entradaM;
    var salidaTotalMinutos = (salidaH * 60) + salidaM;

    return (salidaTotalMinutos - entradaTotalMinutos) / 60; // Retorna el total de horas
}

function calcularTotal(dia) {
    var totalHoras = parseFloat(document.querySelector(`[name="totalHoras_${dia}"]`).value) || 0;
    var precioHora = parseFloat(document.querySelector(`[name="precioHora_${dia}"]`).value) || 0;
    var feriado = document.querySelector(`[name="feriado_${dia}"]`).checked;

    var total = totalHoras * precioHora;

    if (feriado) {
        total *= 2; // Ejemplo: duplicar el total si es feriado
    }

    document.querySelector(`[name="total_${dia}"]`).value = total.toFixed(2);
}

function guardar(dia, id) {
    var usuarioId = document.getElementById('usuario').value;
    var mes = document.getElementById('mes').value;
    var año = document.getElementById('año').value;
    var fecha = `${año}-${String(mes).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;
    var entradaTM = document.querySelector(`[name="entradaTM_${dia}"]`).value;
    var salidaTM = document.querySelector(`[name="salidaTM_${dia}"]`).value;
    var entradaTT = document.querySelector(`[name="entradaTT_${dia}"]`).value;
    var salidaTT = document.querySelector(`[name="salidaTT_${dia}"]`).value;
    var feriado = document.querySelector(`[name="feriado_${dia}"]`).checked;
    var precioHoraInput = document.querySelector(`[name="precioHora_${dia}"]`);
    var precioHora = parseFloat(precioHoraInput.value);

    // Validar que el campo precioHora no esté vacío o sea NaN
    if (!precioHoraInput.value || isNaN(precioHora)) {
        alert('Por favor, ingrese un valor válido para el precio por hora.');
        return;
    }

    var totalHoras = calcularHoras(entradaTM, salidaTM) + calcularHoras(entradaTT, salidaTT);
    var total = totalHoras * precioHora;
    if (feriado) {
        total *= 2;
    }

    // Obtén el token CSRF y el encabezado del HTML
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    fetch(`${BASE_URL}/horas/guardar`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            [csrfHeader]: csrfToken // Agregar token CSRF
        },
        body: new URLSearchParams({
            id: id !== null ? id : '', // Pasar el ID si existe
            usuarioId: usuarioId,
            fecha: fecha,
            entradaTM: entradaTM,
            salidaTM: salidaTM,
            entradaTT: entradaTT,
            salidaTT: salidaTT,
            feriado: feriado,
            precioHora: precioHora,
            totalHoras: totalHoras,
            total: total
        })
    })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    alert('Datos guardados correctamente.');
                    obtenerTotales(); // Actualizar los totales
                } else {
                    alert('Error al guardar los datos.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al guardar los datos.');
            });
}


function agregarAdelanto() {

    var usuarioId = document.getElementById('usuario').value;
    var fechaAdelanto = document.getElementById('fechaAdelanto').value;
    var cantidadAdelanto = parseFloat(document.getElementById('cantidadAdelanto').value);
    var conceptoAdelanto = document.getElementById('conceptoAdelanto').value;

    if (!fechaAdelanto || isNaN(cantidadAdelanto) || !conceptoAdelanto) {
        alert('Por favor complete todos los campos del adelanto.');
        return;
    }

    var adelanto = {
        usuario: {id: usuarioId},
        fecha: fechaAdelanto,
        cantidad: cantidadAdelanto,
        concepto: conceptoAdelanto
    };

    // Obtén el token CSRF y el encabezado desde las metaetiquetas
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    fetch(`${BASE_URL}/adelantos/guardar`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken // Agrega el token CSRF en el encabezado
        },
        body: JSON.stringify(adelanto)
    })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    alert('Adelanto agregado correctamente.');
                    cargarAdelantos();
                    obtenerTotales();
                } else {
                    alert('Error al agregar el adelanto.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al agregar el adelanto.');
            });
}


function cargarAdelantos() {

    var usuarioId = document.getElementById('usuario').value;
    var mes = document.getElementById('mes').value;
    var año = document.getElementById('año').value;
    cargarAdelantosParaUsuario(usuarioId, mes, año);
}

function cargarAdelantosParaUsuario(usuarioId, mes, año) {

    var tablaAdelantos = document.getElementById('tablaAdelantos');
    tablaAdelantos.innerHTML = ''; // Limpiar tabla antes de cargar nuevos datos

    fetch(`${BASE_URL}/adelantos/cargarAdelantos?usuarioId=${usuarioId}&mes=${mes}&año=${año}`)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    data.adelantos.forEach(adelanto => {
                        var fechaAdelanto = new Date(adelanto.fecha);
                        if (fechaAdelanto.getMonth() + 1 === parseInt(mes) && fechaAdelanto.getFullYear() === parseInt(año)) {
                            var fila = `
                        <tr>
                            <td>${adelanto.fecha}</td>
                            <td>${adelanto.cantidad}</td>
                            <td>${adelanto.concepto}</td> <!-- Muestra el concepto -->
                            <td>
                                <button class="btn btn-primary" onclick="editarAdelanto(${adelanto.id})">Editar</button>
                                <button class="btn btn-danger" onclick="eliminarAdelanto(${adelanto.id})">Eliminar</button>
                            </td>
                        </tr>
                    `;
                            tablaAdelantos.insertAdjacentHTML('beforeend', fila);
                        }
                    });
                } else {
                    alert('Error al cargar los adelantos.');
                }

            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al cargar los adelantos.');
            });
}


function editarAdelanto(id) {

    fetch(`${BASE_URL}/adelantos/${id}`)
            .then(response => response.json())
            .then(adelanto => {
                if (adelanto) {
                    document.getElementById('editarAdelantoId').value = adelanto.id;
                    document.getElementById('editarFechaAdelanto').value = adelanto.fecha;
                    document.getElementById('editarCantidadAdelanto').value = adelanto.cantidad;
                    document.getElementById('editarConceptoAdelanto').value = adelanto.concepto; // Agregado el campo concepto
                    $('#editarAdelantoModal').modal('show');
                } else {
                    alert('Adelanto no encontrado.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al cargar el adelanto para editar.');
            });
}


function guardarAdelantoEditado() {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    var id = document.getElementById('editarAdelantoId').value;
    var fecha = document.getElementById('editarFechaAdelanto').value;
    var cantidad = parseFloat(document.getElementById('editarCantidadAdelanto').value);
    var concepto = document.getElementById('editarConceptoAdelanto').value; // Agregado concepto

    if (!fecha || isNaN(cantidad) || !concepto) {
        alert('Por favor complete todos los campos del adelanto.');
        return;
    }

    var adelanto = {
        id: id,
        fecha: fecha,
        cantidad: cantidad,
        concepto: concepto, // Incluyendo concepto
        usuario: {id: document.getElementById('usuario').value}
    };

    fetch(`${BASE_URL}/adelantos/editar`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(adelanto)
    })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    alert('Adelanto editado correctamente.');
                    $('#editarAdelantoModal').modal('hide');
                    cargarAdelantos();
                    obtenerTotales();
                } else {
                    alert('Error al editar el adelanto.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al editar el adelanto.');
            });
}

function eliminarAdelanto(id) {


    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    fetch(`${BASE_URL}/adelantos/eliminar/${id}`, {
        method: 'DELETE',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    alert('Adelanto eliminado correctamente.');
                    cargarAdelantos();
                    obtenerTotales();
                } else {
                    alert('Error al eliminar el adelanto.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al eliminar el adelanto.');
            });
}


function obtenerTotales() {
    var usuarioId = document.getElementById('usuario').value;
    var mes = document.getElementById('mes').value;
    var año = document.getElementById('año').value;

    fetch(`${BASE_URL}/adelantos/totales?usuarioId=${usuarioId}&mes=${mes}&año=${año}`)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    document.getElementById('totalGanado').value = data.totalGanado.toFixed(2);
                    document.getElementById('totalAdelantos').value = data.totalAdelantos.toFixed(2);
                    document.getElementById('totalNeto').value = data.totalNeto.toFixed(2);
                } else {
                    alert('Error al obtener los totales.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al obtener los totales.');
            });
}

function guardarNuevoUsuario() {
    var nombre = document.getElementById('nombreUsuario').value;

    if (!nombre) {
        alert('Por favor complete todos los campos.');
        return;
    }

    var usuario = {
        nombre: nombre
    };

    fetch(`${BASE_URL}/usuarios/crear`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(usuario)
    })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    alert('Usuario agregado correctamente.');
                    $('#nuevoUsuarioModal').modal('hide');
                    location.reload(); // Recargar la página para reflejar los cambios
                } else {
                    alert('Error al agregar el usuario.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al agregar el usuario.');
            });
}


document.addEventListener('DOMContentLoaded', function () {
    // Verificar si la URL es la que requiere cargar los usuarios
    if (window.location.pathname === '/horas') {
        cargarUsuariosEliminar();
    }
});


function cargarUsuariosEliminar() {

    console.trace();
    fetch(`${BASE_URL}/usuarios/listar`)
            .then(response => response.json())
            .then(data => {
                if (data) {
                    var select = document.getElementById('usuarioEliminar');
                    select.innerHTML = '';
                    data.forEach(usuario => {
                        var option = document.createElement('option');
                        option.value = usuario.id;
                        option.textContent = usuario.nombre;
                        select.appendChild(option);
                    });
                } else {
                    alert('Error al cargar los usuarios.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al cargar los usuarios.');
            });
}

function eliminarUsuario() {
    var usuarioId = document.getElementById('usuarioEliminar').value;

    if (!usuarioId) {
        alert('Por favor, seleccione un usuario.');
        return;
    }

    // Mostrar el cuadro de confirmación antes de eliminar
    var confirmarEliminacion = confirm("¿Está seguro que desea eliminar este usuario?");

    if (!confirmarEliminacion) {
        return; // Si el usuario cancela, no hacer nada
    }

    // Obtener el token CSRF de las meta tags
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    fetch(`${BASE_URL}/usuarios/eliminar/${usuarioId}`, {
        method: 'DELETE',
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken // Incluir el token CSRF en los encabezados
        }
    })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    alert('Usuario eliminado correctamente.');
                    $('#eliminarUsuarioModal').modal('hide');
                    location.reload(); // Recargar la página para reflejar los cambios
                } else {
                    alert('Error al eliminar el usuario.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error al eliminar el usuario.');
            });
}



document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("usuarioForm");

    if (form) {
        form.addEventListener("submit", function (event) {
            event.preventDefault();  // Evitar el comportamiento por defecto del formulario

            // Obtener los valores del formulario
            const nombre = document.getElementById("nombre").value;
            // Obtener el token CSRF de las meta tags
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

            fetch(`${BASE_URL}/usuarios/crear`, {
                method: "POST",
                body: JSON.stringify({nombre: nombre}),
                headers: {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken // Incluir el token CSRF en los encabezados
                }
            })
                    .then(response => {
                        if (!response.ok) {  // Verificar si la respuesta es exitosa (código de estado 2xx)
                            return Promise.reject('Error en la solicitud');
                        }
                        return response.json();  // Convertir la respuesta a JSON si es exitosa
                    })
                    .then(data => {
                        const messageContainer = document.getElementById("message");

                        // Mostrar el cartel de mensaje fuera del modal
                        if (data.success) {
                            messageContainer.innerHTML =
                                    `<div class="alert alert-success" role="alert">Usuario creado con éxito: ${data.usuario.nombre}</div>`;

                            // Mostrar un mensaje de éxito en un alert
                            alert(`Usuario creado con éxito: ${data.usuario.nombre}`);
                        } else {
                            messageContainer.innerHTML =
                                    `<div class="alert alert-danger" role="alert">Error al crear usuario.</div>`;
                        }

                        // Hacer visible el mensaje
                        messageContainer.style.display = 'block';

                        // Cerrar el modal
                        $('#crearUsuarioModal').modal('hide');

                        // Refrescar la página
                        location.reload();  // Esto recargará la página actual
                    })
                    .catch(error => {
                        // Manejar errores de la solicitud o problemas con la respuesta
                        document.getElementById("message").innerHTML =
                                `<div class="alert alert-danger" role="alert">Error en la solicitud: ${error}</div>`;
                    });
        });
    }
});



// Llamar a la función cuando se envíe el formulario
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("usuarioForm");
    if (form) {
        form.addEventListener("submit", function (event) {
            event.preventDefault();  // Evitar el comportamiento por defecto del formulario

            // Llamar a la función para crear el usuario
            crearUsuario();
        });
    }
});


document.addEventListener("DOMContentLoaded", function () {
    let fechaInput = document.getElementById("fecha");

    // 📌 Cargar la fecha actual al abrir la página
    let hoy = new Date().toISOString().split("T")[0];
    fechaInput.value = hoy;

    // 🔥 Llamar a la función al cargar la página
    obtenerVentas(hoy);

    // 📅 Evento cuando se cambia la fecha
    fechaInput.addEventListener("change", function () {
        obtenerVentas(this.value);
    });
});

function obtenerVentas(fecha) {
    fetch(`${BASE_URL}/ventas/fechas?fecha=${fecha}`)
            .then(response => response.json())
            .then(data => {

                // 📌 Actualizar el HTML con los valores obtenidos
                document.getElementById("totalEfectivo").textContent = data.totalEfectivo.toFixed(2);
                document.getElementById("totalCredito").textContent = data.totalCredito.toFixed(2);
                document.getElementById("totalDebito").textContent = data.totalDebito.toFixed(2);
                document.getElementById("totalMercadoPago").textContent = data.totalMercadoPago.toFixed(2);
                document.getElementById("totalGeneral").textContent = data.totalGeneral.toFixed(2);
            })
            .catch(error => console.error("Error al obtener las ventas:", error));
}




////////////////////////-PERDIDA INVENTARIO-///////////////////////////

$(document).ready(function () {
    $('#modalPerdida').on('show.bs.modal', function () {
        $.getJSON('/perdida_inventario/productos', function (data) {
            let select = $('#producto');
            select.empty();
            select.append('<option value="">Seleccione un producto</option>');

            $.each(data, function (index, producto) {
                select.append('<option value="' + producto.id + '">' + producto.nombre + '</option>');
            });

            // Recargar el selectpicker después de cargar los productos
            select.selectpicker('refresh');
        });
    });
});

$('#formPerdida').submit(function (event) {
    event.preventDefault(); // Evita el envío automático

    let productoId = $('#producto').val();
    let cantidad = $('#cantidad').val();
    let descripcion = $('#descripcion').val();

    if (!productoId || !cantidad || cantidad <= 0) {
        alert("Debe seleccionar un producto y una cantidad válida.");
        return;
    }

    // Obtener el token CSRF del meta tag
    let csrfToken = $('meta[name="_csrf"]').attr('content');
    let csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    $.ajax({
        url: `${BASE_URL}/perdida_inventario/guardar`,
        type: "POST",
        contentType: "application/x-www-form-urlencoded",
        data: {
            productoId: productoId,
            cantidad: cantidad,
            descripcion: descripcion
        },
        beforeSend: function (xhr) {
            xhr.setRequestHeader(csrfHeader, csrfToken); // Agregar CSRF token en el header
        },
        success: function (response) {
            alert("Pérdida de inventario registrada correctamente.");
            location.reload(); // Recargar la página después de guardar
        },
        error: function (xhr, status, error) {
            alert("Error al registrar la pérdida: " + xhr.responseText);
        }
    });
});


///////////////////////////////-PRECIOS-//////////////////////////

// Manejar el aumento de precios
document.getElementById("formAumento").addEventListener("submit", function (event) {
    event.preventDefault();

    const tipoProducto = document.getElementById("tipoProductoAumento").value;
    const porcentaje = document.getElementById("porcentajeAumento").value;

    if (!confirm(`¿Estás seguro de que deseas aumentar los precios en un ${porcentaje}% para los productos de tipo ${tipoProducto}?`)) {
        return; // Cancelar si el usuario no confirma
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    fetch(`${BASE_URL}/precios/aumentar`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken,
        },
        body: JSON.stringify({tipoProducto, porcentaje}),
    })
            .then((response) => {
                if (response.ok) {
                    alert("Aumento aplicado con éxito.");
                } else {
                    alert("Error al aplicar el aumento.");
                }
            })
            .catch(() => alert("Error de conexión."));
});

// Manejar el descuento de precios
document.getElementById("formDescuento").addEventListener("submit", function (event) {
    event.preventDefault();

    const tipoProducto = document.getElementById("tipoProductoDescuento").value;
    const porcentaje = document.getElementById("porcentajeDescuento").value;

    if (!confirm(`¿Estás seguro de que deseas aplicar un descuento del ${porcentaje}% para los productos de tipo ${tipoProducto}?`)) {
        return; // Cancelar si el usuario no confirma
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

    fetch(`${BASE_URL}/precios/descuento`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken,
        },
        body: JSON.stringify({tipoProducto, porcentaje}),
    })
            .then((response) => {
                if (response.ok) {
                    alert("Descuento aplicado con éxito.");
                } else {
                    alert("Error al aplicar el descuento.");
                }
            })
            .catch(() => alert("Error de conexión."));
});


//////////////////////////////////////////////-VENTAS-/////////////////////////////////////////////



$(document).ready(function () {
    $('#cliente').select2({
        placeholder: 'Seleccionar Cliente...',
        allowClear: true
    });

    $('#producto').select2({
        placeholder: 'Buscar Producto...',
        allowClear: true
    });

    function actualizarTotales() {
        let totalLista = 0;
        let totalMetodoPago = 0;
        let totalFinal = 0;
        const metodoPago = $('#metodoPago').val();
        const descuentoPorcentaje = parseFloat($('#descuento').val()) || 0;

        $('#productosSeleccionados tr').each(function () {
            const precio = parseFloat($(this).find('.precio').text());
            const cantidad = parseInt($(this).find('.cantidad').val());
            totalLista += precio * cantidad;
        });

        if (metodoPago === 'CREDITO') {
            totalMetodoPago = totalLista * 1.15;
        } else {
            totalMetodoPago = totalLista;
        }

        totalFinal = totalMetodoPago - (totalMetodoPago * (descuentoPorcentaje / 100));

        $('#totalLista').val(totalLista.toFixed(2));
        $('#totalMetodoPago').val(totalMetodoPago.toFixed(2));
        $('#totalFinal').val(totalFinal.toFixed(2));
    }

    document.addEventListener("DOMContentLoaded", function () {
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
    });

    document.addEventListener("DOMContentLoaded", function () {
        $('#productosSeleccionados').on('change', '.cantidad', function () {
            const cantidad = $(this).val();
            const stock = $(this).closest('tr').find('td').eq(2).text();

            if (parseInt(cantidad) > parseInt(stock)) {
                alert('La cantidad no puede ser mayor que el stock disponible.');
                $(this).val(stock);
            }

            actualizarTotales();
        });
    });

    document.addEventListener("DOMContentLoaded", function () {
        $('#productosSeleccionados').on('click', '.eliminarProducto', function () {
            if ($('#productosSeleccionados tr').length === 1) {
                alert('No puede eliminar el último producto.');
                return false;
            }
            $(this).closest('tr').remove();
            actualizarTotales();
        });
    });
    
    $('#metodoPago, #descuento').change(function () {
        actualizarTotales();
    });

    // Inicializar eventos al cargar la página
    $('#productosSeleccionados .eliminarProducto').each(function () {
        $(this).off('click').on('click', function () {
            if ($('#productosSeleccionados tr').length === 1) {
                alert('No puede eliminar el último producto.');
                return false;
            }
            $(this).closest('tr').remove();
            actualizarTotales();
        });
    });

    // Llamar a actualizarTotales al cargar la página para mostrar los valores correctos
    actualizarTotales();
});

$(document).ready(function () {
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

    function actualizarTotales() {
        let totalLista = 0;
        let totalMetodoPago = 0;
        let totalFinal = 0;
        const metodoPago = $('#metodoPago').val();
        const descuentoPorcentaje = parseFloat($('#descuento').val()) || 0;

        $('#productosSeleccionados tr').each(function () {
            const precio = parseFloat($(this).find('.precio').text());
            const cantidad = parseInt($(this).find('.cantidad').val());
            totalLista += precio * cantidad;
        });

        if (metodoPago === 'CREDITO') {
            totalMetodoPago = totalLista * 1.15;
        } else {
            totalMetodoPago = totalLista;
        }

        totalFinal = totalMetodoPago - (totalMetodoPago * (descuentoPorcentaje / 100));

        $('#totalLista').val(totalLista.toFixed(2));
        $('#totalMetodoPago').val(totalMetodoPago.toFixed(2));
        $('#totalFinal').val(totalFinal.toFixed(2));
    }

//    $('#agregarProducto').click(function () {
//        const productoId = $('#producto').val();
//        const productoNombre = $('#producto option:selected').text();
//        const precio = $('#producto option:selected').data('precio');
//        const stock = $('#producto option:selected').data('stock');
//
//        if (productoId) {
//            if ($('#productosSeleccionados input[name="productoIds[]"][value="' + productoId + '"]').length > 0) {
//                alert('Este producto ya está en la lista.');
//                return;
//            }
//
//            if (stock <= 0) {
//                alert('Este producto no tiene stock disponible.');
//                return;
//            }
//
//            $('#productosSeleccionados').append(`
//                        <tr>
//                            <td>${productoNombre}</td>
//                            <td class="precio">${precio}</td>
//                            <td>${stock}</td>
//                            <td><input type="number" name="cantidades[]" class="form-control cantidad" min="1" max="${stock}" value="1"></td>
//                            <td><button type="button" class="btn btn-danger eliminarProducto">Eliminar</button></td>
//                            <input type="hidden" name="productoIds[]" value="${productoId}">
//                        </tr>
//                    `);
//
//            actualizarTotales();
//        }
//    });

    $('#productosSeleccionados').on('change', '.cantidad', function () {
        const cantidad = $(this).val();
        const stock = $(this).closest('tr').find('td').eq(2).text();

        if (parseInt(cantidad) > parseInt(stock)) {
            alert('La cantidad no puede ser mayor que el stock disponible.');
            $(this).val(stock);
        }

        actualizarTotales();
    });

    $('#productosSeleccionados').on('click', '.eliminarProducto', function () {
        $(this).closest('tr').remove();
        actualizarTotales();
    });

    $('#metodoPago, #descuento').change(function () {
        actualizarTotales();
    });

    $('#ventaForm').submit(function (event) {
        if ($('#productosSeleccionados tr').length === 0) {
            alert('Debe agregar al menos un producto antes de crear la venta.');
            event.preventDefault(); // Evita que el formulario se envíe
        }
    });

});

