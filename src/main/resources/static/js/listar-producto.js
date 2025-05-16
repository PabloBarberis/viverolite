document.addEventListener("DOMContentLoaded", function () {
    const tipoSelect = document.getElementById("tipoFiltro");
    const nombreInput = document.getElementById("nombreFiltro");
    const marcaSelect = document.getElementById("marcaFiltro");
    const botonBuscar = document.getElementById("btnBuscar");
    const botonPDF = document.getElementById("generarPDF");

    // ✅ Buscar productos con filtros y actualizar la tabla
    botonBuscar.addEventListener("click", function () {
        cargarProductos();

        const tipoSeleccionado = tipoSelect.value;
        const tituloSpan = document.getElementById("tipoSeleccionado");

        if (tipoSeleccionado) {
            tituloSpan.textContent = `: ${tipoSeleccionado}`;
            tituloSpan.classList.add("text-warning");
        } else {
            tituloSpan.textContent = "";
            tituloSpan.classList.remove("text-warning");
        }
    });

    document.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
            e.preventDefault(); // Evita el comportamiento por defecto (como enviar formularios)
            botonBuscar.click(); // Simula el clic en el botón de buscar
        }
    });
    

    // ✅ Generar PDF con filtros aplicados
    botonPDF.addEventListener("click", function () {
        const tipoSeleccionado = tipoSelect?.value || null;
        const nombreSeleccionado = nombreInput?.value.trim() || null;
        const marcaSeleccionada = marcaSelect?.value || null;

        console.log("Tipo:", tipoSeleccionado);
        console.log("Nombre:", nombreSeleccionado);
        console.log("Marca:", marcaSeleccionada);

        let url = "/productos/pdf";

        // ✅ Construir la URL solo con los parámetros que tienen valores
        const params = new URLSearchParams();
        if (tipoSeleccionado) params.append("tipo", tipoSeleccionado);
        if (nombreSeleccionado) params.append("nombre", nombreSeleccionado);
        if (marcaSeleccionada) params.append("marca", marcaSeleccionada);

        // ✅ Agregar los parámetros a la URL si hay alguno presente
        if (params.toString()) url += `?${params.toString()}`;

        fetch(url, { method: "GET" })
            .then(response => response.ok ? response.blob() : Promise.reject("Error al generar el PDF"))
            .then(blob => {
                const urlBlob = window.URL.createObjectURL(blob);
                const link = document.createElement("a");
                link.href = urlBlob;
                link.download = `productos-${tipoSeleccionado || "todos"}-${marcaSeleccionada || "todas"}-${nombreSeleccionado || "todos"}.pdf`;
                link.click();
            })
            .catch(error => {
                console.error("Error al generar el PDF:", error);
                alert("Hubo un problema al generar el PDF. Intenta nuevamente.");
            });
    });

});

// ✅ Función para cargar productos según los filtros
function cargarProductos(page = 0) {
    const tipo = document.getElementById("tipoFiltro").value;
    const nombre = document.getElementById("nombreFiltro").value;
    const marca = document.getElementById("marcaFiltro").value;
    const size = 15;

    fetch(`/productos/listar-datos?page=${page}&size=${size}&nombre=${nombre}&marca=${marca}&tipo=${tipo}`)
        .then(response => response.json())
        .then(data => {
            actualizarTabla(data.content);
            actualizarPaginacion(data.number, data.totalPages);
        })
        .catch(error => console.error("Error cargando productos:", error));
}


