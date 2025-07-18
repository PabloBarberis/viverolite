document.addEventListener("DOMContentLoaded", function () {
    const usuarioSelect = document.getElementById("usuario");
    const mesSelect = document.getElementById("mes");
    const anioSelect = document.getElementById("año");
    const tablaRegistros = document.getElementById("tablaRegistros");

    // === Cargar usuarios en el filtro principal ===
    function cargarUsuariosEnFiltro() {
        fetch("/usuarios/lista-usuarios")
            .then(response => response.json())
            .then(data => {
                const select = document.getElementById("usuario");
                select.innerHTML = '<option value="" disabled selected>Seleccione un usuario</option>';
                data.forEach(usuario => {
                    const option = document.createElement("option");
                    option.value = usuario.id;
                    option.textContent = usuario.nombre;
                    select.appendChild(option);
                });
            })
            .catch(error => console.error("Error al cargar usuarios:", error));
    }

    // === Llenar meses ===
    function llenarMeses(select) {
        const meses = [
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ];
        meses.forEach((mes, index) => {
            const option = document.createElement("option");
            option.value = index + 1;
            option.textContent = mes;
            select.appendChild(option);
        });
    }

    // === Llenar años (desde 2025 en adelante) ===
    function llenarAnios(select) {
        const anioInicio = 2025;
        const cantidadAnios = 10;

        for (let i = anioInicio; i <= anioInicio + cantidadAnios; i++) {
            const option = document.createElement("option");
            option.value = i;
            option.textContent = i;
            select.appendChild(option);
        }
    }

    // === Seleccionar mes y año actual por defecto ===
    function seleccionarMesAnioActual() {
        const hoy = new Date();
        const mesActual = hoy.getMonth() + 1;
        const anioActual = hoy.getFullYear();

        mesSelect.value = mesActual;
        anioSelect.value = anioActual;
    }

    // === Cargar usuarios en el modal de nuevo registro ===
    function cargarUsuariosEnModal() {
        fetch("/usuarios/lista-usuarios")
            .then(response => response.json())
            .then(data => {
                const select = document.getElementById("usuarioRegistro");
                if (!select) return;

                select.innerHTML = '<option value="" disabled selected>Seleccione un usuario</option>';
                data.forEach(usuario => {
                    const option = document.createElement("option");
                    option.value = usuario.id;
                    option.textContent = usuario.nombre;
                    select.appendChild(option);
                });
            })
            .catch(error => console.error("Error al cargar usuarios en modal:", error));
    }

    let totalGanado = 0.0;
    let totalAdelantos = 0.0;


    // === Cargar datos horarios ===
    function cargarDatos() {
        const usuarioId = usuarioSelect.value;
        const mes = mesSelect.value;
        const anio = anioSelect.value;

        if (usuarioId && mes && anio) {
            fetch(`/horas/api/registroshorarios?usuarioId=${usuarioId}&mes=${mes}&anio=${anio}`)
                .then(response => response.json())
                .then(data => {
                    tablaRegistros.innerHTML = "";
                    totalGanado = 0.0;
                    data.forEach(registro => {
                        const row = document.createElement("tr");

                        const totalDia = registro.feriado
                            ? (registro.totalHoras * registro.precioHora * 2).toFixed(2)
                            : (registro.totalHoras * registro.precioHora).toFixed(2);

                        row.innerHTML = `
                            <td>${formatearFechaConDia(registro.fecha)}</td>
                            <td>${registro.entradaTM || ''}</td>
                            <td>${registro.salidaTM || ''}</td>
                            <td>${registro.entradaTT || ''}</td>
                            <td>${registro.salidaTT || ''}</td>
                            <td>${parseFloat(registro.totalHoras).toFixed(2)}</td>
                            <td>${registro.feriado ? "Sí" : "No"}</td>
                            <td>${registro.precioHora}</td>
                            <td>${totalDia}</td>
                            <td>
                                <button class="btn btn-sm btn-warning" onclick="editarRegistro('${registro.id}')">Editar</button>
                                <button class="btn btn-sm btn-danger" onclick="eliminarRegistro('${registro.id}')">Eliminar</button>
                            </td>
                        `;
                        tablaRegistros.appendChild(row);
                        totalGanado += parseFloat(totalDia);
                    });
                    document.getElementById("totalGanado").value = `$${totalGanado.toFixed(2)}`;
                    cargarAdelantos(usuarioId, mes, anio);
                })
                .catch(error => console.error("Error al cargar registros:", error));
        } else {
            tablaRegistros.innerHTML = "";
        }
    }

    // === Formatear fecha con día abreviado en español ===
    function formatearFechaConDia(fechaStr) {
        const dias = {
            'Monday': 'LUN',
            'Tuesday': 'MAR',
            'Wednesday': 'MIE',
            'Thursday': 'JUE',
            'Friday': 'VIE',
            'Saturday': 'SAB',
            'Sunday': 'DOM'
        };

        // Agregamos una hora ficticia local para evitar problema de zona horaria
        const date = new Date(fechaStr + "T00:00:00");
        const diaEnIngles = date.toLocaleDateString('en-US', { weekday: 'long' });
        const diaAbreviado = dias[diaEnIngles] || '---';

        const dia = String(date.getDate()).padStart(2, '0');
        const mes = String(date.getMonth() + 1).padStart(2, '0'); // getMonth() es 0-based

        return `${diaAbreviado} ${dia}-${mes}`;
    }

    // === Botón "Nuevo Registro" ===
    document.getElementById("btnNuevoRegistro")?.addEventListener("click", function () {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');

        document.getElementById("fecha").value = `${year}-${month}-${day}`;
    });

    // === Guardar nuevo registro ===
    window.guardarNuevoRegistro = function () {
        const usuarioId = document.getElementById("usuarioRegistro").value;
        const registroId = document.getElementById("registroId").value;
        const fecha = document.getElementById("fecha").value;
        const entradaTM = document.getElementById("entradaTM").value;
        const salidaTM = document.getElementById("salidaTM").value;
        const entradaTT = document.getElementById("entradaTT").value;
        const salidaTT = document.getElementById("salidaTT").value;
        const feriado = document.getElementById("feriado").checked;
        const precioHora = parseFloat(document.getElementById("precioHora").value);

        if (!usuarioId) {
            alert("Por favor seleccione un usuario.");
            return;
        }

        const registro = {
            id: registroId ? Number(registroId) : null,
            usuarioId: usuarioId,
            fecha: fecha,
            entradaTM: entradaTM,
            salidaTM: salidaTM,
            entradaTT: entradaTT,
            salidaTT: salidaTT,
            feriado: feriado,
            precioHora: precioHora
        };

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

        const url = registroId
            ? `/horas/api/registroshorarios/${registroId}`
            : `/horas/api/registroshorarios`;

        const method = registroId ? "PUT" : "POST";

        fetch(url, {
            method: method,
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify(registro)
        })
            .then(response => {
                if (response.ok) {
                    alert("✅ ¡Registro guardado con éxito!");
                    $('#crearRegistroModal').modal('hide');
                    limpiarModal();
                    cargarDatos();
                } else if (response.status === 400) {
                    // ✅ Manejo de error personalizado
                    return response.text().then(text => {
                        alert("Error: " + text);
                    });
                } else {
                    alert("Hubo un error al guardar el registro.");
                }
            })
            .catch(error => {
                console.error("Error:", error);
                alert("Hubo un error al guardar el registro.");
            });
    };

    function limpiarModal() {
        document.getElementById("registroId").value = "";
        document.getElementById("usuarioRegistro").value = "";
        document.getElementById("fecha").value = "";
        document.getElementById("entradaTM").value = "";
        document.getElementById("salidaTM").value = "";
        document.getElementById("entradaTT").value = "";
        document.getElementById("salidaTT").value = "";
        document.getElementById("feriado").checked = false;
        document.getElementById("precioHora").value = "0.00";
    }

    // Limpiar el modal cuando se cierre
    $('#crearRegistroModal').on('hidden.bs.modal', function () {
        limpiarModal();
    });

    window.editarRegistro = function (id) {
        fetch(`/horas/api/registroshorarios/${id}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("No se pudo cargar el registro");
                }
                return response.json();
            })
            .then(registro => {
                // Precargar datos en el modal
                document.getElementById("registroId").value = registro.id;
                console.log(registro.id);
                document.getElementById("usuarioRegistro").value = registro.usuario.id;
                document.getElementById("fecha").value = registro.fecha; // Formato LocalDate: "YYYY-MM-DD"
                document.getElementById("entradaTM").value = registro.entradaTM || '';
                document.getElementById("salidaTM").value = registro.salidaTM || '';
                document.getElementById("entradaTT").value = registro.entradaTT || '';
                document.getElementById("salidaTT").value = registro.salidaTT || '';
                document.getElementById("feriado").checked = registro.feriado;
                document.getElementById("precioHora").value = registro.precioHora;

                // Mostrar el modal
                $('#crearRegistroModal').modal('show');
            })
            .catch(error => {
                console.error("Error al cargar el registro:", error);
                alert("No se pudo cargar el registro para editar.");
            });
    };

    function cargarAdelantos(usuarioId, mes, anio) {
        fetch(`/horas/api/adelantos?usuarioId=${usuarioId}&mes=${mes}&año=${anio}`)
            .then(response => response.json())
            .then(data => {
                const tablaAdelantos = document.getElementById("tablaAdelantos");
                tablaAdelantos.innerHTML = "";

                data.adelantos.forEach(adelanto => {
                    const row = document.createElement("tr");
                    row.innerHTML = `
                    <td>${adelanto.id}</td>
                    <td>${adelanto.fecha}</td>
                    <td>${adelanto.descripcion}</td>
                    <td>$${adelanto.monto.toFixed(2)}</td>
                `;
                    tablaAdelantos.appendChild(row);
                });

                // Mostrar total adelantos
                document.getElementById("totalAdelantos").value = `$${data.total.toFixed(2)}`;
                totalAdelantos = data.total;

                // Calcular y mostrar total neto
                const totalNeto = (totalGanado - totalAdelantos).toFixed(2);
                document.getElementById("totalNeto").value = `$${totalNeto}`;
            })
            .catch(error => {
                console.error("Error al cargar adelantos:", error);
                document.getElementById("totalAdelantos").value = "$0.00";
            });
    }

    window.eliminarRegistro = function (id) {
        if (!confirm("¿Está seguro de que quiere eliminar este registro?")) {
            return;
        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

        fetch(`/horas/api/registroshorarios/${id}`, {
            method: "DELETE",
            headers: {
                [csrfHeader]: csrfToken
            }
        })
            .then(response => {
                if (response.ok) {
                    cargarDatos(); // Recargar registros horarios
                    alert("✅ Registro eliminado Correctamente");
                } else {
                    alert("Hubo un error al eliminar el registro");
                }
            })
            .catch(error => {
                console.error("Error al eliminar registro:", error);
                alert("Hubo un error al eliminar el registro");
            });
    };

    window.generarReportePdf = function () {
        const usuarioId = usuarioSelect.value;
        const mes = mesSelect.value;
        const anio = anioSelect.value;

        if (!usuarioId || !mes || !anio) {
            alert("Por favor seleccione un usuario, mes y año.");
            return;
        }

        const url = `/horas/reporte-pdf?id=${usuarioId}&mes=${mes}&anio=${anio}`;

        fetch(url, {
            method: 'GET'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la respuesta del servidor');
                }
                return response.blob();
            })
            .then(blob => {
                const urlBlob = window.URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = urlBlob;
                link.setAttribute('download', `reporte_Horas_${anio}_${mes}.pdf`);
                document.body.appendChild(link);
                link.click();
                link.remove();
            })
            .catch(error => {
                console.error("Error al generar el reporte:", error);
                alert("Hubo un error al generar el reporte. Verifique que los datos estén cargados.");
            });
    };
    // === Eventos de cambio para cargar datos ===
    usuarioSelect.addEventListener("change", cargarDatos);
    mesSelect.addEventListener("change", cargarDatos);
    anioSelect.addEventListener("change", cargarDatos);

    // === Cargar datos iniciales ===
    cargarUsuariosEnFiltro();
    llenarMeses(mesSelect);
    llenarAnios(anioSelect);
    seleccionarMesAnioActual();

    // === Cargar usuarios en el modal ===
    cargarUsuariosEnModal();
});