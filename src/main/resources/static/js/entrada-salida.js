// Variables globales
import BASE_URL from '/js/config.js';
let ventas = [];
let ingresosEgresos = [];
let ventasMesAnterior = [];
let ingresosEgresosMesAnterior = [];
let editando = false; // Variable global que indica si estamos en modo edición
const btnCrearIngreso = document.getElementById("btnCrearIngreso");

const usuarioSelect = document.getElementById("usuario");
const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute("content") : null;
const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute("content") : null;


// Llenar los selects al cargar la página
document.addEventListener("DOMContentLoaded", () => {
    cargarMeses();
    cargarAnios();
    seleccionarMesYAnioActual();
    obtenerDatos(); // Llamar apenas carga la página

    document.getElementById("mes").addEventListener("change", obtenerDatos);
    document.getElementById("anio").addEventListener("change", obtenerDatos);

    

    // Asignamos la función abrirModalCrear al evento click del botón
    if (btnCrearIngreso) {
        btnCrearIngreso.addEventListener("click", abrirModalCrear);
    }


});

// Cargar usuarios
fetch(`${BASE_URL}/usuarios/listar`, {
    method: "GET"

})
    .then(response => response.json())
    .then(data => {
        usuarioSelect.innerHTML = data.map(user => `<option value="${user.id}">${user.nombre}</option>`).join("");
    })
    .catch(error => console.error("Error cargando usuarios:", error));


formIngresoEgreso.addEventListener("submit", function (event) {
    event.preventDefault();

    // Captura los valores del formulario
    const ingreso = document.getElementById("tipoMovimiento").value === "true";
    const metodoPago = document.getElementById("metodoPago").value;
    const usuarioId = document.getElementById("usuario").value;
    let fecha = document.getElementById("fecha").value;
    const descripcion = document.getElementById("descripcion").value;
    const monto = parseFloat(document.getElementById("monto").value);
    const esAdelanto = document.getElementById("esAdelanto").checked; // Capturar estado del checkbox

    // 📌 Convertir fecha correctamente a LocalDateTime
    if (fecha) {
        const ahora = new Date(); // Obtener la hora actual
        const hora = ahora.getHours().toString().padStart(2, '0');   // HH
        const minutos = ahora.getMinutes().toString().padStart(2, '0'); // mm
        const segundos = ahora.getSeconds().toString().padStart(2, '0'); // ss

        // Formato correcto: YYYY-MM-DDTHH:mm:ss
        fecha = `${fecha}T${hora}:${minutos}:${segundos}`;
    }

    // Si estamos editando, incluir el ID en el objeto
    const movimiento = {
        ingreso,
        metodoPago,
        usuarioId,
        fecha, // Ahora en formato LocalDateTime
        descripcion,
        monto,
        adelanto: esAdelanto, // Añadir si es un adelanto
        id: editando ? formIngresoEgreso.dataset.editandoId : undefined // Si estamos editando, agregar el id
    };

    // Definir la URL y el método
    const url = editando
  ? `${BASE_URL}/ingresoegreso/actualizar`   // ✅ correcto
  : `${BASE_URL}/ingresoegreso/guardar`;

    

    fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(movimiento)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert("Ingreso/Egreso guardado correctamente.");
                formIngresoEgreso.reset();
                location.reload();
            } else {
                alert("Error: " + data.message);
            }
        })
        .catch(error => console.error("Error al guardar movimiento:", error));
});


// Escuchar el evento de cierre del modal
document.getElementById("modalIngresoEgreso").addEventListener('hidden.bs.modal', function () {
    editando = false;  // Restablecer el estado de edición
    formIngresoEgreso.reset();  // Limpiar el formulario
});


// Llenar los meses
function cargarMeses() {
    const selectMes = document.getElementById("mes");
    const meses = [
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    ];
    meses.forEach((mes, index) => {
        const option = document.createElement("option");
        option.value = index + 1; // Mes en formato numérico (1-12)
        option.textContent = mes;
        selectMes.appendChild(option);
    });
}

// Llenar los años (2025 - 2030)
function cargarAnios() {
    const selectAnio = document.getElementById("anio");
    for (let anio = 2025; anio <= 2030; anio++) {
        const option = document.createElement("option");
        option.value = anio;
        option.textContent = anio;
        selectAnio.appendChild(option);
    }
}