function actualizarTabla(productos) {
    const tablaBody = document.getElementById("tablaProductos");
    tablaBody.innerHTML = ""; // Limpiar la tabla antes de insertar nuevos datos

    productos.forEach(producto => {
        const proveedores = producto.proveedores?.map(proveedor => proveedor.nombre).join(", ") || "Sin proveedores";

        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td>${producto.id}</td>
            <td>${producto.nombre}</td>
            <td>${producto.marca}</td>
            <td contenteditable="true" class="editable" 
                data-id="${producto.id}" data-campo="precio">${producto.precio}</td>
            <td contenteditable="true" class="editable" 
                data-id="${producto.id}" data-campo="stock">${producto.stock}</td>
            <td>${producto.descripcion}</td>
            <td>${proveedores}</td>
            <td>
                <button class="btn btn-warning btn-sm mt-1 mb-1" onclick="editarProducto(${producto.id})">Editar</button>
                <button class="btn btn-danger btn-sm mt-1 mb-1" onclick="darDeBajaProducto(${producto.id})">Dar de baja</button>
            </td>
        `;
        tablaBody.appendChild(fila);
    });
}



function editarProducto(id) {
    if (!id) {
        console.error("ID de producto inválido.");
        return;
    }

    window.location.href = `/productos/editar/${encodeURIComponent(id)}`;
}


// Función para dar de baja el producto (por ahora solo muestra un mensaje)

async function darDeBajaProducto(id) {
    if (!confirm(`¿Estás seguro de que quieres dar de baja el producto con ID ${id}?`)) {
        return;
    }

    try {
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        const response = await fetch('/productos/dar-de-baja', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader]: csrfToken
            },
            body: new URLSearchParams({ id: id }) // Asegurar que el parámetro se llama "id"
        });

        if (!response.ok) throw new Error(`Error al dar de baja el producto. Código: ${response.status}`);

        alert('Producto dado de baja correctamente');
        location.reload();
    } catch (error) {
        console.error('❌ Error:', error.message);
        alert('Hubo un problema al dar de baja el producto');
    }
}

    



function actualizarPaginacion(currentPage, totalPages) {
    const paginacionArriba = document.getElementById("paginacionArriba");
    const paginacionAbajo = document.getElementById("paginacionAbajo");

    paginacionArriba.innerHTML = "";
    paginacionAbajo.innerHTML = "";

    if (totalPages <= 1 || currentPage === undefined) return;

    const ulArriba = document.createElement("ul");
    ulArriba.classList.add("pagination");

    const ulAbajo = document.createElement("ul");
    ulAbajo.classList.add("pagination");

    ulArriba.appendChild(crearBotonPaginacion("Inicio", 0, currentPage === 0, currentPage === 0));
    ulArriba.appendChild(crearBotonPaginacion("Anterior", currentPage - 1, currentPage === 0, currentPage === 0));

    ulAbajo.appendChild(crearBotonPaginacion("Inicio", 0, currentPage === 0, currentPage === 0));
    ulAbajo.appendChild(crearBotonPaginacion("Anterior", currentPage - 1, currentPage === 0, currentPage === 0));

    // **Lógica para limitar la cantidad de páginas visibles**
    let startPage = Math.max(0, currentPage - 5); // Evita ir más atrás de la primera página
    let endPage = Math.min(totalPages - 1, currentPage + 4); // Limita el total a 10 páginas visibles

    if (totalPages > 10) {
        if (currentPage <= 5) {
            endPage = 9; // Si estamos cerca del inicio, mostramos las primeras 10 páginas
        } else if (currentPage >= totalPages - 5) {
            startPage = totalPages - 10; // Si estamos cerca del final, mostramos las últimas 10 páginas
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        ulArriba.appendChild(crearBotonPaginacion(i + 1, i, i === currentPage, false));
        ulAbajo.appendChild(crearBotonPaginacion(i + 1, i, i === currentPage, false));
    }

    ulArriba.appendChild(crearBotonPaginacion("Siguiente", currentPage + 1, currentPage + 1 >= totalPages, currentPage + 1 >= totalPages));
    ulArriba.appendChild(crearBotonPaginacion("Fin", totalPages - 1, currentPage + 1 >= totalPages, currentPage + 1 >= totalPages));

    ulAbajo.appendChild(crearBotonPaginacion("Siguiente", currentPage + 1, currentPage + 1 >= totalPages, currentPage + 1 >= totalPages));
    ulAbajo.appendChild(crearBotonPaginacion("Fin", totalPages - 1, currentPage + 1 >= totalPages, currentPage + 1 >= totalPages));

    paginacionArriba.appendChild(ulArriba);
    paginacionAbajo.appendChild(ulAbajo);
}

function crearBotonPaginacion(texto, page, isActive, isDisabled) {
    const li = document.createElement("li");
    li.classList.add("page-item");
    if (isActive) li.classList.add("active");
    if (isDisabled) li.classList.add("disabled");

    const a = document.createElement("a");
    a.classList.add("page-link");
    a.href = "#";
    a.textContent = texto;
    if (!isDisabled) a.onclick = (event) => {
        event.preventDefault();
        cargarProductos(page);
    };

    li.appendChild(a);
    return li;
}


