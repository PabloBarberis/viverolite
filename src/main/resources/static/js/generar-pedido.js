$(document).ready(function () {
    // Inicializar Select2
    $('#producto').select2({
        placeholder: "Buscar producto...",
        allowClear: true,
        width: '50%',
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

    // Enfocar automáticamente en el campo de búsqueda dentro del select2 abierto
    $('#producto').on('select2:open', function () {
        setTimeout(() => {
            document.querySelector('.select2-container--open .select2-search__field').focus();
        }, 100);
    });

    // Al seleccionar un producto, ir al campo cantidad
    $('#producto').on('select2:select', function () {
        setTimeout(() => $('#cantidadProducto').focus().select(), 100);
    });

    // Agregar producto al hacer clic en el botón
    $('#agregarProducto').on('click', function () {
        let productoSeleccionado = $('#producto').select2('data')[0];
        let cantidad = parseInt($('#cantidadProducto').val());

        if (!productoSeleccionado) return;
        if (isNaN(cantidad) || cantidad <= 0) {
            alert("Ingresá una cantidad válida.");
            return;
        }

        let existeProducto = $(`#productosSeleccionados tr[data-id="${productoSeleccionado.id}"]`).length > 0;
        if (existeProducto) {
            alert("Este producto ya fue agregado.");
            return;
        }

        // Insertar al inicio de la lista
        $('#productosSeleccionados').prepend(`
            <tr data-id="${productoSeleccionado.id}">
                <td>${productoSeleccionado.id}</td>
                <td>${productoSeleccionado.text}</td>
                <td>${productoSeleccionado.stock}</td>
                <td><input type="number" class="cantidad" min="1" value="${cantidad}"></td>
                <td><button class="btn btn-danger eliminarProducto">Eliminar</button></td>
            </tr>
        `);

        // Limpiar cantidad y volver a enfocar en el producto
        $('#cantidadProducto').val(1);
        $('#producto').select2('focus');
    });

    // Manejar Enter en el campo cantidad para agregar producto
    $('#cantidadProducto').on('keypress', function (e) {
        if (e.which === 13) { // Enter
            $('#agregarProducto').click(); // Simular clic en "Agregar Producto"
        }
    });

    // Eliminar producto
    $(document).on('click', '.eliminarProducto', function () {
        $(this).closest('tr').remove();
    });

    // Generar pedido
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

    // Foco inicial en el select2 al cargar la página
    setTimeout(() => $('#producto').select2('focus'), 300);
});