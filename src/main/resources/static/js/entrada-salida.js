import BASE_URL from '/js/config.js';

// Obtener los tokens CSRF de las meta etiquetas
const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute("content") : null;
const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute("content") : null;

document.addEventListener("DOMContentLoaded", function () {


    const fechaInput = document.getElementById("fecha");

    // 📌 Obtener la fecha actual en formato YYYY-MM-DD
    const hoy = new Date();
    const year = hoy.getFullYear();
    const month = (hoy.getMonth() + 1).toString().padStart(2, "0"); // Mes en 2 dígitos
    const day = hoy.getDate().toString().padStart(2, "0"); // Día en 2 dígitos

    const fechaActual = `${year}-${month}-${day}`;
    fechaInput.value = fechaActual; // ✅ Asignar la fecha actual al input

    console.log("Fecha preseleccionada:", fechaActual); // 🔍 Verifica en consola


    const metodoPagoSelect = document.getElementById("metodoPago");
    const usuarioSelect = document.getElementById("usuario");
    const formIngresoEgreso = document.getElementById("formIngresoEgreso");

    if (!csrfToken || !csrfHeader) {
        console.error("CSRF token o header no encontrados.");
        return;
    }

    // Cargar métodos de pago
    fetch(`${BASE_URL}/ingresoegreso/metodos-pago`, {
        method: "GET",
        headers: { [csrfHeader]: csrfToken }
    })
        .then(response => response.json())
        .then(data => {
            metodoPagoSelect.innerHTML = data.map(mp => `<option value="${mp}">${mp}</option>`).join("");
        })
        .catch(error => console.error("Error cargando métodos de pago:", error));

    // Cargar usuarios
    fetch(`${BASE_URL}/usuarios/listar`, {
        method: "GET",
        headers: { [csrfHeader]: csrfToken }
    })
        .then(response => response.json())
        .then(data => {
            usuarioSelect.innerHTML = data.map(user => `<option value="${user.id}">${user.nombre}</option>`).join("");
        })
        .catch(error => console.error("Error cargando usuarios:", error));

    // Manejo del formulario
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
        console.log(esAdelanto);
        console.log(ingreso);
    
        // 📌 Convertir fecha correctamente a LocalDateTime
        if (fecha) {
            const ahora = new Date(); // Obtener la hora actual
            const hora = ahora.getHours().toString().padStart(2, '0');   // HH
            const minutos = ahora.getMinutes().toString().padStart(2, '0'); // mm
            const segundos = ahora.getSeconds().toString().padStart(2, '0'); // ss
    
            // Formato correcto: YYYY-MM-DDTHH:mm:ss
            fecha = `${fecha}T${hora}:${minutos}:${segundos}`;
        }
        
       
    
        // Construir el objeto con los datos del formulario
        const movimiento = {
            ingreso,
            metodoPago,
            usuarioId,
            fecha, // Ahora en formato LocalDateTime
            descripcion,
            monto,
            adelanto: esAdelanto // Añadir si es un adelanto
        };

        console.log("Fecha enviada:", fecha); // 🔍 Verifica en consola

        fetch(`${BASE_URL}/ingresoegreso/guardar`, {
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


    const selectMes = document.getElementById("mes");
    const selectAnio = document.getElementById("anio");
    const tablaEfectivo = document.getElementById("tablaEfectivo");
    const movEfectivo = document.getElementById("movEfectivo");
    const tablaTarjeta = document.getElementById("tablaTarjeta");
    const movTarjeta = document.getElementById("movTarjeta");
    const tablaMpVale = document.getElementById("tablaMpVale");
    const movMpVale = document.getElementById("movMpVale");
    const tablaMpSacha = document.getElementById("tablaMpSacha");
    const movMpSacha = document.getElementById("movMpSacha");


    function llenarSelectores() {
        console.log("SELECTORES FUERA")
        // 🟢 Llenar selector de mes
        const meses = [
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ];
        let mesActual = new Date().getMonth() + 1;
        let anioActual = new Date().getFullYear();

        selectMes.innerHTML = meses.map((mes, index) =>
            `<option value="${index + 1}" ${index + 1 === mesActual ? "selected" : ""}>${mes}</option>`
        ).join("");

        // 🟢 Llenar selector de año (de 2025 a 2030)
        let opcionesAnio = "";
        for (let i = 2025; i <= 2030; i++) {
            opcionesAnio += `<option value="${i}" ${i === anioActual ? "selected" : ""}>${i}</option>`;
        }
        selectAnio.innerHTML = opcionesAnio;


        // 🔴 Llamar al endpoint cuando se carguen los select
        obtenerIngresosEfectivo(mesActual, anioActual);
    }

    function obtenerMesAnterior(mes, anio) {
        if (mes === 1) {
            return { mes: 12, anio: anio - 1 };
        }
        return { mes: mes - 1, anio: anio };
    }


    function obtenerIngresosEfectivo(mes, anio) {
        console.log("OBTENER INGRESOS LARGO");
        // Calcular el mes y año anterior
        let mesAnterior = mes - 1;
        let anioAnterior = anio;
        if (mesAnterior === 0) {
            mesAnterior = 12;
            anioAnterior--;
        }

        // Obtener los datos del mes seleccionado
        let urlActual = `${BASE_URL}/ventas/ventas-gastos?mes=${mes}&anio=${anio}`;
        let urlAnterior = `${BASE_URL}/ventas/ventas-gastos?mes=${mesAnterior}&anio=${anioAnterior}`;

        Promise.all([
            fetch(urlActual).then(response => response.json()),
            fetch(urlAnterior).then(response => response.json())
        ])
            .then(([dataActual, dataAnterior]) => {
                console.log("Datos actuales:", dataActual);
                console.log("Datos del mes anterior:", dataAnterior);
                actualizarTabla(dataActual, dataAnterior);
            })
            .catch(error => {
                console.error("Error al obtener los datos:", error);
            });
    }


    function actualizarTabla(dataActual, dataAnterior) {
        // Limpiar contenido de las tablas
        tablaEfectivo.innerHTML = "";
        movEfectivo.innerHTML = "";
        tablaTarjeta.innerHTML = "";
        movTarjeta.innerHTML = "";
        tablaMpVale.innerHTML = "";
        movMpVale.innerHTML = "";
        tablaMpSacha.innerHTML = "";
        movMpSacha.innerHTML = "";


        const periodos = ["1 al 7", "8 al 14", "15 al 21", "22 al 28", "29 al fin de mes"];
        const totalesEfectivo = [0, 0, 0, 0, 0];
        const totalesTarjeta = [0, 0, 0, 0, 0];
        const totalesMpVale = [0, 0, 0, 0, 0];
        const totalesMpSacha = [0, 0, 0, 0, 0];

        let saldoMesAnteriorEfectivo = 0;
        let saldoMesAnteriorTarjeta = 0;
        let saldoMesAnteriorMpVale = 0;
        let saldoMesAnteriorMpSacha = 0;
        let totalVentasEfectivo = 0, totalVentasTarjeta = 0;
        let totalIngresosEfectivo = 0, totalIngresosTarjeta = 0;
        let totalEgresosEfectivo = 0, totalEgresosTarjeta = 0;

        let totalVentasMpVale = 0, TotalVentasMpSacha = 0;
        let totalIngresosMpVale = 0, totalIngresosMpSacha = 0;
        let totalEgresosMpVale = 0, totalEgresosMpSacha = 0;


        // 📌 Calcular ventas en efectivo y tarjeta del mes actual
        dataActual.ventas.forEach(venta => {
            let fecha = new Date(venta.fecha);
            let dia = fecha.getDate();
            let index = obtenerPeriodoIndex(dia);

            if (venta.metodoPago === "EFECTIVO") {
                totalesEfectivo[index] += venta.total;
                totalVentasEfectivo += venta.total;
            } else if (venta.metodoPago === "DEBITO" || venta.metodoPago === "CREDITO") {
                totalesTarjeta[index] += venta.total;
                totalVentasTarjeta += venta.total;
            } else if (venta.metodoPago === "MERCADOPAGO_VAL") {
                totalesMpVale[index] += venta.total;
                totalVentasMpVale += venta.total;
            } else if (venta.metodoPago === "MERCADOPAGO_SAC") {
                totalesMpSacha[index] += venta.total;
                TotalVentasMpSacha += venta.total;
            }


        });

        // 📌 Calcular ingresos y egresos en efectivo y tarjeta
        dataActual.ingresosEgresos.forEach(ie => {
            let fecha = new Date(ie.fecha);
            let dia = fecha.getDate();
            let index = obtenerPeriodoIndex(dia);
            let tipo = ie.ingreso ? "INGRESO" : "EGRESO";

            if (ie.metodoPago === "EFECTIVO") {
                if (ie.ingreso) {
                    totalesEfectivo[index] += ie.monto;
                    totalIngresosEfectivo += ie.monto;
                } else {
                    totalesEfectivo[index] -= ie.monto;
                    totalEgresosEfectivo += ie.monto;
                }

                // 🟢 Agregar fila a la tabla de movimientos en efectivo
                let fila = `
                <tr>
                    <td>${fecha.toLocaleDateString()}</td>
                    <td>${ie.descripcion || "Ingreso/Egreso"}</td>
                    <td>${ie.usuario?.nombre || "N/A"}</td>
                    <td>${tipo}</td>
                    <td>$${ie.monto.toFixed(2)}</td>
                </tr>`;
                movEfectivo.innerHTML += fila;
            } else if (ie.metodoPago === "DEBITO" || ie.metodoPago === "CREDITO") {
                if (ie.ingreso) {
                    totalesTarjeta[index] += ie.monto;
                    totalIngresosTarjeta += ie.monto;
                } else {
                    totalesTarjeta[index] -= ie.monto;
                    totalEgresosTarjeta += ie.monto;
                }

                // 🟢 Agregar fila a la tabla de movimientos con tarjeta
                let fila = `
                <tr>
                    <td>${fecha.toLocaleDateString()}</td>
                    <td>${ie.descripcion || "Ingreso/Egreso"}</td>
                    <td>${ie.usuario?.nombre || "N/A"}</td>
                    <td>${tipo}</td>
                <td>${ie.metodoPago}</td>
                    <td>$${ie.monto.toFixed(2)}</td>
                </tr>`;
                movTarjeta.innerHTML += fila;
            } else if (ie.metodoPago === "MERCADOPAGO_VAL") {
                if (ie.ingreso) {
                    totalesMpVale[index] += ie.monto;
                    totalIngresosMpVale += ie.monto;
                } else {
                    totalesMpVale[index] -= ie.monto;
                    totalEgresosMpVale += ie.monto;
                }

                // 🟢 Agregar fila a la tabla de movimientos MercadoPago Valeria
                let fila = `
                <tr>
                    <td>${fecha.toLocaleDateString()}</td>
                    <td>${ie.descripcion || "Ingreso/Egreso"}</td>
                    <td>${ie.usuario?.nombre || "N/A"}</td>
                    <td>${tipo}</td>                
                    <td>$${ie.monto.toFixed(2)}</td>
                </tr>`;
                movMpVale.innerHTML += fila;
            } else if (ie.metodoPago === "MERCADOPAGO_SAC") {
                if (ie.ingreso) {
                    totalesMpSacha[index] += ie.monto;
                    totalIngresosMpSacha += ie.monto;
                } else {
                    totalesMpSacha[index] -= ie.monto;
                    totalEgresosMpSacha += ie.monto;
                }

                // 🟢 Agregar fila a la tabla de movimientos MercadoPago Sacha
                let fila = `
                <tr>
                    <td>${fecha.toLocaleDateString()}</td>
                    <td>${ie.descripcion || "Ingreso/Egreso"}</td>
                    <td>${ie.usuario?.nombre || "N/A"}</td>
                    <td>${tipo}</td>
                    <td>$${ie.monto.toFixed(2)}</td>
                </tr>`;
                movMpSacha.innerHTML += fila;
            }


        });

        // 📌 Calcular saldo del mes anterior (ventas e ingresos)
        dataAnterior.ventas.forEach(venta => {
            if (venta.metodoPago === "EFECTIVO") {
                saldoMesAnteriorEfectivo += venta.total;
            } else if (venta.metodoPago === "DEBITO" || venta.metodoPago === "CREDITO") {
                saldoMesAnteriorTarjeta += venta.total;
            } else if (venta.metodoPago === "MERCADOPAGO_VAL") {
                saldoMesAnteriorMpVale += venta.total;
            } else if (venta.metodoPago === "MERCADOPAGO_SAC") {
                saldoMesAnteriorMpSacha += venta.total;
            }

        });


        dataAnterior.ingresosEgresos.forEach(ie => {
            if (ie.ingreso) {
                if (ie.metodoPago === "EFECTIVO") {
                    saldoMesAnteriorEfectivo += ie.monto;
                } else if (ie.metodoPago === "DEBITO" || ie.metodoPago === "CREDITO") {
                    saldoMesAnteriorTarjeta += ie.monto;
                } else if (ie.metodoPago === "MERCADOPAGO_VAL") {
                    saldoMesAnteriorMpVale += ie.monto;
                } else if (ie.metodoPago === "MERCADOPAGO_SAC") {
                    saldoMesAnteriorMpSacha += ie.monto;
                }
            }
        });

        // 🟢 Agregar filas a tablaEfectivo
        tablaEfectivo.innerHTML += `<tr style="font-weight: bold;">
        <td>SALDO MES ANTERIOR</td>
        <td>$${saldoMesAnteriorEfectivo.toFixed(2)}</td>
    </tr>`;
        periodos.forEach((periodo, index) => {
            tablaEfectivo.innerHTML += `<tr>
            <td>${periodo}</td>
            <td>$${totalesEfectivo[index].toFixed(2)}</td>
        </tr>`;
        });

        let totalMesEfectivo = totalVentasEfectivo + totalIngresosEfectivo - totalEgresosEfectivo;
        let totalMesSaldoAnteriorEfectivo = totalMesEfectivo + saldoMesAnteriorEfectivo;

        tablaEfectivo.innerHTML += `
    <tr style="font-weight: bold;"><td>TOTAL VENTAS</td><td>$${totalVentasEfectivo.toFixed(2)}</td></tr>
    <tr style="font-weight: bold;"><td>TOTAL INGRESOS</td><td>$${totalIngresosEfectivo.toFixed(2)}</td></tr>
    <tr style="font-weight: bold; color: red;"><td>TOTAL EGRESOS</td><td>-$${totalEgresosEfectivo.toFixed(2)}</td></tr>
    <tr style="font-weight: bold;"><td>TOTAL DEL MES</td><td>$${totalMesEfectivo.toFixed(2)}</td></tr>
    <tr style="font-weight: bold;"><td>TOTAL DEL MES + SALDO ANTERIOR</td><td>$${totalMesSaldoAnteriorEfectivo.toFixed(2)}</td></tr>`;

        // 🟢 Agregar filas a tablaTarjeta
        tablaTarjeta.innerHTML += `<tr style="font-weight: bold;">
        <td>SALDO MES ANTERIOR</td>
        <td>$${saldoMesAnteriorTarjeta.toFixed(2)}</td>
        </tr>`;
        periodos.forEach((periodo, index) => {
            tablaTarjeta.innerHTML += `<tr>
            <td>${periodo}</td>
            <td>$${totalesTarjeta[index].toFixed(2)}</td>
        </tr>`;
        });

        let totalMesTarjeta = totalVentasTarjeta + totalIngresosTarjeta - totalEgresosTarjeta;
        let totalMesSaldoAnteriorTarjeta = totalMesTarjeta + saldoMesAnteriorTarjeta;

        tablaTarjeta.innerHTML += `
    <tr style="font-weight: bold;"><td>TOTAL VENTAS</td><td>$${totalVentasTarjeta.toFixed(2)}</td></tr>
    <tr style="font-weight: bold;"><td>TOTAL INGRESOS</td><td>$${totalIngresosTarjeta.toFixed(2)}</td></tr>
    <tr style="font-weight: bold; color: red;"><td>TOTAL EGRESOS</td><td>-$${totalEgresosTarjeta.toFixed(2)}</td></tr>
    <tr style="font-weight: bold;"><td>TOTAL DEL MES</td><td>$${totalMesTarjeta.toFixed(2)}</td></tr>
    <tr style="font-weight: bold;"><td>TOTAL DEL MES + SALDO ANTERIOR</td><td>$${totalMesSaldoAnteriorTarjeta.toFixed(2)}</td></tr>`;


        // 🟢 Agregar filas a tablaMpVale
        tablaMpVale.innerHTML += `<tr style="font-weight: bold;">
    <td>SALDO MES ANTERIOR</td>
    <td>$${saldoMesAnteriorMpVale.toFixed(2)}</td>
    </tr>`;
        periodos.forEach((periodo, index) => {
            tablaMpVale.innerHTML += `<tr>
        <td>${periodo}</td>
        <td>$${totalesMpVale[index].toFixed(2)}</td>
    </tr>`;
        });

        let totalMesMpVale = totalVentasMpVale + totalIngresosMpVale - totalEgresosMpVale;
        let totalMesSaldoAnteriorMpVale = totalMesMpVale + saldoMesAnteriorMpVale;

        tablaMpVale.innerHTML += `
<tr style="font-weight: bold;"><td>TOTAL VENTAS</td><td>$${totalVentasMpVale.toFixed(2)}</td></tr>
<tr style="font-weight: bold;"><td>TOTAL INGRESOS</td><td>$${totalIngresosMpVale.toFixed(2)}</td></tr>
<tr style="font-weight: bold; color: red;"><td>TOTAL EGRESOS</td><td>-$${totalEgresosMpVale.toFixed(2)}</td></tr>
<tr style="font-weight: bold;"><td>TOTAL DEL MES</td><td>$${totalMesMpVale.toFixed(2)}</td></tr>
<tr style="font-weight: bold;"><td>TOTAL DEL MES + SALDO ANTERIOR</td><td>$${totalMesSaldoAnteriorMpVale.toFixed(2)}</td></tr>`;


        // 🟢 Agregar filas a tablaMpSacha
        tablaMpSacha.innerHTML += `<tr style="font-weight: bold;">
<td>SALDO MES ANTERIOR</td>
<td>$${saldoMesAnteriorMpSacha.toFixed(2)}</td>
</tr>`;
        periodos.forEach((periodo, index) => {
            tablaMpSacha.innerHTML += `<tr>
    <td>${periodo}</td>
    <td>$${totalesMpSacha[index].toFixed(2)}</td>
</tr>`;
        });

        let totalMesMpSacha = TotalVentasMpSacha + totalIngresosMpSacha - totalEgresosMpSacha;
        let totalMesSaldoAnteriorMpSacha = totalMesMpSacha + saldoMesAnteriorMpSacha;

        tablaMpSacha.innerHTML += `
<tr style="font-weight: bold;"><td>TOTAL VENTAS</td><td>$${TotalVentasMpSacha.toFixed(2)}</td></tr>
<tr style="font-weight: bold;"><td>TOTAL INGRESOS</td><td>$${totalIngresosMpSacha.toFixed(2)}</td></tr>
<tr style="font-weight: bold; color: red;"><td>TOTAL EGRESOS</td><td>-$${totalEgresosMpSacha.toFixed(2)}</td></tr>
<tr style="font-weight: bold;"><td>TOTAL DEL MES</td><td>$${totalMesMpSacha.toFixed(2)}</td></tr>
<tr style="font-weight: bold;"><td>TOTAL DEL MES + SALDO ANTERIOR</td><td>$${totalMesSaldoAnteriorMpSacha.toFixed(2)}</td></tr>`;

    }

    function obtenerPeriodoIndex(dia) {
        if (dia >= 1 && dia <= 7)
            return 0;
        if (dia >= 8 && dia <= 14)
            return 1;
        if (dia >= 15 && dia <= 21)
            return 2;
        if (dia >= 22 && dia <= 28)
            return 3;
        return 4;
    }

    function actualizarTotales() {

        document.getElementById("totalEfectivo").textContent = `$ ${data.totalMesEfectivo.toFixed(2)}`;
        document.getElementById("totalCredito").textContent = `$ ${data.totalCredito.toFixed(2)}`;
        document.getElementById("totalDebito").textContent = `$ ${data.totalDebito.toFixed(2)}`;
        document.getElementById("totalMercadoPago").textContent = `$ ${data.totalMercadoPago.toFixed(2)}`;
        document.getElementById("totalGeneral").textContent = `$ ${data.totalGeneral.toFixed(2)}`;

    }

    // Evento para actualizar datos cuando cambia el mes o el año
    selectMes.addEventListener("change", () => {
        obtenerIngresosEfectivo(selectMes.value, selectAnio.value);
    });

    selectAnio.addEventListener("change", () => {
        obtenerIngresosEfectivo(selectMes.value, selectAnio.value);
    });

    // Cargar selectores y datos al inicio
    llenarSelectores();

});
