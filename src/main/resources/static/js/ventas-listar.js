import BASE_URL from '/js/config.js';

document.addEventListener("DOMContentLoaded", function () {

    // Elemento #fecha
    const fechaInput = document.getElementById("fecha");
    if (!fechaInput) {
        console.warn("El elemento #fecha no existe en esta página. Código no ejecutado.");
        return
    };

    const mesInput = document.getElementById('mes');
    const anioInput = document.getElementById('anio');
    const fechaActual = new Date();
    const mesActual = fechaActual.getMonth() + 1; // getMonth() devuelve de 0 a 11
    const anioActual = fechaActual.getFullYear();
  
    mesInput.value = mesActual;
    anioInput.value = anioActual;


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
        
        fetch(`${BASE_URL}/ventas/fechas?fecha=${fecha}`)
            .then(response => response.json())
            .then(data => {                
                document.getElementById("totalEfectivo").textContent = `$ ${(data.totalEfectivo || 0).toFixed(2)}`;
                document.getElementById("totalCredito").textContent = `$ ${(data.totalCredito || 0).toFixed(2)}`;
                document.getElementById("totalDebito").textContent = `$ ${(data.totalDebito || 0).toFixed(2)}`;
                document.getElementById("totalMercadoPagoVale").textContent = `$ ${(data.totalMercadoPagoVale || 0).toFixed(2)}`;
                document.getElementById("totalMercadoPagoSacha").textContent = `$ ${(data.totalMercadoPagoSacha || 0).toFixed(2)}`;
                document.getElementById("totalGeneral").textContent = `$ ${(data.totalGeneral || 0).toFixed(2)}`;
            })
            .catch(error => console.error("Error al obtener las ventas:", error));
    }

    document.getElementById('btnGenerarReporte').addEventListener('click', generarReporte);

    function generarReporte() {
        const mes = document.getElementById('mes').value;
        const anio = document.getElementById('anio').value;

        if (!mes || !anio) {
            alert('Por favor completa el mes y el año.');
            return;
        }

        fetch(`/ventas/reporte-pdf?mes=${mes}&anio=${anio}`, {
            method: 'GET'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al generar el reporte');
                }
                return response.blob();
            })
            .then(blob => {
                const url = window.URL.createObjectURL(new Blob([blob]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', `reporte_ventas_${anio}_${mes}.pdf`);
                document.body.appendChild(link);
                link.click();
                link.remove();
            })
            .catch(error => {
                console.error('Error:', error);
            });
    }





});