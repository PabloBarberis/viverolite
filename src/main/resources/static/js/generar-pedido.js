$(document).ready(function () {
    $('#producto').select2({
        placeholder: "Buscar producto...",
        allowClear: true,
        width: '100%',
        minimumInputLength: 2,
        language: {
            inputTooShort: () => 'Ingresá al menos 2 caracteres',
            noResults: () => 'No se encontraron productos',
            searching: () => 'Buscando...'
        },
        ajax: {
            url: '/api/productos',
            dataType: 'json',
            delay: 250,
            data: params => ({ q: params.term || '' }),
            processResults: data => ({
                results: data.map(producto => ({
                    id: producto.id,
                    text: producto.nombre,
                    stock: producto.stock
                }))
            }),
            cache: true
        }
    });

    $('#producto').on('select2:open', function () {
        setTimeout(() => {
            document.querySelector('.select2-container--open .select2-search__field').focus();
        }, 100);
    });

    $('#agregarProducto').on('click', function () {
        let productoSeleccionado = $('#producto').select2('data')[0];

        if (!productoSeleccionado) return;

        let existeProducto = $(`#productosSeleccionados tr[data-id="${productoSeleccionado.id}"]`).length > 0;
        if (existeProducto) {
            alert("Este producto ya fue agregado.");
            return;
        }

        $('#productosSeleccionados').append(`
            <tr data-id="${productoSeleccionado.id}">
                <td>${productoSeleccionado.id}</td>
                <td>${productoSeleccionado.text}</td>
                <td>${productoSeleccionado.stock}</td>
                <td><input type="number" class="cantidad" min="1" value="1"></td>
                <td><button class="btn btn-danger eliminarProducto">Eliminar</button></td>
            </tr>
        `);
    });

    $(document).on('click', '.eliminarProducto', function () {
        $(this).closest('tr').remove();
    });

    $('#generarPedido').on('click', function () {
        let productos = [];
        $('#productosSeleccionados tr').each(function () {
            let producto = {
                id: $(this).data('id'),
                nombre: $(this).find('td:eq(1)').text(),
                stock: $(this).find('td:eq(2)').text(),
                cantidad: $(this).find('.cantidad').val()
            };
            productos.push(producto);
        });

        if (productos.length === 0) {
            alert("Debes agregar al menos un producto antes de generar el pedido.");
            return;
        }

        console.log(JSON.stringify(productos));

        const csrfMeta = document.querySelector("meta[name='_csrf']");
        const csrfHeaderMeta = document.querySelector("meta[name='_csrf_header']");

        fetch("/pedido/generarPedido", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeaderMeta.content]: csrfMeta.content
            },
            body: JSON.stringify(productos)
        })
        .then(response => {
            if (!response.ok) throw new Error(`Error ${response.status}`);
            return response.blob();
        })
        .then(blob => {
            let link = document.createElement("a");
            link.href = URL.createObjectURL(blob);
            link.download = "pedido.pdf";
            link.click();
        })
        .catch(error => console.error("Error:", error));
    });
});