// Seleccionar el mes y año actuales por defecto
function seleccionarMesYAnioActual() {
    const fechaActual = new Date();
    document.getElementById("mes").value = fechaActual.getMonth() + 1; // getMonth() devuelve 0-11
    document.getElementById("anio").value = fechaActual.getFullYear();
}

// Obtener datos desde el backend y asignar a las variables globales
function obtenerDatos() {
    const mes = document.getElementById("mes").value;
    const anio = document.getElementById("anio").value;

    if (!mes || !anio) return;

    fetch(`${BASE_URL}/ventas/movimientos?mes=${mes}&anio=${anio}`)
        .then(res => res.json())
        .then(data => {
            // Asignar las listas recibidas a las variables globales
            ventas = data.ventas || [];
            ingresosEgresos = data.ingresosEgresos || [];
            ventasMesAnterior = data.ventasMesAnterior || [];
            ingresosEgresosMesAnterior = data.ingresosEgresosMesAnterior || [];

            // Procesar los datos y actualizar las tablas
            const { efectivoMesActual, movimientosEfectivo } = procesarDatosEfectivo(
                ventas,
                ingresosEgresos,
                ventasMesAnterior,
                ingresosEgresosMesAnterior
            );

            const { tarjetaMesActual, movimientosTarjeta } = procesarDatosTarjeta(
                ventas,
                ingresosEgresos,
                ventasMesAnterior,
                ingresosEgresosMesAnterior
            );

            const { mpSachaMesActual, movimientosMpSacha } = procesarDatosMercadoPagoSacha(
                ventas,
                ingresosEgresos,
                ventasMesAnterior,
                ingresosEgresosMesAnterior
            );

            const { mpValeMesActual, movimientosMpVale } = procesarDatosMercadoPagoVale(
                ventas,
                ingresosEgresos,
                ventasMesAnterior,
                ingresosEgresosMesAnterior
            );


            // Asegúrate de que estos arrays ya estén filtrados con los datos correctos
            actualizarTablaMovimientos("movEfectivo", movimientosEfectivo);
            actualizarTablaMovimientos("movTarjeta", movimientosTarjeta);
            actualizarTablaMovimientos("movMpSacha", movimientosMpSacha);
            actualizarTablaMovimientos("movMpVale", movimientosMpVale);

            actualizarTablaEfectivo(efectivoMesActual);
            actualizarTablaTarjeta(tarjetaMesActual);
            actualizarTablaMpSacha(mpSachaMesActual);
            actualizarTablaMpVale(mpValeMesActual);

        })
        .catch(err => console.error("Error al obtener datos:", err));
}


