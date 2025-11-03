package com.saferoute.helper;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.saferoute.constants.EtiquetaConstants;
import com.saferoute.dto.EtiquetaDTO;
import org.springframework.stereotype.Component;

/**
 * Helper para la creación de celdas de etiquetas en PDF.
 */
@Component
public class EtiquetaPdfHelper {

    /**
     * Crea una celda de etiqueta individual con diseño profesional.
     *
     * @param etiqueta Datos de la etiqueta
     * @return Celda formateada para el PDF
     */
    public Cell crearCeldaEtiqueta(EtiquetaDTO etiqueta) {
        Cell cell = crearCeldaBase();

        cell.add(crearTituloSafeRoute());
        cell.add(crearLineaSeparadora());
        cell.add(crearCampoCliente(etiqueta.getNombreCliente()));
        cell.add(crearCampoDireccion(etiqueta.getDireccion()));
        cell.add(crearCampoCedula(etiqueta.getCedula()));
        cell.add(crearCampoContacto(etiqueta.getTelefono()));

        return cell;
    }

    /**
     * Crea la celda base con estilos y bordes.
     */
    private Cell crearCeldaBase() {
        Cell cell = new Cell();
        cell.setPadding(EtiquetaConstants.PADDING_CELDA);
        cell.setMargin(EtiquetaConstants.MARGEN_CELDA);
        cell.setBorder(new SolidBorder(ColorConstants.BLACK, EtiquetaConstants.BORDER_WIDTH));
        cell.setMinHeight(EtiquetaConstants.MIN_HEIGHT_CELDA);
        cell.setVerticalAlignment(VerticalAlignment.TOP);
        return cell;
    }

    /**
     * Crea el título SafeRoute de la etiqueta.
     */
    private Paragraph crearTituloSafeRoute() {
        return new Paragraph(EtiquetaConstants.TEXTO_SAFE_ROUTE)
                .setFontSize(EtiquetaConstants.FONT_SIZE_TITULO_ETIQUETA)
                .setBold()
                .setFontColor(EtiquetaConstants.COLOR_SAFEROUTE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(EtiquetaConstants.MARGEN_BOTTOM_SAFEROUTE);
    }

    /**
     * Crea la línea separadora decorativa.
     */
    private Paragraph crearLineaSeparadora() {
        return new Paragraph(EtiquetaConstants.TEXTO_SEPARADOR)
                .setFontSize(EtiquetaConstants.FONT_SIZE_SEPARADOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(EtiquetaConstants.MARGEN_BOTTOM_SEPARADOR);
    }

    /**
     * Crea el campo de cliente con label y valor.
     */
    private Paragraph crearCampoCliente(String nombreCliente) {
        return new Paragraph()
                .add(new Paragraph(EtiquetaConstants.TEXTO_CLIENTE)
                        .setBold()
                        .setFontSize(EtiquetaConstants.FONT_SIZE_LABEL))
                .add(new Paragraph(nombreCliente)
                        .setFontSize(EtiquetaConstants.FONT_SIZE_TEXTO))
                .setMarginBottom(EtiquetaConstants.MARGEN_BOTTOM_CAMPO);
    }

    /**
     * Crea el campo de dirección con label y valor.
     */
    private Paragraph crearCampoDireccion(String direccion) {
        return new Paragraph()
                .add(new Paragraph(EtiquetaConstants.TEXTO_DIRECCION)
                        .setBold()
                        .setFontSize(EtiquetaConstants.FONT_SIZE_LABEL))
                .add(new Paragraph(direccion)
                        .setFontSize(EtiquetaConstants.FONT_SIZE_TEXTO))
                .setMarginBottom(EtiquetaConstants.MARGEN_BOTTOM_CAMPO);
    }

    private Paragraph crearCampoCedula(String cedula) {
        return new Paragraph()
                .add(new Paragraph(EtiquetaConstants.TEXTO_CEDULA)
                        .setBold()
                        .setFontSize(EtiquetaConstants.FONT_SIZE_LABEL))
                .add(new Paragraph(cedula)
                        .setFontSize(EtiquetaConstants.FONT_SIZE_TEXTO))
                .setMarginBottom(EtiquetaConstants.MARGEN_BOTTOM_CAMPO);
    }

    /**
     * Crea el campo de contacto con label y valor.
     */
    private Paragraph crearCampoContacto(String telefono) {
        return new Paragraph()
                .add(new Paragraph(EtiquetaConstants.TEXTO_CONTACTO)
                        .setBold()
                        .setFontSize(EtiquetaConstants.FONT_SIZE_LABEL))
                .add(new Paragraph(telefono)
                        .setFontSize(EtiquetaConstants.FONT_SIZE_TEXTO))
                .setMarginBottom(EtiquetaConstants.MARGEN_BOTTOM_CONTACTO);
    }

    /**
     * Crea una celda vacía para rellenar espacios en la cuadrícula.
     */
    public Cell crearCeldaVacia() {
        return new Cell().setBorder(null);
    }
}
