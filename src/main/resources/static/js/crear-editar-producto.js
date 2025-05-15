document.addEventListener("DOMContentLoaded", function () {
    // Detectar edición
    const path = window.location.pathname;
    const esEdicion = path.includes("/productos/editar/");
    const productoId = esEdicion ? path.split("/").pop() : null;

    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // Variables del formulario
    const tipoSelect = document.getElementById("tipo");
    const marcaSelect = document.getElementById("marcaExistente");
    const marcaNuevaRadio = document.getElementById("marcaNuevaRadio");
    const marcaExistenteRadio = document.getElementById("marcaExistenteRadio");
    const marcaNuevaInput = document.getElementById("marcaNueva");
    const marcaExistenteSelect = document.getElementById("marcaExistente");



    // ✅ Si estamos en edición, obtener los datos del producto
    if (esEdicion) {


        fetch(`/productos/api/${productoId}`)
            .then(response => response.json())
            .then(data => {
                // Cargar datos generales del producto
                document.getElementById("nombre").value = data.nombre;
                document.getElementById("precio").value = data.precio;
                document.getElementById("stock").value = data.stock;
                document.getElementById("descripcion").value = data.descripcion;

                // ✅ Seleccionar el `dtype` en el `select` de tipo de producto
                console.log(data.dtype);
                tipoSelect.value = data.dtype;

                // ✅ Cargar las marcas según el tipo seleccionado
                cargarMarcas(data.dtype, data.marca);

                // ✅ Marcar proveedores correctos
                data.proveedores.forEach(proveedor => {
                    const checkbox = document.getElementById(`prov-${proveedor.id}`);
                    if (checkbox) checkbox.checked = true;
                });
            })
            .catch(error => console.error("Error al cargar el producto:", error));

        // ✅ Detectar si el select está deshabilitado y mostrar alerta
        tipoSelect.addEventListener("mousedown", function (event) {
            if (tipoSelect.hasAttribute("disabled")) {
                event.preventDefault(); // ✅ Evita la interacción con el select
                alert("No se puede editar el tipo de producto."); // ✅ Muestra el mensaje al usuario
            }
        });
    }

    // ✅ Si es creación, cargar marcas cuando el usuario elige tipo de producto
    tipoSelect.addEventListener("change", function () {
        const tipoSeleccionado = tipoSelect.value;
        if (tipoSeleccionado) {
            cargarMarcas(tipoSeleccionado);
        }
    });




    // ✅ Función para cargar marcas según el tipo de producto
    function cargarMarcas(tipo, marcaSeleccionada = null) {
        marcaSelect.innerHTML = '<option value="">Seleccione...</option>';

        fetch(`/api/marcas?tipo=${tipo}`)
            .then(response => response.json())
            .then(marcas => {
                marcas.forEach(marca => {
                    let option = document.createElement("option");
                    option.value = marca;
                    option.textContent = marca;
                    marcaSelect.appendChild(option);
                });

                // Si estamos en edición, seleccionar la marca correcta
                if (marcaSeleccionada) {
                    marcaSelect.value = marcaSeleccionada;
                }
            })
            .catch(error => console.error("Error al obtener las marcas:", error));
    }

    // ✅ Alternar entre marca nueva y existente
    function actualizarVisibilidadMarca() {
        marcaNuevaInput.style.display = marcaNuevaRadio.checked ? "block" : "none";
        marcaExistenteSelect.style.display = marcaExistenteRadio.checked ? "block" : "none";
    }

    // Escuchar cambios en los radios y aplicar la visibilidad correcta
    marcaNuevaRadio.addEventListener("change", actualizarVisibilidadMarca);
    marcaExistenteRadio.addEventListener("change", actualizarVisibilidadMarca);
    actualizarVisibilidadMarca(); // Aplicar al cargar

    // ✅ Enviar el formulario al backend (POST para crear, PUT para editar)
    document.getElementById("productoForm").addEventListener("submit", function (event) {
        event.preventDefault(); // Evita la recarga de página

        const formData = new URLSearchParams();
        formData.append("nombre", document.getElementById("nombre").value);
        formData.append("tipo", tipoSelect.value);
        formData.append("precio", parseFloat(document.getElementById("precio").value));
        formData.append("stock", parseInt(document.getElementById("stock").value));
        formData.append("descripcion", document.getElementById("descripcion").value);

        // ✅ Capturar selección de marca
        const marcaOption = document.querySelector('input[name="marcaOption"]:checked').value;
        formData.append("marca", marcaOption === "existente" ? marcaSelect.value : "");
        formData.append("marcaNueva", marcaOption === "nueva" ? marcaNuevaInput.value : "");

        // ✅ Capturar proveedores seleccionados

        const proveedoresSeleccionados = Array.from(proveedoresContainer.querySelectorAll("input[type='checkbox']:checked"));

        if (proveedoresSeleccionados.length > 0) {
            proveedoresSeleccionados.forEach(cb => formData.append("proveedoresId", cb.value));
        } else {
            // 👇 Esto fuerza el envío de una lista vacía
            formData.append("proveedoresId", "");
        }


        /*        Array.from(proveedoresContainer.querySelectorAll("input[type='checkbox']:checked"))
                    .forEach(cb => formData.append("proveedoresId", cb.value));
        */
        // ✅ Definir el endpoint y método según si es edición o creación
        const url = esEdicion ? `/productos/editar/${productoId}` : "/productos/guardar";
        const metodo = esEdicion ? "PUT" : "POST";

        fetch(url, {
            method: metodo,
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                [csrfHeader]: csrfToken // ✅ Asegurar que el token CSRF se envíe correctamente
            },
            body: formData.toString()
        })

            .then(response => {
                if (response.ok) {
                    alert("✅ Producto guardado con éxito.");
                    window.location.href = "/productos/listar";
                } else {
                    return response.text().then(text => alert("Error: " + text));
                }
            })

            .catch(error => console.error(`Error al ${metodo === "POST" ? "guardar" : "actualizar"} el producto:`, error));
    });

});
