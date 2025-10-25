package com.saferoute.helper;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

/**
 * Helper para la creación de estilos de celdas en documentos Excel.
 * Centraliza la creación de todos los estilos utilizados en los informes.
 */
@Component
public class ExcelStyleHelper {

    /**
     * Crea estilo para el título principal del documento.
     */
    public CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Crea estilo para los headers de las tablas.
     */
    public CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(style);
        return style;
    }

    /**
     * Crea estilo normal para celdas de datos.
     */
    public CellStyle crearEstiloNormal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        aplicarBordes(style);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Crea estilo para celdas con formato de moneda.
     */
    public CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = crearEstiloNormal(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }

    /**
     * Crea estilo para celdas con formato de fecha.
     */
    public CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = crearEstiloNormal(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Crea estilo para la fila de totales.
     */
    public CellStyle crearEstiloTotal(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.DOUBLE);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }

    /**
     * Aplica bordes finos a todas las direcciones de una celda.
     */
    private void aplicarBordes(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