function procesarDatosEfectivo(
    ventas,
    ingresosEgresos,
    ventasMesAnterior,
    ingresosEgresosMesAnterior) {

    const efectivoMesActual = {
        saldoAnterior: 0,
        periodos: {
            "1 al 7": 0,
            "8 al 14": 0,
            "15 al 21": 0,
            "22 al 28": 0,
            "29 al fin de mes": 0,
        },
        totalVentas: 0,
        totalIngresos: 0,
        totalEgresos: 0,
    };

    const movimientosEfectivo = [];

    // Función auxiliar para determinar el período de una fecha
    function obtenerPeriodo(fecha) {
        const dia = fecha.getDate();
        if (dia >= 1 && dia <= 7) return "1 al 7";
        if (dia >= 8 && dia <= 14) return "8 al 14";
        if (dia >= 15 && dia <= 21) return "15 al 21";
        if (dia >= 22 && dia <= 28) return "22 al 28";
        const ultimoDiaMes = new Date(fecha.getFullYear(), fecha.getMonth() + 1, 0).getDate();
        if (dia >= 29 && dia <= ultimoDiaMes) return "29 al fin de mes";
        return null;
    }

    // Calcular el saldo anterior en efectivo
    let saldoAnteriorVentas = 0;
    ventasMesAnterior.forEach((venta) => {
        if (venta && venta.pagos) {
            venta.pagos
                .filter((pago) => pago.metodo === "EFECTIVO")
                .forEach((pago) => {
                    saldoAnteriorVentas += pago.monto;
                });
        }
    });

    let saldoAnteriorIngresos = 0;
    let saldoAnteriorEgresos = 0;
    ingresosEgresosMesAnterior
        .filter((ie) => ie.metodoPago === "EFECTIVO")
        .forEach((ie) => {
            if (ie.ingreso) {
                saldoAnteriorIngresos += ie.monto;
            } else {
                saldoAnteriorEgresos += ie.monto;
            }
        });

    efectivoMesActual.saldoAnterior = saldoAnteriorVentas + saldoAnteriorIngresos - saldoAnteriorEgresos;

    // Procesar ventas del mes actual
    ventas.forEach((venta) => {
        if (venta && venta.pagos) {
            venta.pagos
                .filter((pago) => pago.metodo === "EFECTIVO")
                .forEach((pago) => {
                    const fechaVenta = new Date(venta.fecha);
                    const periodo = obtenerPeriodo(fechaVenta);
                    if (periodo) {
                        efectivoMesActual.periodos[periodo] += pago.monto;
                    }
                    efectivoMesActual.totalVentas += pago.monto;
                });
        }
    });

    // Procesar ingresos y egresos del mes actual
    ingresosEgresos
        .filter((ie) => ie.metodoPago === "EFECTIVO")
        .forEach((ie) => {
            const fechaIE = new Date(ie.fecha);
            const periodo = obtenerPeriodo(fechaIE);
            const monto = ie.ingreso ? ie.monto : -ie.monto;
            if (periodo) {
                efectivoMesActual.periodos[periodo] += monto; // Sumar ingresos, restar egresos al período
            }
            if (ie.ingreso) {
                efectivoMesActual.totalIngresos += ie.monto;
            } else {
                efectivoMesActual.totalEgresos += ie.monto;
            }
            movimientosEfectivo.push({
                id: ie.id,
                fecha: fechaIE.toLocaleDateString(),
                descripcion: ie.descripcion,
                usuario: ie.usuario ? ie.usuario.nombre : "N/A",
                tipo: ie.ingreso ? "Ingreso" : "Egreso",
                monto: monto.toFixed(2),
                metodo: ie.metodoPago,
            });
        });

    const totalDelMes =
        efectivoMesActual.totalVentas +
        efectivoMesActual.totalIngresos -
        efectivoMesActual.totalEgresos;
    efectivoMesActual.totalDelMes = totalDelMes;
    efectivoMesActual.totalConSaldoAnterior =
        efectivoMesActual.saldoAnterior + totalDelMes;

    return { efectivoMesActual, movimientosEfectivo };
}

// El resto de tu código (funciones actualizarTablaEfectivo y actualizarTablaMovimientos, y la llamada a obtenerDatos) permanece igual.

// El resto de tu código permanece igual.

function actualizarTablaEfectivo(data) {
    const tablaEfectivoBody = document.getElementById("tablaEfectivo");
    tablaEfectivoBody.innerHTML = ""; // Limpiar la tabla

    const filas = [
        { periodo: "SALDO MES ANTERIOR", total: `$${data.saldoAnterior.toFixed(2)}` },
        { periodo: "1 al 7", total: `$${data.periodos["1 al 7"].toFixed(2)}` },
        { periodo: "8 al 14", total: `$${data.periodos["8 al 14"].toFixed(2)}` },
        { periodo: "15 al 21", total: `$${data.periodos["15 al 21"].toFixed(2)}` },
        { periodo: "22 al 28", total: `$${data.periodos["22 al 28"].toFixed(2)}` },
        { periodo: "29 al fin de mes", total: `$${data.periodos["29 al fin de mes"].toFixed(2)}` },
        { periodo: "TOTAL VENTAS", total: `$${data.totalVentas.toFixed(2)}` },
        { periodo: "TOTAL INGRESOS", total: `$${data.totalIngresos.toFixed(2)}` },
        { periodo: "TOTAL EGRESOS", total: `-$${data.totalEgresos.toFixed(2)}` },
        { periodo: "TOTAL DEL MES", total: `$${data.totalDelMes.toFixed(2)}` },
        { periodo: "TOTAL DEL MES + SALDO ANTERIOR", total: `$${data.totalConSaldoAnterior.toFixed(2)}` },
    ];

    filas.forEach((fila) => {
        const tr = document.createElement("tr");
        const tdPeriodo = document.createElement("td");
        const tdTotal = document.createElement("td");
        tdPeriodo.textContent = fila.periodo;
        tdTotal.textContent = fila.total;
        tr.appendChild(tdPeriodo);
        tr.appendChild(tdTotal);
        tablaEfectivoBody.appendChild(tr);
    });
}

