package com.vivero.viveroApp.service;

import com.pdfjet.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class PdfService {

    /**
     * Genera un PDF a partir de una lista de datos.
     *
     * @param items Lista de objetos que se incluirán en el PDF.
     * @param headers Encabezados de la tabla del PDF.
     * @param rowMapper Función que convierte cada objeto en una fila de datos.
     * @return Array de bytes del PDF generado.
     * @throws IOException En caso de error durante la creación del PDF.
     */
    public byte[] generarPDF(List<?> items, String[] headers, Function<Object, String[]> rowMapper) throws IOException, Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDF pdf = new PDF(outputStream);
            Font font = new Font(pdf, CoreFont.HELVETICA);
            Font dateFont = new Font(pdf, CoreFont.HELVETICA_BOLD);

            int itemsPerPage = 38; // Máximo por página
            int totalItems = items.size();
            int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy", new Locale("es", "ES"));
            String currentDate = sdf.format(new Date());

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                Page page = new Page(pdf, Letter.PORTRAIT);

                // Fecha
                TextLine dateText = new TextLine(dateFont, currentDate);
                dateText.setPosition(50, 30);
                dateText.drawOn(page);

                // Tabla
                Table table = new Table();
                List<List<Cell>> tableData = new java.util.ArrayList<>();

                // Encabezados
                List<Cell> headerRow = new java.util.ArrayList<>();
                for (String header : headers) {
                    Cell cell = new Cell(font, header);
                    cell.setTextAlignment(Align.CENTER);
                    cell.setBgColor(Color.lightgray);
                    headerRow.add(cell);
                }
                tableData.add(headerRow);

                // Rango de esta página
                int start = pageIndex * itemsPerPage;
                int end = Math.min(start + itemsPerPage, totalItems);

                // Filas de datos
                for (int i = start; i < end; i++) {
                    String[] row = rowMapper.apply(items.get(i));
                    List<Cell> dataRow = new java.util.ArrayList<>();
                    for (String cellValue : row) {
                        Cell cell = new Cell(font, cellValue);
                        cell.setTextAlignment(Align.LEFT);
                        dataRow.add(cell);
                    }
                    tableData.add(dataRow);
                }

                table.setData(tableData);
                table.setCellBordersWidth(0.5f);

                table.setColumnWidth(0, 40f);   // ID
                table.setColumnWidth(1, 240f);  // Nombre
                table.setColumnWidth(2, 130f);  // Marca
                table.setColumnWidth(3, 80f);   // Precio
                table.setColumnWidth(4, 40f);   // Stock

                table.setPosition(50, 50);
                table.drawOn(page);
            }

            pdf.flush();
            return outputStream.toByteArray();
        }
    }

}
