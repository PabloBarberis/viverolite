$(document).ready(function () {
    const csrfMeta = document.querySelector("meta[name='_csrf']");
    const csrfHeaderMeta = document.querySelector("meta[name='_csrf_header']");

    // Parámetros de paginación
    let currentPage = 0;
    const pageSize = 10; // Puedes hacerlo configurable si quieres

    // 🔥 Cargar lista de compras (con paginación)
    function cargarCompras(page) {
        currentPage = page;

        fetch(`/compras/mostrar?page=${page}&size=${pageSize}`, {
            method: "GET",
            headers: {
                [csrfHeaderMeta.content]: csrfMeta.content
            }
        })
        .then(response => {
            if (!response.ok) throw new Error("Error en la respuesta del servidor");
            return response.json();
        })
        .then(data => {
            console.log("Datos paginados:", data);

            let tablaCompras = $("#tablaCompras");
            tablaCompras.empty();

            // Iterar sobre data.content (el array real de compras)
            data.content.forEach(compra => {
                tablaCompras.append(`
                    <tr data-id="${compra.id}">
                        <td>${compra.id}</td>
                        <td>${new Date(compra.fecha).toLocaleString()}</td>
                        <td>${compra.comentario || 'Sin comentario'}</td>
                        <td>
                            <button class="btn btn-info btn-sm verCompra">Ver</button>
                            <button class="btn btn-danger btn-sm eliminarCompra">Eliminar</button>
                        </td>
                    </tr>
                `);
            });

            // Renderizar controles de paginación
            renderPaginacion(data);
        })
        .catch(error => {
            console.error("Error al listar compras:", error);
            alert("❌ No se pudieron cargar las compras.");
        });
    }

    // 🔥 Renderizar controles de paginación
    function renderPaginacion(pageData) {
    const { number: currentPage, totalPages, totalElements } = pageData;

    // Si no hay elementos, no mostramos paginación
    if (totalElements === 0) {
        $("#paginacion").html('<p class="text-muted">No hay compras registradas.</p>');
        return;
    }

    let paginationHtml = `<nav aria-label="Paginación"><ul class="pagination">`; // ← sin "justify-content-center"

    // Botón Anterior (siempre visible, pero deshabilitado si es la primera página)
    const prevDisabled = currentPage === 0 ? 'disabled' : '';
    paginationHtml += `
        <li class="page-item ${prevDisabled}">
            <a class="page-link" href="#" data-page="${currentPage - 1}" tabindex="${prevDisabled ? '-1' : ''}" aria-disabled="${prevDisabled ? 'true' : 'false'}">
                Anterior
            </a>
        </li>`;

    // Mostrar todas las páginas (o un rango si son muchas)
    const maxVisiblePages = 5;
    let startPage = 0;
    let endPage = totalPages - 1;

    if (totalPages > maxVisiblePages) {
        const half = Math.floor(maxVisiblePages / 2);
        startPage = Math.max(0, currentPage - half);
        endPage = Math.min(totalPages - 1, currentPage + half);

        // Ajustar si estamos al inicio o al final
        if (currentPage < half) {
            endPage = Math.min(totalPages - 1, maxVisiblePages - 1);
        } else if (currentPage + half >= totalPages) {
            startPage = Math.max(0, totalPages - maxVisiblePages);
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        const active = i === currentPage ? 'active' : '';
        paginationHtml += `
            <li class="page-item ${active}">
                <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
            </li>`;
    }

    // Botón Siguiente (siempre visible, pero deshabilitado si es la última página)
    const nextDisabled = currentPage === totalPages - 1 ? 'disabled' : '';
    paginationHtml += `
        <li class="page-item ${nextDisabled}">
            <a class="page-link" href="#" data-page="${currentPage + 1}" tabindex="${nextDisabled ? '-1' : ''}" aria-disabled="${nextDisabled ? 'true' : 'false'}">
                Siguiente
            </a>
        </li>`;

    paginationHtml += `</ul></nav>`;
    paginationHtml += `<p class="text-muted small">Total: ${totalElements} compras</p>`;

    $("#paginacion").html(paginationHtml);

    // Manejar clics (solo en enlaces no deshabilitados)
    $("#paginacion a.page-link").on("click", function (e) {
        e.preventDefault();
        if ($(this).parent().hasClass("disabled")) return; // Ignorar si está deshabilitado
        const page = parseInt($(this).data("page"));
        cargarCompras(page);
    });
}

    // 🔥 Cargar primera página al iniciar
    cargarCompras(0);

    // 🔥 Ver detalles de compra (modal)
    $(document).on("click", ".verCompra", function () {
        let compraId = $(this).closest("tr").data("id");

        fetch(`/compras/detalle/${compraId}`, {
            method: "GET",
            headers: {
                [csrfHeaderMeta.content]: csrfMeta.content
            }
        })
        .then(response => response.json())
        .then(data => {
            $("#compraId").text(data.id);
            $("#compraFecha").text(new Date(data.fecha).toLocaleString());
            $("#compraComentario").text(data.comentario || 'Sin comentario');

            let tablaProductos = $("#tablaProductosCompras");
            tablaProductos.empty();

            if (data.productos && data.productos.length > 0) {
                data.productos.forEach(producto => {
                    tablaProductos.append(`
                        <tr>
                            <td>${producto.nombre || '—'}</td>
                            <td>${producto.precioCompra ? producto.precioCompra.toFixed(2) : '—'}</td>
                            <td>${producto.precio ? producto.precio.toFixed(2) : '—'}</td>
                            <td>${producto.cantidad || '—'}</td>
                        </tr>
                    `);
                });
            } else {
                tablaProductos.append(`<tr><td colspan="4" class="text-center">No hay productos</td></tr>`);
            }

            $("#modalCompra").modal("show");
        })
        .catch(error => {
            console.error("Error al obtener compra:", error);
            alert("❌ No se pudo cargar el detalle de la compra.");
        });
    });

    // 🔥 Eliminar compra
    $(document).on("click", ".eliminarCompra", function () {
        let compraId = $(this).closest("tr").data("id");

        if (!confirm(`¿Estás seguro de que quieres eliminar la compra #${compraId}?`)) return;

        fetch(`/compras/eliminar/${compraId}`, {
            method: "DELETE",
            headers: {
                [csrfHeaderMeta.content]: csrfMeta.content
            }
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text || "Error desconocido"); });
            }
            return response.text();
        })
        .then(message => {
            // Eliminar fila de la tabla
            $(this).closest("tr").fadeOut(300, function() {
                $(this).remove();
            });

            // Mostrar mensaje de éxito
            alert(`✅ ${message || "Compra eliminada correctamente."}`);

            // Si la página queda vacía y no es la primera, retroceder
            if ($("#tablaCompras tr").length === 0 && currentPage > 0) {
                cargarCompras(currentPage - 1);
            }
        })
        .catch(error => {
            console.error("Error al eliminar compra:", error);
            alert(`❌ Error al eliminar: ${error.message}`);
        });
    });
});