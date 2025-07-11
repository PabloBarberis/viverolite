$(document).ready(function () {
    // Inicializar Select2 para buscar productos
    $('#producto').select2({
        placeholder: "Buscar producto...",
        allowClear: true,
        width: '40%',
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
                    stock: producto.stock,
                    precio: producto.precio
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

    // Al seleccionar un producto, enfocar automáticamente en el campo de cantidad
    $('#producto').on('select2:select', function () {
        setTimeout(() => $('#cantidadProducto').focus().select(), 100);
    });

    // Agregar producto a la tabla al hacer clic en "Agregar Producto"
    $('#agregarProducto').on('click', function () {
        let productoSeleccionado = $('#producto').select2('data')[0];

        if (!productoSeleccionado) return;

        // Capturar la cantidad del input
        let cantidad = parseInt($('#cantidadProducto').val());
        let precioCompra =parseInt($('#precioCompraInput').val());

        if (isNaN(cantidad) || cantidad <= 0) {
            alert("La cantidad debe ser mayor a cero.");
            return;
        }

        let existeProducto = $(`#productosSeleccionados tr[data-id="${productoSeleccionado.id}"]`).length > 0;
        if (existeProducto) {
            alert("Este producto ya fue agregado.");
            return;
        }

        $('#productosSeleccionados').prepend(`
            <tr data-id="${productoSeleccionado.id}">
                <td>${productoSeleccionado.id}</td>
                <td>${productoSeleccionado.text}</td>
                <td>${productoSeleccionado.stock}</td>
                <td><input type="number" class="cantidad" min="1" value="${cantidad}" required></td>
                <td><input type="number" class="precio" min="0" step="0.01" value="${productoSeleccionado.precio}"></td>
                <td><input type="number" class="precioCompra" min="0" step="0.01" value="${precioCompra}" required ></td>
                <td><button class="btn btn-danger eliminarProducto">Eliminar</button></td>
            </tr>
        `);

        // Limpiar el campo de cantidad
        $('#cantidadProducto').val(1);
        $('#precioCompraInput').val(0);

        // Volver a enfocar en el select2
        $('#producto').select2('focus');
    });

    // Eliminar producto de la lista
    $(document).on('click', '.eliminarProducto', function () {
        $(this).closest('tr').remove();
    });

    // Guardar compra
    $('#guardarCompra').on('click', function () {
        let productos = [];
        let valid = true;

        $('#productosSeleccionados tr').each(function () {
            let cantidad = $(this).find('.cantidad').val();
            let precioCompra = $(this).find('.precioCompra').val();

            if (!cantidad || cantidad <= 0) {
                alert("Todos los productos deben tener una cantidad válida mayor a cero.");
                valid = false;
                return false;
            }

            let producto = {
                id: $(this).data('id'),
                nombre: $(this).find('td:eq(1)').text(),
                stock: parseInt($(this).find('td:eq(2)').text()),
                cantidad: parseInt($(this).find('.cantidad').val()),
                precio: parseFloat($(this).find('.precio').val()),  // Precio de venta
                precioCompra: parseFloat($(this).find('.precioCompra').val()) // Precio de compra
            };

            productos.push(producto);
        });

        if (!valid) return;
        if (productos.length === 0) {
            alert("Debes agregar al menos un producto antes de guardar la compra.");
            return;
        }

        let comentario = $('#comentario').val().trim();

        const csrfMeta = document.querySelector("meta[name='_csrf']");
        const csrfHeaderMeta = document.querySelector("meta[name='_csrf_header']");

        let compraData = {
            comentario: comentario,
            productos: productos
        };

        fetch("/ingresarcompra", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeaderMeta.content]: csrfMeta.content
            },
            body: JSON.stringify(compraData)
        })
        .then(response => response.text())
        .then(data => {
            alert(data);
            location.reload();
        })
        .catch(error => console.error("Error:", error));
    });

    // Foco inicial en el select2 al cargar la página
    setTimeout(() => $('#producto').select2('focus'), 300);

    // Manejar Enter en el campo de cantidad para agregar el producto
    $('#precioCompraInput').on('keypress', function (e) {
        if (e.which === 13) { // Enter
            $('#agregarProducto').click(); // Simular clic en botón
        }
    });

    $('#cantidadProducto').on('keypress', function (e) {
        if (e.which === 13) { // Enter
            $('#precioCompraInput').focus(); // Simular clic en botón
        }
    });
});