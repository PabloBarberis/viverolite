package com.vivero.viveroApp.service;

import com.pdfjet.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class PdfService {

    /**
     * Genera un PDF a partir de una lista de datos.
     *
     * @param items      Lista de objetos que se incluirán en el PDF.
     * @param headers    Encabezados de la tabla del PDF.
     * @param rowMapper  Función que convierte cada objeto en una fila de datos.
     * @param isMacetaOrTierraOrDeco    Indicador si los datos son de macetas o tierra.
     * @return           Array de bytes del PDF generado.
     * @throws IOException En caso de error durante la creación del PDF.
     */
    public byte[] generarPDF(List<?> items, String[] headers, Function<Object, String[]> rowMapper, boolean isMacetaOrTierraOrDeco) throws IOException, Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDF pdf = new PDF(outputStream);
            Font font = new Font(pdf, CoreFont.HELVETICA);
            Font dateFont = new Font(pdf, CoreFont.HELVETICA_BOLD);

            int itemsPerPage = isMacetaOrTierraOrDeco ? 38 : 76; // Total items per page (38 per table for non-maceta/tierra, single table for maceta/tierra)
            int totalItems = items.size();
            int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy", new Locale("es", "ES"));
            String currentDate = sdf.format(new Date());

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                Page page = new Page(pdf, Letter.PORTRAIT);

                // Agregar la fecha en la parte superior de la página
                TextLine dateText = new TextLine(dateFont, currentDate);
                dateText.setPosition(50, 30);
                dateText.drawOn(page);

                if (isMacetaOrTierraOrDeco) {
                    // Crear la tabla única para maceta o tierra
                    Table table = new Table();
                    List<List<Cell>> tableData = new java.util.ArrayList<>();

                    // Agregar encabezados a la tabla
                    List<Cell> headerRow = new java.util.ArrayList<>();
                    for (String header : headers) {
                        Cell cell = new Cell(font, header);
                        cell.setTextAlignment(Align.CENTER);
                        cell.setBgColor(Color.lightgray);
                        headerRow.add(cell);
                    }
                    tableData.add(headerRow);

                    // Calcular los límites para esta página
                    int start = pageIndex * itemsPerPage;
                    int end = Math.min(start + itemsPerPage, totalItems);

                    // Agregar filas de datos a la tabla
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

                    // Configurar y dibujar la tabla
                    table.setData(tableData);
                    table.setCellBordersWidth(0.5f);
                    table.autoAdjustColumnWidths();
                    table.setPosition(50, 50);
                    table.drawOn(page);
                } else {
                    // Crear las dos tablas para los demás
                    Table tableLeft = new Table();
                    Table tableRight = new Table();
                    List<List<Cell>> tableDataLeft = new java.util.ArrayList<>();
                    List<List<Cell>> tableDataRight = new java.util.ArrayList<>();

                    // Agregar encabezados a ambas tablas
                    List<Cell> headerRowLeft = new java.util.ArrayList<>();
                    List<Cell> headerRowRight = new java.util.ArrayList<>();
                    for (String header : headers) {
                        Cell cellLeft = new Cell(font, header);
                        cellLeft.setTextAlignment(Align.CENTER);
                        cellLeft.setBgColor(Color.lightgray);
                        headerRowLeft.add(cellLeft);

                        Cell cellRight = new Cell(font, header);
                        cellRight.setTextAlignment(Align.CENTER);
                        cellRight.setBgColor(Color.lightgray);
                        headerRowRight.add(cellRight);
                    }
                    tableDataLeft.add(headerRowLeft);
                    tableDataRight.add(headerRowRight);

                    // Calcular los límites para esta página
                    int start = pageIndex * itemsPerPage;
                    int end = Math.min(start + itemsPerPage, totalItems);

                    // Agregar filas de datos a las tablas
                    for (int i = start; i < end; i++) {
                        String[] row = rowMapper.apply(items.get(i));
                        List<Cell> dataRow = new java.util.ArrayList<>();
                        for (String cellValue : row) {
                            Cell cell = new Cell(font, cellValue);
                            cell.setTextAlignment(Align.LEFT);
                            dataRow.add(cell);
                        }

                        if (i % itemsPerPage < 38) {
                            tableDataLeft.add(dataRow);
                        } else {
                            tableDataRight.add(dataRow);
                        }
                    }

                    // Configurar y dibujar las tablas
                    tableLeft.setData(tableDataLeft);
                    tableLeft.setCellBordersWidth(0.5f);
                    tableLeft.autoAdjustColumnWidths();
                    tableLeft.setPosition(50, 50);
                    tableLeft.drawOn(page);

                    tableRight.setData(tableDataRight);
                    tableRight.setCellBordersWidth(0.5f);
                    tableRight.autoAdjustColumnWidths();
                    tableRight.setPosition(page.getWidth() / 2 + 20, 50);
                    tableRight.drawOn(page);
                }
            }

            pdf.flush();
            return outputStream.toByteArray();
        }
    }
}
