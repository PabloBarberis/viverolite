document.querySelectorAll('.editable').forEach(cell => {
    // VALIDACIÓN DE ENTRADA: bloquear letras si es campo 'precio'
    cell.addEventListener('keydown', (e) => {
      const campo = cell.dataset.campo;
      if (campo === 'precio') {
        // Permitimos: números, punto, coma, backspace, delete, tab, flechas
        const permitidos = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'];
        const esNumero = /^[0-9.,]$/.test(e.key);
  
        if (!esNumero && !permitidos.includes(e.key)) {
          e.preventDefault(); // Bloqueamos la tecla
        }
      }
    });
  
    cell.addEventListener('blur', async () => {
      const id = cell.dataset.id;
      const campo = cell.dataset.campo;
      const valor = cell.innerText.trim();
  
      const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
  
      // Limpiar estilos previos (si los hubiera)
      cell.classList.remove('guardado', 'error');
      cell.style.backgroundColor = ''; // Restaurar fondo original
      cell.style.color = '';           // Restaurar color de texto original
  
      // Validación final por si se pega texto con mouse
      if (campo === 'precio' && isNaN(valor)) {
        cell.classList.add('error');
        cell.innerText = '⚠️ Solo números';
        setTimeout(() => {
          cell.classList.remove('error');
          cell.innerText = '0.0'; // Podés restaurar el valor anterior si lo guardás antes
        }, 2000);
        return;
      }
  
      try {
        const response = await fetch('/producto/actualizar-campo', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
          },
          body: JSON.stringify({ id, campo, valor })
        });
  
        if (!response.ok) throw new Error('Error al guardar');
  
        cell.classList.add('guardado');
        setTimeout(() => cell.classList.remove('guardado'), 2000);
      } catch (error) {
        console.error('❌ Error:', error.message);
        cell.classList.add('error');
        setTimeout(() => cell.classList.remove('error'), 2000);
      }
    });
  
    // Estilo visual al enfocar
    cell.addEventListener('focus', () => {
      cell.style.backgroundColor = '#fffbe6';
      cell.style.outline = '2px solid #ffc107';
      cell.style.color = '#000';
    });
  
    // Al perder el foco
    cell.addEventListener('blur', () => {
      cell.style.outline = 'none';
    });
  
    setTimeout(() => {
      cell.classList.remove('guardado');
      cell.style.boxShadow = 'none';
    }, 2000);
  });
  