function actualizarTablaMovimientos(tablaId, movimientos) {
    const movEfectivoBody = document.getElementById(tablaId);
    movEfectivoBody.innerHTML = ""; // Limpiar la tabla

    movimientos.forEach((movimiento) => {
        const tr = document.createElement("tr");

        const tdFecha = document.createElement("td");
        const tdDescripcion = document.createElement("td");
        const tdUsuario = document.createElement("td");
        const tdTipo = document.createElement("td");
        const tdMetodo = document.createElement("td");
        const tdMonto = document.createElement("td");
        const tdAcciones = document.createElement("td"); // Columna nueva para acciones

        tdFecha.textContent = movimiento.fecha;
        tdDescripcion.textContent = movimiento.descripcion;
        tdUsuario.textContent = movimiento.usuario;
        tdTipo.textContent = movimiento.tipo;
        tdMetodo.textContent = movimiento.metodo;
        tdMonto.textContent = movimiento.monto;

        // Crear botón eliminar
        const botonEliminar = document.createElement("button");
        botonEliminar.textContent = "Eliminar";
        botonEliminar.className = "btn btn-danger btn-sm me-2";
        botonEliminar.onclick = function () {
            eliminarMovimiento(movimiento.id);
        };

        // Crear botón editar
        const botonEditar = document.createElement("button");
        botonEditar.textContent = "Editar";
        botonEditar.className = "btn btn-primary btn-sm mt-2 ml-2";

        botonEditar.onclick = function () {
            abrirModalEditar(movimiento.id);
        };

        // Agregar ambos botones a la columna
        tdAcciones.appendChild(botonEliminar);
        tdAcciones.appendChild(botonEditar);

        tr.appendChild(tdFecha);
        tr.appendChild(tdDescripcion);
        tr.appendChild(tdUsuario);
        tr.appendChild(tdTipo);
        tr.appendChild(tdMetodo);
        tr.appendChild(tdMonto);
        tr.appendChild(tdAcciones);

        movEfectivoBody.appendChild(tr);
    });
}





//////////////////////////////////////

function procesarDatosTarjeta(
    ventas,
    ingresosEgresos,
    ventasMesAnterior,
    ingresosEgresosMesAnterior
) {

    const tarjetaMesActual = {
        saldoAnterior: 0,
        periodos: {
            "1 al 7": 0,
            "8 al 14": 0,
            "15 al 21": 0,
            "22 al 28": 0,
            "29 al fin de mes": 0,
        },
        totalVentas: 0,
        totalIngresos: 0,
        totalEgresos: 0,
    };

    const movimientosTarjeta = [];
    const metodosTarjeta = ["CREDITO", "DEBITO"];

    // Función auxiliar para determinar el período de una fecha
    function obtenerPeriodo(fecha) {
        const dia = fecha.getDate();
        if (dia >= 1 && dia <= 7) return "1 al 7";
        if (dia >= 8 && dia <= 14) return "8 al 14";
        if (dia >= 15 && dia <= 21) return "15 al 21";
        if (dia >= 22 && dia <= 28) return "22 al 28";
        const ultimoDiaMes = new Date(fecha.getFullYear(), fecha.getMonth() + 1, 0).getDate();
        if (dia >= 29 && dia <= ultimoDiaMes) return "29 al fin de mes";
        return null;
    }

    // Calcular el saldo anterior de tarjetas
    let saldoAnteriorVentasTarjeta = 0;
    ventasMesAnterior.forEach((venta) => {
        if (venta && venta.pagos) {
            venta.pagos
                .filter((pago) => metodosTarjeta.includes(pago.metodo))
                .forEach((pago) => {
                    const montoConRecargo = pago.metodo === "CREDITO" ? pago.monto * 1.15 : pago.monto;
                    saldoAnteriorVentasTarjeta += montoConRecargo;
                });
        }
    });

    let saldoAnteriorIngresosTarjeta = 0;
    let saldoAnteriorEgresosTarjeta = 0;
    ingresosEgresosMesAnterior
        .filter((ie) => metodosTarjeta.includes(ie.metodoPago))
        .forEach((ie) => {
            saldoAnteriorIngresosTarjeta += ie.ingreso ? ie.monto : 0;
            saldoAnteriorEgresosTarjeta += ie.ingreso ? 0 : ie.monto;
        });

    tarjetaMesActual.saldoAnterior = saldoAnteriorVentasTarjeta + saldoAnteriorIngresosTarjeta - saldoAnteriorEgresosTarjeta;

    // Procesar ventas del mes actual con tarjeta
    ventas.forEach((venta) => {
        if (venta && venta.pagos) {
            venta.pagos
                .filter((pago) => metodosTarjeta.includes(pago.metodo))
                .forEach((pago) => {
                    const fechaVenta = new Date(venta.fecha);
                    const periodo = obtenerPeriodo(fechaVenta);
                    const montoConRecargo = pago.metodo === "CREDITO" ? pago.monto * 1.15 : pago.monto;
                    if (periodo) {
                        tarjetaMesActual.periodos[periodo] += montoConRecargo;
                    }
                    tarjetaMesActual.totalVentas += montoConRecargo;
                });
        }
    });

    // Procesar ingresos y egresos del mes actual con tarjeta
    ingresosEgresos
        .filter((ie) => metodosTarjeta.includes(ie.metodoPago))
        .forEach((ie) => {
            const fechaIE = new Date(ie.fecha);
            const periodo = obtenerPeriodo(fechaIE);
            const montoConSigno = ie.ingreso ? ie.monto : -ie.monto;
            if (periodo) {
                tarjetaMesActual.periodos[periodo] += montoConSigno;
            }
            if (ie.ingreso) {
                tarjetaMesActual.totalIngresos += ie.monto;
            } else {
                tarjetaMesActual.totalEgresos += ie.monto;
            }
            movimientosTarjeta.push({
                id: ie.id,
                fecha: fechaIE.toLocaleDateString(),
                descripcion: ie.descripcion,
                usuario: ie.usuario ? ie.usuario.nombre : "N/A",
                tipo: ie.ingreso ? "Ingreso" : "Egreso",
                metodo: ie.metodoPago,
                monto: montoConSigno.toFixed(2),
            });
        });

    const totalDelMesTarjeta =
        tarjetaMesActual.totalVentas +
        tarjetaMesActual.totalIngresos -
        tarjetaMesActual.totalEgresos;
    tarjetaMesActual.totalDelMes = totalDelMesTarjeta;
    tarjetaMesActual.totalConSaldoAnterior =
        tarjetaMesActual.saldoAnterior + totalDelMesTarjeta;

    return { tarjetaMesActual, movimientosTarjeta };
}

