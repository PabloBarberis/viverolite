$(document).ready(function () {
    const csrfMeta = document.querySelector("meta[name='_csrf']");
    const csrfHeaderMeta = document.querySelector("meta[name='_csrf_header']");

    // 🔥 Cargar lista de compras
    fetch("/compras/mostrar", {
        method: "GET",
        headers: {
            [csrfHeaderMeta.content]: csrfMeta.content
        }
    })
    .then(response => response.json())
    .then(compras => {

        console.log(compras); // 🔥 Verificar qué tipo de datos está llegando


        let tablaCompras = $("#tablaCompras");
        tablaCompras.empty();

        compras.forEach(compra => {
            tablaCompras.append(`
                <tr data-id="${compra.id}">
                    <td>${compra.id}</td>
                    <td>${new Date(compra.fecha).toLocaleString()}</td>
                    <td>${compra.comentario || 'Sin comentario'}</td>
                    <td>
                        <button class="btn btn-info verCompra">Ver</button>
                        <button class="btn btn-danger eliminarCompra">Eliminar</button>
                    </td>
                </tr>
            `);
        });
    })
    .catch(error => console.error("Error al listar compras:", error));

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

            data.productos.forEach(producto => {
                tablaProductos.append(`
                    <tr>
                        <td>${producto.nombre}</td>
                        <td>${producto.precioCompra.toFixed(2)}</td>
                        <td>${producto.precio}</td>
                        <td>${producto.cantidad}</td>
                    </tr>
                `);
            });

            $("#modalCompra").modal("show");
        })
        .catch(error => console.error("Error al obtener compra:", error));
    });

   // 🔥 Eliminar compra con alerta de éxito
$(document).on("click", ".eliminarCompra", function () {
    let compraId = $(this).closest("tr").data("id");

    if (!confirm(`¿Estás seguro de que quieres eliminar la compra ${compraId}?`)) return;

    fetch(`/compras/eliminar/${compraId}`, {
        method: "DELETE",
        headers: {
            [csrfHeaderMeta.content]: csrfMeta.content
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al eliminar compra.");
        }
        return response.text();
    })
    .then(message => {
        $(this).closest("tr").remove();
        alert(`✅ ${message}`); // 🔥 Muestra un mensaje de éxito con el texto del backend
    })
    .catch(error => {
        console.error("Error al eliminar compra:", error);
        alert("❌ Hubo un problema al eliminar la compra.");
    });
});

});
