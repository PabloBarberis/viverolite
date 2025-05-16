package com.vivero.viveroApp.service;

import com.vivero.viveroApp.model.IngresoEgreso;
import com.vivero.viveroApp.model.RegistroHorario;
import java.io.IOException;
import java.util.List;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.UnitValue;
import com.vivero.viveroApp.dto.ProductoDTO;
import com.vivero.viveroApp.model.Producto;
import com.vivero.viveroApp.model.enums.MetodoPago;
import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PdfService {

    /**
     * @param productos
     *
     * @return Array de bytes del PDF generado.
     * @throws IOException En caso de error durante la creación del PDF.
     */
    public byte[] generarPDFPorProducto(List<Producto> productos) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);

        try (Document document = new Document(pdf)) {
            Table table = new Table(new float[]{1, 4, 2, 2, 2}); // 5 columnas
            table.addHeaderCell(new Cell().add(new Paragraph("ID")));
            table.addHeaderCell(new Cell().add(new Paragraph("Nombre")));
            table.addHeaderCell(new Cell().add(new Paragraph("Marca")));
            table.addHeaderCell(new Cell().add(new Paragraph("Precio")));
            table.addHeaderCell(new Cell().add(new Paragraph("Stock")));

            for (Producto producto : productos) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(producto.getId()))));
                table.addCell(new Cell().add(new Paragraph(producto.getNombre())));
                table.addCell(new Cell().add(new Paragraph(producto.getMarca())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(producto.getPrecio()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(producto.getStock()))));
            }
            document.add(table);
        }
        return baos.toByteArray();
    }

    public byte[] generarReporteVentas(
            Map<String, Integer> productosVendidos,
            Map<DayOfWeek, Integer> ventasPorDia,
            Map<String, Integer> comprasPorCliente,
            Map<String, Double> gastoPorCliente,
            double totalVentas,
            double totalGastos,
            double granTotal,
            int mes,
            int anio,
            Map<MetodoPago, Double> totales) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);

        try (Document document = new Document(pdf)) {
            document.add(new Paragraph("Reporte de Ventas - " + mes + "/" + anio).setFontSize(18));

            document.add(new Paragraph("Productos vendidos del mes"));
            Table productosTable = new Table(new float[]{3, 1});
            productosTable.addHeaderCell(new Cell().add(new Paragraph("Producto")));
            productosTable.addHeaderCell(new Cell().add(new Paragraph("Cantidad")));
            productosVendidos.forEach((producto, cantidad) -> {
                productosTable.addCell(new Cell().add(new Paragraph(producto)));
                productosTable.addCell(new Cell().add(new Paragraph(cantidad.toString())));
            });
            document.add(productosTable);

            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Ventas por día de la semana"));
            Table ventasDiaTable = new Table(new float[]{2, 1});
            ventasDiaTable.addHeaderCell(new Cell().add(new Paragraph("Día")));
            ventasDiaTable.addHeaderCell(new Cell().add(new Paragraph("Cantidad")));
            for (DayOfWeek dia : DayOfWeek.values()) {
                String diaEsp = "";
                switch (dia) {
                    case MONDAY ->
                        diaEsp = "Lunes";
                    case TUESDAY ->
                        diaEsp = "Martes";
                    case WEDNESDAY ->
                        diaEsp = "Miércoles";
                    case THURSDAY ->
                        diaEsp = "Jueves";
                    case FRIDAY ->
                        diaEsp = "Viernes";
                    case SATURDAY ->
                        diaEsp = "Sábado";
                    case SUNDAY ->
                        diaEsp = "Domingo";
                    default ->
                        throw new AssertionError();
                }

                ventasDiaTable.addCell(new Cell().add(new Paragraph(diaEsp)));
                ventasDiaTable.addCell(new Cell().add(new Paragraph(ventasPorDia.getOrDefault(dia, 0).toString())));
            }
            document.add(ventasDiaTable);

            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Cantidad de compras por cliente"));
            Table clienteTable = new Table(new float[]{3, 1, 2});
            clienteTable.addHeaderCell(new Cell().add(new Paragraph("Cliente")));
            clienteTable.addHeaderCell(new Cell().add(new Paragraph("Cant. Compras")));
            clienteTable.addHeaderCell(new Cell().add(new Paragraph("Gasto ($)")));
            comprasPorCliente.forEach((cliente, compras) -> {
                Double gasto = gastoPorCliente.getOrDefault(cliente, 0.0);
                clienteTable.addCell(new Cell().add(new Paragraph(cliente)));
                clienteTable.addCell(new Cell().add(new Paragraph(compras.toString())));
                clienteTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", gasto))));
            });
            document.add(clienteTable);

            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Totales"));
            Table totalesTable = new Table(new float[]{3, 2});

            totalesTable.addCell(new Cell().add(new Paragraph("Efectivo ($):")));
            totalesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", totales.getOrDefault(MetodoPago.EFECTIVO, 0.0)))));
            totalesTable.addCell(new Cell().add(new Paragraph("Débito ($):")));
            totalesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", totales.getOrDefault(MetodoPago.DEBITO, 0.0)))));
            totalesTable.addCell(new Cell().add(new Paragraph("Crédito ($):")));
            totalesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", totales.getOrDefault(MetodoPago.CREDITO, 0.0)))));
            totalesTable.addCell(new Cell().add(new Paragraph("MP Vale ($):")));
            totalesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", totales.getOrDefault(MetodoPago.MERCADOPAGO_VAL, 0.0)))));
            totalesTable.addCell(new Cell().add(new Paragraph("MP Sacha ($):")));
            totalesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", totales.getOrDefault(MetodoPago.MERCADOPAGO_SAC, 0.0)))));
            totalesTable.addCell(new Cell().add(new Paragraph("Total Ventas ($):")));
            totalesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", totalVentas))));
            totalesTable.addCell(new Cell().add(new Paragraph("Total Gastos ($):")));
            totalesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", totalGastos))));

            totalesTable.addCell(new Cell().add(new Paragraph("Total Final ($):")));
            totalesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", granTotal))));
            document.add(totalesTable);

        }
        return baos.toByteArray();
    }

    public byte[] generarPdf(List<RegistroHorario> registros, List<IngresoEgreso> ingresosEgresos) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);

        try (Document document = new Document(pdf)) {
            document.add(new Paragraph("Registro de horas"));

            Table tableRegistros = new Table(new float[]{1, 1, 1, 1, 1, 1, 1, 1, 1});
            tableRegistros.addHeaderCell(new Cell().add(new Paragraph("Fecha")));
            tableRegistros.addHeaderCell(new Cell().add(new Paragraph("E. TM")));
            tableRegistros.addHeaderCell(new Cell().add(new Paragraph("S. TM")));
            tableRegistros.addHeaderCell(new Cell().add(new Paragraph("E. TT")));
            tableRegistros.addHeaderCell(new Cell().add(new Paragraph("S. TT")));
            tableRegistros.addHeaderCell(new Cell().add(new Paragraph("Horas")));
            tableRegistros.addHeaderCell(new Cell().add(new Paragraph("Feriado")));
            tableRegistros.addHeaderCell(new Cell().add(new Paragraph("Precio hora")));
            tableRegistros.addHeaderCell(new Cell().add(new Paragraph("Total ($)")));

            double sumaTotalFinal = 0;

            for (RegistroHorario registro : registros) {
                tableRegistros.addCell(new Cell().add(new Paragraph(String.valueOf(registro.getFecha()))));
                tableRegistros.addCell(new Cell().add(new Paragraph(registro.getEntradaTM())));
                tableRegistros.addCell(new Cell().add(new Paragraph(registro.getSalidaTM())));
                tableRegistros.addCell(new Cell().add(new Paragraph(registro.getEntradaTT())));
                tableRegistros.addCell(new Cell().add(new Paragraph(registro.getSalidaTT())));

                double horas = registro.getTotalHoras();
                double precioHora = registro.getPrecioHora();
                tableRegistros.addCell(new Cell().add(new Paragraph(String.format("%.2f", horas))));

                String feriado = registro.isFeriado() ? "Sí" : "No";
                tableRegistros.addCell(new Cell().add(new Paragraph(feriado)));
                tableRegistros.addCell(new Cell().add(new Paragraph(String.format("%.2f", precioHora))));

                double multiplicador = registro.isFeriado() ? 2 : 1;
                double total = horas * precioHora * multiplicador;
                sumaTotalFinal += total;
                tableRegistros.addCell(new Cell().add(new Paragraph(String.format("%.2f", total))));
            }

            document.add(tableRegistros);

            Table footerRegistros = new Table(1);
            footerRegistros.addCell(new Cell().add(new Paragraph("Total Horas Trabajadas ($): " + String.format("%.2f", sumaTotalFinal))));
            document.add(footerRegistros);

            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Adelantos"));

            Table tableIngresos = new Table(new float[]{1, 4, 2, 2});
            tableIngresos.addHeaderCell(new Cell().add(new Paragraph("ID")));
            tableIngresos.addHeaderCell(new Cell().add(new Paragraph("Descripción")));
            tableIngresos.addHeaderCell(new Cell().add(new Paragraph("Método de Pago")));
            tableIngresos.addHeaderCell(new Cell().add(new Paragraph("Monto ($)")));

            double sumaMontos = 0;

            for (IngresoEgreso ingreso : ingresosEgresos) {
                tableIngresos.addCell(new Cell().add(new Paragraph(String.valueOf(ingreso.getId()))));
                tableIngresos.addCell(new Cell().add(new Paragraph(ingreso.getDescripcion())));
                tableIngresos.addCell(new Cell().add(new Paragraph(ingreso.getMetodoPago().name())));
                tableIngresos.addCell(new Cell().add(new Paragraph(String.format("%.2f", ingreso.getMonto()))));
                sumaMontos += ingreso.getMonto();
            }

            document.add(tableIngresos);

            Table footerIngresos = new Table(1);
            footerIngresos.addCell(new Cell().add(new Paragraph("Total Adelantos ($): " + String.format("%.2f", sumaMontos))));
            document.add(footerIngresos);

            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Totales"));
            Table resumenFinal = new Table(1);
            resumenFinal.addCell(new Cell().add(new Paragraph("Total Horas Trabajadas ($): " + String.format("%.2f", sumaTotalFinal))));
            resumenFinal.addCell(new Cell().add(new Paragraph("Total Adelantos ($): " + String.format("%.2f", sumaMontos))));
            double totalFinal = sumaTotalFinal - sumaMontos;
            resumenFinal.addCell(new Cell().add(new Paragraph("Total Final ($): " + String.format("%.2f", totalFinal))));
            document.add(resumenFinal);
        }

        return baos.toByteArray();
    }

    public byte[] generarReporteProductosVendidos(
        Map<String, Map<String, Integer>> productosPorProveedor,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin) throws Exception {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PdfWriter writer = new PdfWriter(baos);
    PdfDocument pdf = new PdfDocument(writer);

    try (Document document = new Document(pdf)) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String titulo = "Productos Vendidos del "
                + fechaInicio.format(formatter) + " al "
                + fechaFin.format(formatter);

        document.add(new Paragraph(titulo).setFontSize(16));
        document.add(new Paragraph("\n"));

        for (Map.Entry<String, Map<String, Integer>> entry : productosPorProveedor.entrySet()) {
            String nombreProveedor = entry.getKey();
            Map<String, Integer> productos = entry.getValue();

            // Título de la tabla con el nombre del proveedor
            document.add(new Paragraph("Proveedor: " + nombreProveedor)
                    .setFontSize(14)
                    
                    .setMarginTop(15)
                    .setMarginBottom(5));

            Table tabla = new Table(new float[]{4, 2});
            
            tabla.addHeaderCell(new Cell().add(new Paragraph("Producto")));
            tabla.addHeaderCell(new Cell().add(new Paragraph("Cantidad")));

            for (Map.Entry<String, Integer> productoEntry : productos.entrySet()) {
                tabla.addCell(new Cell().add(new Paragraph(productoEntry.getKey())));
                tabla.addCell(new Cell().add(new Paragraph(String.valueOf(productoEntry.getValue()))));
            }

            document.add(tabla);
        }
    }

    return baos.toByteArray();
}


    public byte[] generarPedidoPDF(List<ProductoDTO> productos) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);

        try (Document document = new Document(pdf)) {
            // Fecha actual
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechaActual = LocalDate.now().format(formatter);
            document.add(new Paragraph("Fecha: " + fechaActual).setFontSize(14));
            document.add(new Paragraph("\n"));

            // Tabla de productos
            Table tabla = new Table(UnitValue.createPercentArray(new float[]{2, 5, 2})).useAllAvailableWidth();
            tabla.addHeaderCell(new Cell().add(new Paragraph("ID")));
            tabla.addHeaderCell(new Cell().add(new Paragraph("Nombre")));
            tabla.addHeaderCell(new Cell().add(new Paragraph("Cantidad")));

            for (ProductoDTO producto : productos) {
                tabla.addCell(new Cell().add(new Paragraph(producto.getId().toString())));
                tabla.addCell(new Cell().add(new Paragraph(producto.getNombre())));
                tabla.addCell(new Cell().add(new Paragraph(producto.getCantidad().toString())));
            }

            document.add(tabla);
        }

        return baos.toByteArray();
    }
}