function actualizarTablaTarjeta(data) {
    const tablaTarjetaBody = document.getElementById("tablaTarjeta");
    tablaTarjetaBody.innerHTML = ""; // Limpiar la tabla

    const filas = [
        { periodo: "SALDO MES ANTERIOR", total: `$${data.saldoAnterior.toFixed(2)}` },
        { periodo: "1 al 7", total: `$${data.periodos["1 al 7"].toFixed(2)}` },
        { periodo: "8 al 14", total: `$${data.periodos["8 al 14"].toFixed(2)}` },
        { periodo: "15 al 21", total: `$${data.periodos["15 al 21"].toFixed(2)}` },
        { periodo: "22 al 28", total: `$${data.periodos["22 al 28"].toFixed(2)}` },
        { periodo: "29 al fin de mes", total: `$${data.periodos["29 al fin de mes"].toFixed(2)}` },
        { periodo: "TOTAL VENTAS", total: `$${data.totalVentas.toFixed(2)}` },
        { periodo: "TOTAL INGRESOS", total: `$${data.totalIngresos.toFixed(2)}` },
        { periodo: "TOTAL EGRESOS", total: `-$${data.totalEgresos.toFixed(2)}` },
        { periodo: "TOTAL DEL MES", total: `$${data.totalDelMes.toFixed(2)}` },
        { periodo: "TOTAL DEL MES + SALDO ANTERIOR", total: `$${data.totalConSaldoAnterior.toFixed(2)}` },
    ];

    filas.forEach((fila) => {
        const tr = document.createElement("tr");
        const tdPeriodo = document.createElement("td");
        const tdTotal = document.createElement("td");
        tdPeriodo.textContent = fila.periodo;
        tdTotal.textContent = fila.total;
        tr.appendChild(tdPeriodo);
        tr.appendChild(tdTotal);
        tablaTarjetaBody.appendChild(tr);
    });
}


