document.addEventListener("DOMContentLoaded", function () {
    const tipoProducto = document.getElementById("tipoProducto")?.value;
    const marcaSelect = document.getElementById("marcaPDF");
    const botonPDF = document.getElementById("generarPDFGrow");

    if (!tipoProducto) {
        console.error("No se encontró el tipo de producto.");
        return;
    }

    // Cargar marcas
    fetch(`/precios/marcas?tipo=${tipoProducto}`)
        .then(response => {
            if (!response.ok)
                throw new Error("Error al obtener las marcas");
            return response.json();
        })
        .then(marcas => {
            marcas.forEach(marca => {
                const option = document.createElement("option");
                option.value = marca;
                option.textContent = marca;
                marcaSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error("Error al cargar marcas:", error);
        });

    // Generar PDF
    botonPDF.addEventListener("click", function () {
        const marcaSeleccionada = marcaSelect.value;
        let url = `/producto/pdf?tipo=${encodeURIComponent(tipoProducto)}`;

        if (marcaSeleccionada) {
            url += `&marca=${encodeURIComponent(marcaSeleccionada)}`;
        }

        fetch(url, { method: 'GET' })
            .then(response => {
                if (!response.ok)
                    throw new Error("Error al generar el PDF");
                return response.blob();
            })
            .then(blob => {
                const urlBlob = window.URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = urlBlob;
                link.download = `productos-${tipoProducto.toLowerCase()}-${marcaSeleccionada || "completo"}.pdf`;
                link.click();
            })
            .catch(error => {
                console.error("Error al generar el PDF:", error);
                alert("Hubo un problema al generar el PDF. Intenta nuevamente.");
            });
    });
});
