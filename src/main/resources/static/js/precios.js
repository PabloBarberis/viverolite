// ============================
// CONFIGURACIÓN Y CONSTANTES
// ============================
import BASE_URL from '/js/config.js';

///////////////////////////////-AUMENTO DE PRECIOS-//////////////////////////

document.getElementById("formAumento").addEventListener("submit", function (event) {
    event.preventDefault(); // Evitar que el formulario se envíe de la manera tradicional

    // Asegurarse de que el token y los valores CSRF están disponibles
    let csrfToken = $('meta[name="_csrf"]').attr('content');
    let csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    if (!csrfToken || !csrfHeader) {
        alert("⚠️ Error: No se encontró el token CSRF.");
        return;
    }

    const tipoProducto = document.getElementById("tipoProductoAumento").value;
    const porcentaje = document.getElementById("porcentajeAumento").value;
    const marca = document.getElementById("marcaAumento").value;

    // Confirmar con el usuario antes de aplicar el aumento
    if (!confirm(`¿Estás seguro de que deseas aplicar un AUMENTO del ${porcentaje}% para los productos de tipo ${tipoProducto} y marca ${marca}?`)) {
        return; // Cancelar si el usuario no confirma
    }

    fetch(`${BASE_URL}/precios/aumentar`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken // Agregar el CSRF token al header
        },

        body: JSON.stringify({tipoProducto, porcentaje, marca}) // Enviar los datos como JSON
    })
            .then(response => {
                if (response.status !== 200) {  // Verificar si el estado es 200
                    throw new Error("❌ Error en la solicitud: " + response.statusText);
                }
                return response.text(); // Leer la respuesta como texto
            })
            .then(data => {
                console.log("Respuesta del servidor:", data);  // Loguear los datos
                alert("✅ Aumento aplicado con éxito: " + data);
            })
            .catch(error => {
                console.error("❌ Error:", error);
                alert("⚠️ No se pudo aplicar el aumento. Verifica el token y los datos.");
            });
});



///////////////////////////////-DESCUENTO DE PRECIOS-//////////////////////////


document.getElementById("formDescuento").addEventListener("submit", function (event) {
    event.preventDefault(); // Evitar que el formulario se envíe de la manera tradicional

    // Asegurarse de que los valores CSRF estén disponibles
    let csrfToken = $('meta[name="_csrf"]').attr('content');
    let csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    if (!csrfToken || !csrfHeader) {
        alert("⚠️ Error: No se encontró el token CSRF.");
        return;
    }

    // Obtener los datos del formulario
    const tipoProducto = document.getElementById("tipoProductoDescuento").value;
    const porcentaje = document.getElementById("porcentajeDescuento").value;
    const marca = document.getElementById("marcaDescuento").value;

    // Confirmar con el usuario antes de aplicar el descuento
    if (!confirm(`¿Estás seguro de que deseas aplicar un DESCUENTO del ${porcentaje}% para los productos de tipo ${tipoProducto}?`)) {
        return; // Cancelar si el usuario no confirma
    }

    // Realizar la solicitud fetch para aplicar el descuento
    fetch(`${BASE_URL}/precios/descuento`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken // Incluir el token CSRF en el encabezado
        },
        body: JSON.stringify({tipoProducto, porcentaje, marca}) // Enviar los datos como JSON
    })
            .then(response => {
                if (response.status === 200) {
                    return response.text(); // Leer la respuesta como texto
                } else {
                    throw new Error("❌ Error al aplicar el descuento.");
                }
            })
            .then(data => {
                alert("✅ Descuento aplicado con éxito: " + data); // Mostrar mensaje del servidor
            })
            .catch(error => {
                console.error("❌ Error:", error);
                alert("⚠️ No se pudo aplicar el descuento. Verifica los datos.");
            });
});

document.addEventListener('DOMContentLoaded', () => {
    
    // Función para cargar marcas según el tipo de producto seleccionado
    const cargarMarcas = async (tipoProducto, idSelectMarca) => {
        try {
            const response = await fetch(`/precios/marcas?tipo=${tipoProducto}`);
    
            if (response.ok) {
                const marcas = await response.json();
                const select = document.getElementById(idSelectMarca);

                // Limpiar opciones previas
                select.innerHTML = '<option value="">Seleccionar Marca</option>';

                // Agregar opciones de marcas
                marcas.forEach(marca => {
                    const option = document.createElement('option');
                    option.value = marca;
                    option.textContent = marca;
                    select.appendChild(option);
                });
            } else {
                console.error("Error al cargar las marcas:", response.status);
            }
        } catch (error) {
            console.error("Error al realizar la solicitud:", error);
        }
    };

    // Evento: cambiar tipo de producto en el formulario de aumento
    document.getElementById('tipoProductoAumento').addEventListener('change', (e) => {
        cargarMarcas(e.target.value, 'marcaAumento');
    });

    // Evento: cambiar tipo de producto en el formulario de descuento
    document.getElementById('tipoProductoDescuento').addEventListener('change', (e) => {
        cargarMarcas(e.target.value, 'marcaDescuento');
    });
});