function procesarDatosMercadoPagoSacha(
    ventas,
    ingresosEgresos,
    ventasMesAnterior,
    ingresosEgresosMesAnterior
) {


    const mpSachaMesActual = {
        saldoAnterior: 0,
        periodos: {
            "1 al 7": 0,
            "8 al 14": 0,
            "15 al 21": 0,
            "22 al 28": 0,
            "29 al fin de mes": 0,
        },
        totalVentas: 0,
        totalIngresos: 0,
        totalEgresos: 0,
    };

    const movimientosMpSacha = [];
    const metodoPago = "MERCADOPAGO_SAC"; // Ajustar el método de pago

    // Función auxiliar para determinar el período de una fecha
    function obtenerPeriodo(fecha) {
        const dia = fecha.getDate();
        if (dia >= 1 && dia <= 7) return "1 al 7";
        if (dia >= 8 && dia <= 14) return "8 al 14";
        if (dia >= 15 && dia <= 21) return "15 al 21";
        if (dia >= 22 && dia <= 28) return "22 al 28";
        const ultimoDiaMes = new Date(fecha.getFullYear(), fecha.getMonth() + 1, 0).getDate();
        if (dia >= 29 && dia <= ultimoDiaMes) return "29 al fin de mes";
        return null;
    }

    // Calcular el saldo anterior de MercadoPago Sacha
    ventasMesAnterior.forEach((venta) => {
        if (venta && venta.pagos) {
            venta.pagos
                .filter((pago) => pago.metodo === metodoPago)
                .forEach((pago) => {
                    mpSachaMesActual.saldoAnterior += pago.monto;
                });
        }
    });

    ingresosEgresosMesAnterior
        .filter((ie) => ie.metodoPago === metodoPago)
        .forEach((ie) => {
            mpSachaMesActual.saldoAnterior += ie.ingreso ? ie.monto : -ie.monto;
        });

    // Procesar ventas del mes actual de MercadoPago Sacha
    ventas.forEach((venta) => {
        if (venta && venta.pagos) {
            venta.pagos
                .filter((pago) => pago.metodo === metodoPago)
                .forEach((pago) => {
                    const fechaVenta = new Date(venta.fecha);
                    const periodo = obtenerPeriodo(fechaVenta);
                    if (periodo) {
                        mpSachaMesActual.periodos[periodo] += pago.monto;
                    }
                    mpSachaMesActual.totalVentas += pago.monto;
                });
        }
    });

    // Procesar ingresos y egresos del mes actual de MercadoPago Sacha
    ingresosEgresos
        .filter((ie) => ie.metodoPago === metodoPago)
        .forEach((ie) => {
            const fechaIE = new Date(ie.fecha);
            const periodo = obtenerPeriodo(fechaIE);
            const monto = ie.ingreso ? ie.monto : -ie.monto;
            if (periodo) {
                mpSachaMesActual.periodos[periodo] += monto;
            }
            if (ie.ingreso) {
                mpSachaMesActual.totalIngresos += ie.monto;
            } else {
                mpSachaMesActual.totalEgresos += ie.monto;
            }
            movimientosMpSacha.push({
                id: ie.id,
                fecha: fechaIE.toLocaleDateString(),
                descripcion: ie.descripcion,
                usuario: ie.usuario ? ie.usuario.nombre : "N/A",
                tipo: ie.ingreso ? "Ingreso" : "Egreso",
                monto: monto.toFixed(2),
                metodo: ie.metodoPago,
            });
        });

    const totalDelMesMpSacha =
        mpSachaMesActual.totalVentas +
        mpSachaMesActual.totalIngresos -
        mpSachaMesActual.totalEgresos;
    mpSachaMesActual.totalDelMes = totalDelMesMpSacha;
    mpSachaMesActual.totalConSaldoAnterior =
        mpSachaMesActual.saldoAnterior + totalDelMesMpSacha;


    return { mpSachaMesActual, movimientosMpSacha };
}

function actualizarTablaMpSacha(data) {
    const tablaMpSachaBody = document.getElementById("tablaMpSacha");
    tablaMpSachaBody.innerHTML = ""; // Limpiar la tabla

    const filas = [
        { periodo: "SALDO MES ANTERIOR", total: `$${data.saldoAnterior.toFixed(2)}` },
        { periodo: "1 al 7", total: `$${data.periodos["1 al 7"].toFixed(2)}` },
        { periodo: "8 al 14", total: `$${data.periodos["8 al 14"].toFixed(2)}` },
        { periodo: "15 al 21", total: `$${data.periodos["15 al 21"].toFixed(2)}` },
        { periodo: "22 al 28", total: `$${data.periodos["22 al 28"].toFixed(2)}` },
        { periodo: "29 al fin de mes", total: `$${data.periodos["29 al fin de mes"].toFixed(2)}` },
        { periodo: "TOTAL VENTAS", total: `$${data.totalVentas.toFixed(2)}` },
        { periodo: "TOTAL INGRESOS", total: `$${data.totalIngresos.toFixed(2)}` },
        { periodo: "TOTAL EGRESOS", total: `-$${data.totalEgresos.toFixed(2)}` },
        { periodo: "TOTAL DEL MES", total: `$${data.totalDelMes.toFixed(2)}` },
        { periodo: "TOTAL DEL MES + SALDO ANTERIOR", total: `$${data.totalConSaldoAnterior.toFixed(2)}` },
    ];

    filas.forEach((fila) => {
        const tr = document.createElement("tr");
        const tdPeriodo = document.createElement("td");
        const tdTotal = document.createElement("td");
        tdPeriodo.textContent = fila.periodo;
        tdTotal.textContent = fila.total;
        tr.appendChild(tdPeriodo);
        tr.appendChild(tdTotal);
        tablaMpSachaBody.appendChild(tr);
    });
}



