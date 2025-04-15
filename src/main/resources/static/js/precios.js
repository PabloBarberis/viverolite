// ============================
// CONFIGURACIÓN Y CONSTANTES
// ============================
import BASE_URL from '/js/config.js';

document.addEventListener("DOMContentLoaded", () => {
    // Elementos clave del formulario
    const tipoAccion = document.getElementById("tipoAccion");
    const tipoProducto = document.getElementById("tipoProducto");
    const interiorExteriorDiv = document.getElementById("idInteriorExteriorDiv"); 
    const interiorExteriorSelect = document.getElementById("idInteriorExterior"); 
    const marcaSelect = document.getElementById("marca");
    const porcentajeInput = document.getElementById("porcentaje");

    // ===========================
    // Validar que los decimales se ingresen con "."
    // ===========================
    porcentajeInput.addEventListener("input", function () {
        this.value = this.value.replace(",", "."); // Convierte automáticamente las comas en puntos
    });

    // ===========================
    // Mostrar/Ocultar Interior/Exterior si el producto es "Planta"
    // ===========================
    tipoProducto.addEventListener("change", (e) => {
        const selectedValue = e.target.value;

        if (selectedValue === "Planta") {
            interiorExteriorDiv.style.display = "block"; // Mostrar el select
        } else {
            interiorExteriorDiv.style.display = "none"; // Ocultar el select
            interiorExteriorSelect.value = ""; // Limpiar selección
        }
        
        // Cargar las marcas según el producto seleccionado
        cargarMarcas(selectedValue);
    });

    // ===========================
    // Función para cargar marcas según el tipo de producto seleccionado
    // ===========================
    const cargarMarcas = async (tipoProducto) => {
        try {
            const response = await fetch(`/precios/marcas?tipo=${tipoProducto}`);
            if (response.ok) {
                const marcas = await response.json();
                marcaSelect.innerHTML = '<option value="">Seleccionar Marca</option>';
                
                marcas.forEach(marca => {
                    const option = document.createElement("option");
                    option.value = marca;
                    option.textContent = marca;
                    marcaSelect.appendChild(option);
                });
            } else {
                console.error("Error al cargar las marcas:", response.status);
            }
        } catch (error) {
            console.error("Error al realizar la solicitud:", error);
        }
    };

    // ===========================
    // Evento para el formulario unificado
    // ===========================
    document.getElementById("formPrecios").addEventListener("submit", function (event) {
        event.preventDefault(); // Evitar el envío tradicional

        // Obtener valores del formulario
        const accion = tipoAccion.value; // Aumento o Descuento
        
        const producto = tipoProducto.value;
        const porcentaje = parseFloat(porcentajeInput.value); // Asegurar que sea un número válido
        const marca = marcaSelect.value;
        const interiorExterior = interiorExteriorSelect.value || "";

        // Asegurar valores CSRF
        let csrfToken = $('meta[name="_csrf"]').attr('content');
        let csrfHeader = $('meta[name="_csrf_header"]').attr('content');

        if (!csrfToken || !csrfHeader) {
            alert("⚠️ Error: No se encontró el token CSRF.");
            return;
        }

        // Validar que el porcentaje sea un número válido
        if (isNaN(porcentaje) || porcentaje <= 0) {
            alert("⚠️ Ingrese un porcentaje válido mayor a 0.");
            return;
        }

        // Confirmar con el usuario
        if (!confirm(`¿Aplicar ${accion} del ${porcentaje}% para ${producto} (${interiorExterior || 'Todos'})?`)) {
            return;
        }

        console.log("Datos enviados:", JSON.stringify({ tipoAccion: accion, tipoProducto: producto, porcentaje, marca, interiorExterior }));


        // Nueva URL unificada
        let url = `${BASE_URL}/precios/aumentoDescuento`;

        // Enviar datos al servidor con acción incluida
        fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ tipoAccion: accion, tipoProducto: producto, porcentaje, marca, interiorExterior })
        })
        .then(response => {
            if (response.status !== 200) throw new Error("❌ Error en la solicitud: " + response.statusText);
            return response.text();
        })
        .then(data => {
            alert(`✅ ${accion} aplicado con éxito: ${data}`);
        })
        .catch(error => {
            console.error("❌ Error:", error);
            alert("⚠️ No se pudo aplicar el cambio.");
        });
    });
});
