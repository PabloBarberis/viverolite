$(document).ready(function () {
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
                <td><input type="number" class="cantidad" min="1" value="1" required></td>
                <td><input type="number" class="precio" min="0" step="0.01" value="${productoSeleccionado.precio}"></td>
                <td><input type="number" class="precioCompra" min="0" step="0.01" required></td>
                <td><button class="btn btn-danger eliminarProducto">Eliminar</button></td>
            </tr>
        `);
    });

    $(document).on('click', '.eliminarProducto', function () {
        $(this).closest('tr').remove();
    });

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
                precio: parseFloat($(this).find('.precio').val()),  // Precio de venta al público
                precioCompra: parseFloat($(this).find('.precioCompra').val()) // Precio de compra
            };
    
            productos.push(producto);
        });
        
        if (!valid) return;
        if (productos.length === 0) {
            alert("Debes agregar al menos un producto antes de guardar la compra.");
            return;
        }
    
        let comentario = $('#comentario').val().trim(); // 🔥 Capturar el comentario
    
        const csrfMeta = document.querySelector("meta[name='_csrf']");
        const csrfHeaderMeta = document.querySelector("meta[name='_csrf_header']");
    
        let compraData = {
            comentario: comentario, // 🔥 Incluir el comentario
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
        .then(response => response.text()) // 🔥 Cambiar a `text()` en lugar de `json()`
        .then(data => {
            alert(data); // Ahora sí se muestra correctamente el mensaje del servidor
            location.reload();
        })
        .catch(error => console.error("Error:", error));
    });
    
});