function procesarDatosMercadoPagoVale(
    ventas,
    ingresosEgresos,
    ventasMesAnterior,
    ingresosEgresosMesAnterior
) {

    const mpValeMesActual = {
        saldoAnterior: 0,
        periodos: {
            "1 al 7": 0,
            "8 al 14": 0,
            "15 al 21": 0,
            "22 al 28": 0,
            "29 al fin de mes": 0,
        },
        totalVentas: 0,
        totalIngresos: 0,
        totalEgresos: 0,
    };

    const movimientosMpVale = [];
    const metodoPago = "MERCADOPAGO_VAL"; // Ajustar el método de pago

    // Función auxiliar para determinar el período de una fecha
    function obtenerPeriodo(fecha) {
        const dia = fecha.getDate();
        if (dia >= 1 && dia <= 7) return "1 al 7";
        if (dia >= 8 && dia <= 14) return "8 al 14";
        if (dia >= 15 && dia <= 21) return "15 al 21";
        if (dia >= 22 && dia <= 28) return "22 al 28";
        const ultimoDiaMes = new Date(fecha.getFullYear(), fecha.getMonth() + 1, 0).getDate();
        if (dia >= 29 && dia <= ultimoDiaMes) return "29 al fin de mes";
        return null;
    }

    // Calcular el saldo anterior de MercadoPago Valeria
    ventasMesAnterior.forEach((venta) => {
        if (venta && venta.pagos) {
            venta.pagos
                .filter((pago) => pago.metodo === metodoPago)
                .forEach((pago) => {
                    mpValeMesActual.saldoAnterior += pago.monto;
                });
        }
    });

    ingresosEgresosMesAnterior
        .filter((ie) => ie.metodoPago === metodoPago)
        .forEach((ie) => {
            mpValeMesActual.saldoAnterior += ie.ingreso ? ie.monto : -ie.monto;
        });

    // Procesar ventas del mes actual de MercadoPago Valeria
    ventas.forEach((venta) => {
        if (venta && venta.pagos) {
            venta.pagos
                .filter((pago) => pago.metodo === metodoPago)
                .forEach((pago) => {
                    const fechaVenta = new Date(venta.fecha);
                    const periodo = obtenerPeriodo(fechaVenta);
                    if (periodo) {
                        mpValeMesActual.periodos[periodo] += pago.monto;
                    }
                    mpValeMesActual.totalVentas += pago.monto;
                });
        }
    });

    // Procesar ingresos y egresos del mes actual de MercadoPago Valeria
    ingresosEgresos
        .filter((ie) => ie.metodoPago === metodoPago)
        .forEach((ie) => {
            const fechaIE = new Date(ie.fecha);
            const periodo = obtenerPeriodo(fechaIE);
            const monto = ie.ingreso ? ie.monto : -ie.monto;
            if (periodo) {
                mpValeMesActual.periodos[periodo] += monto;
            }
            if (ie.ingreso) {
                mpValeMesActual.totalIngresos += ie.monto;
            } else {
                mpValeMesActual.totalEgresos += ie.monto;
            }
            movimientosMpVale.push({
                id: ie.id,
                fecha: fechaIE.toLocaleDateString(),
                descripcion: ie.descripcion,
                usuario: ie.usuario ? ie.usuario.nombre : "N/A",
                tipo: ie.ingreso ? "Ingreso" : "Egreso",
                monto: monto.toFixed(2),
                metodo: ie.metodoPago,
            });
        });

    const totalDelMesMpVale =
        mpValeMesActual.totalVentas +
        mpValeMesActual.totalIngresos -
        mpValeMesActual.totalEgresos;
    mpValeMesActual.totalDelMes = totalDelMesMpVale;
    mpValeMesActual.totalConSaldoAnterior =
        mpValeMesActual.saldoAnterior + totalDelMesMpVale;

    return { mpValeMesActual, movimientosMpVale };
}

