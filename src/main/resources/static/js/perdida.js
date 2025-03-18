import BASE_URL from '/js/config.js';

////////////////////////-PERDIDA INVENTARIO-///////////////////////////



$(document).ready(function () {
    if (window.location.pathname === "/perdida_inventario") {


        $('#modalPerdida').on('show.bs.modal', function () {
            $.getJSON(`${BASE_URL}/perdida_inventario/productos`, function (data) {
                let select = $('#producto');
                select.empty();
                select.append('<option value="">Seleccione un producto</option>');

                $.each(data, function (index, producto) {
                    select.append('<option value="' + producto.id + '">' + producto.nombre + '</option>');
                });

                // Recargar el selectpicker después de cargar los productos
                select.selectpicker('refresh');
            });
        });

        $('#formPerdida').submit(function (event) {
            event.preventDefault(); // Evita el envío automático

            let productoId = $('#producto').val();
            let cantidad = $('#cantidad').val();
            let descripcion = $('#descripcion').val();

            if (!productoId || !cantidad || cantidad <= 0) {
                alert("Debe seleccionar un producto y una cantidad válida.");
                return;
            }

            // Obtener el token CSRF del meta tag
            let csrfToken = $('meta[name="_csrf"]').attr('content');
            let csrfHeader = $('meta[name="_csrf_header"]').attr('content');


            $.ajax({
                url: `${BASE_URL}/perdida_inventario/guardar`,
                type: "POST",
                contentType: "application/x-www-form-urlencoded",
                data: {
                    productoId: productoId,
                    cantidad: cantidad,
                    descripcion: descripcion
                },
                beforeSend: function (xhr) {
                    xhr.setRequestHeader(csrfHeader, csrfToken); // Agregar CSRF token en el header
                },
                success: function (response) {
                    alert("Pérdida de inventario registrada correctamente.");
                    location.reload(); // Recargar la página después de guardar
                },
                error: function (xhr, status, error) {
                    alert("Error al registrar la pérdida: " + xhr.responseText);
                }
            });

        });
    }
});

