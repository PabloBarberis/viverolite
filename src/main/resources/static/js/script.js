import BASE_URL from '/js/config.js';

// Obtén el token CSRF y el encabezado del HTML
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

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

window.cargarUsuario = function () {
    
    var usuarioId = document.getElementById('usuario').value;
    var configuracion = document.getElementById('configuracion');
    configuracion.style.display = 'block';

    if (usuarioId !== configuracion.dataset.lastUserId) {
        configuracion.dataset.lastUserId = usuarioId;

        var tablaRegistros = document.getElementById('tablaRegistros');
        tablaRegistros.innerHTML = '';

        saludarUsuario(usuarioId);
    }
};



function saludarUsuario(usuarioId) {
    var usuario = document.querySelector('#usuario option[value="' + usuarioId + '"]').text;
    document.getElementById('saludo').innerText = 'Hola ' + usuario + '!';
    cargarDias(); // Solo llamamos a cargarDias aquí    
}

window.cargarDias = cargarDias;
function cargarDias() {
    var usuarioId = document.getElementById('usuario').value;
    var mes = document.getElementById('mes').value;
    var año = document.getElementById('año').value;
    cargarDiasParaUsuario(usuarioId, mes, año);
    obtenerTotales();
}

window.cargarDiasParaUsuario = cargarDiasParaUsuario;
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



window.calcularTotalHoras = function (dia) {
    
    var entradaTM = document.querySelector(`[name="entradaTM_${dia}"]`);
    if (!entradaTM) {
        console.error(`Elemento entradaTM_${dia} no encontrado`);
        return;
    }
    var salidaTM = document.querySelector(`[name="salidaTM_${dia}"]`);
    var entradaTT = document.querySelector(`[name="entradaTT_${dia}"]`);
    var salidaTT = document.querySelector(`[name="salidaTT_${dia}"]`);

    var totalHorasTM = calcularHoras(entradaTM.value, salidaTM.value);
    var totalHorasTT = calcularHoras(entradaTT.value, salidaTT.value);
    var totalHoras = totalHorasTM + totalHorasTT;

    document.querySelector(`[name="totalHoras_${dia}"]`).value = totalHoras.toFixed(2);
    calcularTotal(dia);
};

window.guardar = function (dia, id) {
    console.log("Función guardar ejecutada para el día:", dia, "ID:", id);
};


window.calcularHoras = function (entrada, salida) {
    if (!entrada || !salida)
        return 0;

    var [entradaH, entradaM] = entrada.split(':').map(Number);
    var [salidaH, salidaM] = salida.split(':').map(Number);

    var entradaTotalMinutos = (entradaH * 60) + entradaM;
    var salidaTotalMinutos = (salidaH * 60) + salidaM;

    return (salidaTotalMinutos - entradaTotalMinutos) / 60;
};


window.calcularTotal = function (dia) {
    var totalHoras = parseFloat(document.querySelector(`[name="totalHoras_${dia}"]`).value) || 0;
    var precioHora = parseFloat(document.querySelector(`[name="precioHora_${dia}"]`).value) || 0;
    var feriado = document.querySelector(`[name="feriado_${dia}"]`).checked;

    var total = totalHoras * precioHora;

    if (feriado) {
        total *= 2;
    }

    document.querySelector(`[name="total_${dia}"]`).value = total.toFixed(2);
};

window.guardar = guardar;
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
window.eliminarUsuario = eliminarUsuario;
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
            
        });
    }
});