function actualizarTablaMpVale(data) {
    const tablaMpValeBody = document.getElementById("tablaMpVale");
    tablaMpValeBody.innerHTML = ""; // Limpiar la tabla

    const filas = [
        { periodo: "SALDO MES ANTERIOR", total: `$${data.saldoAnterior.toFixed(2)}` },
        { periodo: "1 al 7", total: `$${data.periodos["1 al 7"].toFixed(2)}` },
        { periodo: "8 al 14", total: `$${data.periodos["8 al 14"].toFixed(2)}` },
        { periodo: "15 al 21", total: `$${data.periodos["15 al 21"].toFixed(2)}` },
        { periodo: "22 al 28", total: `$${data.periodos["22 al 28"].toFixed(2)}` },
        { periodo: "29 al fin de mes", total: `$${data.periodos["29 al fin de mes"].toFixed(2)}` },
        { periodo: "TOTAL VENTAS", total: `$${data.totalVentas.toFixed(2)}` },
        { periodo: "TOTAL INGRESOS", total: `$${data.totalIngresos.toFixed(2)}` },
        { periodo: "TOTAL EGRESOS", total: `-$${data.totalEgresos.toFixed(2)}` },
        { periodo: "TOTAL DEL MES", total: `$${data.totalDelMes.toFixed(2)}` },
        { periodo: "TOTAL DEL MES + SALDO ANTERIOR", total: `$${data.totalConSaldoAnterior.toFixed(2)}` },
    ];

    filas.forEach((fila) => {
        const tr = document.createElement("tr");
        const tdPeriodo = document.createElement("td");
        const tdTotal = document.createElement("td");
        tdPeriodo.textContent = fila.periodo;
        tdTotal.textContent = fila.total;
        tr.appendChild(tdPeriodo);
        tr.appendChild(tdTotal);
        tablaMpValeBody.appendChild(tr);
    });
}

//Funcion para eliminar Ingreso Egreso
function eliminarMovimiento(id) {
    if (confirm("¿Estás seguro de que deseas eliminar este movimiento?")) {
        fetch('/ingresoegreso/eliminar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken   // Usar el nombre correcto de cabecera dinámicamente
            },
            body: JSON.stringify({ id: id })
        })
            .then(response => {
                if (response.ok) {
                    alert("Movimiento eliminado correctamente.");
                    // Después de eliminar, actualizar la tabla:
                    window.location.reload();
                } else {
                    alert("Error al eliminar el movimiento.");
                }
            })
            .catch(error => {
                console.error("Error:", error);
                alert("Error de conexión al intentar eliminar.");
            });
    }
}

function abrirModalEditar(id) {
    fetch(`${BASE_URL}/ingresoegreso/editar/${id}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Error al obtener el ingreso/egreso.");
            }
            return response.json();
        })
        .then(data => {
            // Setear los valores en el modal
            document.getElementById("tipoMovimiento").value = data.ingreso;
            document.getElementById("metodoPago").value = data.metodoPago;
            document.getElementById("usuario").value = data.usuarioId;
            document.getElementById("esAdelanto").checked = data.adelanto;
            document.getElementById("fecha").value = data.fecha.substring(0, 10); // solo yyyy-MM-dd
            document.getElementById("descripcion").value = data.descripcion;
            document.getElementById("monto").value = data.monto;

            // Guardamos el ID para luego hacer la actualización
            document.getElementById("formIngresoEgreso").dataset.editandoId = id;

            // Setear la variable global en true para indicar que estamos editando
            editando = true;

            // Cambiar el título del modal
            document.getElementById("modalIngresoEgresoLabel").textContent = "Editar Ingreso/Egreso";

            // Mostrar el modal
            const modal = new bootstrap.Modal(document.getElementById("modalIngresoEgreso"));
            modal.show();
        })
        .catch(error => {
            console.error("Error al abrir modal de edición:", error);
            alert("Hubo un error al cargar los datos del movimiento.");
        });
}



function abrirModalCrear() {
    // Limpiar cualquier campo del formulario si había datos de una edición anterior
    formIngresoEgreso.reset();
    document.getElementById("modalIngresoEgresoLabel").textContent = "Nuevo Ingreso/Egreso";

    // Setear la variable global en false para indicar que estamos creando
    editando = false;

    // Mostrar el modal
    const modal = new bootstrap.Modal(document.getElementById("modalIngresoEgreso"));
    modal.show();
}


