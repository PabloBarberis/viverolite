document.querySelectorAll('.editable').forEach(cell => {
  // VALIDACIÓN DE ENTRADA
  cell.addEventListener('keydown', (e) => {
    const campo = cell.dataset.campo;

    // Navegación con flechas ↑ ↓
    if (e.key === 'ArrowUp' || e.key === 'ArrowDown') {
      e.preventDefault();
      const currentRow = cell.closest('tr');
      const currentIndex = Array.from(currentRow.children).indexOf(cell);

      const nextRow = e.key === 'ArrowDown'
        ? currentRow.nextElementSibling
        : currentRow.previousElementSibling;

      if (nextRow) {
        const targetCell = nextRow.children[currentIndex];
        if (targetCell && targetCell.classList.contains('editable')) {
          targetCell.focus();
        }
      }
      return;
    }

    // Bloquear letras si es campo 'precio'
    if (campo === 'precio') {
      const permitidos = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight'];
      const esNumero = /^[0-9.,]$/.test(e.key);

      if (!esNumero && !permitidos.includes(e.key)) {
        e.preventDefault();
      }
    }
  });

  // Seleccionar todo el texto al enfocar
  cell.addEventListener('focus', () => {
    const range = document.createRange();
    range.selectNodeContents(cell);
    const sel = window.getSelection();
    sel.removeAllRanges();
    sel.addRange(range);

    cell.style.backgroundColor = '#fffbe6';
    cell.style.outline = '2px solidrgb(28, 255, 7)';
    cell.style.color = '#000';
  });

  // Al perder foco: restaurar estilos y guardar
  cell.addEventListener('blur', async () => {
    const id = cell.dataset.id;
    const campo = cell.dataset.campo;
    const valor = cell.innerText.trim();

    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    cell.classList.remove('guardado', 'error');
    cell.style.backgroundColor = '';
    cell.style.color = '';

    if (campo === 'precio' && isNaN(valor)) {
      cell.classList.add('error');
      cell.innerText = '⚠️ Solo números';
      setTimeout(() => {
        cell.classList.remove('error');
        cell.innerText = '0.0';
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
});
