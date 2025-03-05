window.onload = function() {
    fetch('/templates/navbar.html')  // Verifica si la ruta es correcta
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al cargar la navbar');
            }
            return response.text();
        })
        .then(data => {
            document.getElementById('navbar-container').innerHTML = data; // Inserta el contenido en el contenedor
        })
        .catch(error => {
            console.error('Error al cargar la navbar:', error);
        });
};
