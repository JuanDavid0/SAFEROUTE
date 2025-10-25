package com.saferoute.helper;

import com.saferoute.constants.ExcelConstants;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Helper para la creación de filas en documentos Excel.
 * Centraliza la lógica de construcción de filas, celdas y formato.
 */
@Component
@RequiredArgsConstructor
public class ExcelRowHelper {

    /**
     * Crea una fila de título con merge de celdas.
     */
    public void crearFilaTitulo(Sheet sheet, int rowNum, String titulo, int columnCount, CellStyle style) {
        Row titleRow = sheet.createRow(rowNum);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(titulo);
        titleCell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, columnCount - 1));
    }

    /**
     * Crea una fila de información con merge de celdas.
     */
    public void crearFilaInfo(Sheet sheet, int rowNum, String info, int columnCount, CellStyle style) {
        Row infoRow = sheet.createRow(rowNum);
        Cell infoCell = infoRow.createCell(0);
        infoCell.setCellValue(info);
        infoCell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, columnCount - 1));
    }

    /**
     * Crea una fila de headers con los nombres de columnas especificados.
     */
    public void crearFilaHeaders(Sheet sheet, int rowNum, String[] headers, CellStyle style) {
        Row headerRow = sheet.createRow(rowNum);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    /**
     * Crea una fila de total con label y valor.
     */
    public void crearFilaTotal(Sheet sheet, int rowNum, int labelColumn, int valueColumn,
            BigDecimal totalValue, CellStyle style) {
        Row totalRow = sheet.createRow(rowNum);

        Cell totalLabelCell = totalRow.createCell(labelColumn);
        totalLabelCell.setCellValue(ExcelConstants.LABEL_TOTAL);
        totalLabelCell.setCellStyle(style);

        Cell totalValueCell = totalRow.createCell(valueColumn);
        totalValueCell.setCellValue(totalValue.doubleValue());
        totalValueCell.setCellStyle(style);
    }

    /**
     * Crea una celda con valor de texto.
     */
    public void crearCeldaTexto(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * Crea una celda con valor numérico.
     */
    public void crearCeldaNumerica(Row row, int column, int value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * Crea una celda con valor de moneda (BigDecimal).
     */
    public void crearCeldaMoneda(Row row, int column, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }
}
