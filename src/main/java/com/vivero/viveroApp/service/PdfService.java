package com.vivero.viveroApp.service;

import com.pdfjet.*;
import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.RegistroHorario;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.thymeleaf.templateparser.raw.IRawHandler;

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

    public byte[] generarReporteVentas(List<String[]> datos, String mesAnio) throws IOException, Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDF pdf = new PDF(outputStream);
            Font font = new Font(pdf, CoreFont.HELVETICA);
            Font boldFont = new Font(pdf, CoreFont.HELVETICA_BOLD);
            Font dateFont = new Font(pdf, CoreFont.HELVETICA_BOLD);

            int itemsPerPage = 25; // Máximo por página
            int totalItems = datos.size();
            int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy", new Locale("es", "ES"));
            String currentDate = sdf.format(new Date());

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                Page page = new Page(pdf, Letter.PORTRAIT);

                // Fecha actual arriba
                TextLine dateText = new TextLine(dateFont, currentDate);
                dateText.setPosition(50, 30);
                dateText.drawOn(page);

                // Título principal
                TextLine title = new TextLine(font, "Reporte de Ventas del Mes (" + mesAnio + ")");
                title.setPosition(50, 60);
                title.drawOn(page);

                // Tabla
                Table table = new Table();
                List<List<Cell>> tableData = new ArrayList<>();

                // Rango de esta página
                int start = pageIndex * itemsPerPage;
                int end = Math.min(start + itemsPerPage, totalItems);

                for (int i = start; i < end; i++) {
                    String[] row = datos.get(i);

                    // Si detecta filas especiales vacías o con un solo valor, pone títulos/subtítulos
                    if (row.length == 1) {
                        List<Cell> titleRow = new ArrayList<>();
                        Cell cell = new Cell(boldFont, row[0]);
                        cell.setTextAlignment(Align.LEFT);
                        cell.setTopPadding(10f);
                        titleRow.add(cell);
                        tableData.add(titleRow);
                    } else {
                        List<Cell> dataRow = new ArrayList<>();
                        for (String cellValue : row) {
                            Cell cell = new Cell(font, cellValue);
                            cell.setTextAlignment(Align.LEFT);
                            dataRow.add(cell);
                        }
                        tableData.add(dataRow);
                    }
                }

                table.setData(tableData);
                table.setCellBordersWidth(0.5f);
                table.setColumnWidth(0, 240f);   // Primera columna
                table.setColumnWidth(1, 100f);   // Segunda columna
                if (tableData.stream().anyMatch(row -> row.size() > 2)) {
                    table.setColumnWidth(2, 100f);   // Tercera columna para sección Clientes
                }
                table.setPosition(50, 100);
                table.drawOn(page);
            }

            pdf.flush();
            return outputStream.toByteArray();
        }
    }

    public byte[] generarReporteSimple(List<RegistroHorario> registros, List<IngresoEgreso> adelantos) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PDF pdf = new PDF(baos, Compliance.PDF_A_1B);

            Font font = new Font(pdf, CoreFont.HELVETICA);
            Font fontBold = new Font(pdf, CoreFont.HELVETICA_BOLD);

            Page page = new Page(pdf, Letter.PORTRAIT);

            float x = 50;
            float y = 50;

            TextLine title = new TextLine(fontBold, "Reporte Simple");
            title.setFontSize(16);
            title.setLocation(x, y);
            title.drawOn(page);
            y += 30;

            TextLine header1 = new TextLine(fontBold, "Tabla: RegistroHorario");
            header1.setLocation(x, y);
            header1.drawOn(page);
            y += 20;

            for (RegistroHorario registro : registros) {
                TextLine line = new TextLine(font, "ID: " + registro.getId() + " | Nombre: " + registro.getUsuario().getNombre());
                line.setLocation(x, y);
                line.drawOn(page);
                y += 15;
            }

            y += 30;

            TextLine header2 = new TextLine(fontBold, "Tabla: IngresoEgreso");
            header2.setLocation(x, y);
            header2.drawOn(page);
            y += 20;

            for (IngresoEgreso ingreso : adelantos) {
                TextLine line = new TextLine(font, "ID: " + ingreso.getId() + " | Cantidad: " + ingreso.getMonto());
                line.setLocation(x, y);
                line.drawOn(page);
                y += 15;
            }

            pdf.close(); // Aquí cerramos el PDF correctamente
            return baos.toByteArray(); // Retornamos el byte array del PDF

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //////////////////////////////////
    public byte[] generarReporteHorarios(List<String[]> datos, String mesAnio) throws IOException, Exception {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        PDF pdf = new PDF(outputStream);
        Font font = new Font(pdf, CoreFont.HELVETICA);
        Font boldFont = new Font(pdf, CoreFont.HELVETICA_BOLD);
        Font dateFont = new Font(pdf, CoreFont.HELVETICA_BOLD);

        int itemsPerPage = 25;
        int totalItems = datos.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy", new Locale("es", "ES"));
        String currentDate = sdf.format(new Date());

        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            Page page = new Page(pdf, Letter.PORTRAIT);

            // Fecha actual arriba
            TextLine dateText = new TextLine(dateFont, currentDate);
            dateText.setPosition(50, 30);
            dateText.drawOn(page);

            // Título principal
            TextLine title = new TextLine(font, "Reporte de Registros de Horarios (" + mesAnio + ")");
            title.setPosition(50, 60);
            title.drawOn(page);

            // Tabla
            Table table = new Table();
            List<List<Cell>> tableData = new ArrayList<>();

            // Rango de esta página
            int start = pageIndex * itemsPerPage;
            int end = Math.min(start + itemsPerPage, totalItems);

            for (int i = start; i < end; i++) {
                String[] row = datos.get(i);

                List<Cell> dataRow = new ArrayList<>();

                for (String cellValue : row) {
                    Cell cell;
                    if (row.length == 1 || (row[0].isEmpty() && row[1].isEmpty())) {
                        // Para separadores o títulos grandes
                        cell = new Cell(boldFont, cellValue);
                    } else {
                        cell = new Cell(font, cellValue);
                    }
                    cell.setTextAlignment(Align.LEFT);
                    dataRow.add(cell);
                }
                tableData.add(dataRow);
            }

            table.setData(tableData, Table.DATA_HAS_1_HEADER_ROWS);
            table.setPosition(50, 100);
            table.drawOn(page);
        }

        pdf.close();
        return outputStream.toByteArray();
    }
}


}
