package com.saferoute.constants;

import com.itextpdf.kernel.colors.DeviceRgb;

/**
 * Constantes para el servicio de generación de Etiquetas PDF.
 */
public final class EtiquetaConstants {

    private EtiquetaConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes");
    }

    // ========== MENSAJES DE LOG ==========
    public static final String LOG_GENERANDO_ETIQUETAS_JSON = "🏷️ Generando etiquetas JSON para pedido ID: {}";
    public static final String LOG_ETIQUETAS_GENERADAS = "✅ Generadas {} etiquetas para pedido #{}";
    public static final String LOG_GENERANDO_ETIQUETAS_PDF = "📄 Generando PDF de etiquetas para pedido ID: {}";
    public static final String LOG_PDF_GENERADO = "✅ PDF de etiquetas generado exitosamente - {} bytes";
    public static final String LOG_ERROR_GENERANDO_PDF = "❌ Error al generar PDF de etiquetas: {}";
    public static final String LOG_PEDIDO_NO_ENTREGADO = "⚠️ Pedido {} no está en estado ENTREGADO (estado actual: {})";

    // ========== MENSAJES DE ERROR ==========
    public static final String ERROR_PEDIDO_NO_ENCONTRADO = "Pedido no encontrado";
    public static final String ERROR_PEDIDO_SIN_SOLICITUDES = "El pedido no tiene solicitudes";
    public static final String ERROR_PEDIDO_NO_ENTREGADO = "Solo se pueden generar etiquetas para pedidos entregados (estado ENT)";

    // ========== TEXTOS DE ETIQUETA ==========
    public static final String TEXTO_SAFE_ROUTE = "🚚 SAFE ROUTE";
    public static final String TEXTO_SEPARADOR = "━━━━━━━━━━━━━━━━━━━━━━";
    public static final String TEXTO_CLIENTE = "Cliente:";
    public static final String TEXTO_DIRECCION = "Dirección:";
    public static final String TEXTO_CONTACTO = "Contacto:";
    public static final String TEXTO_NUMERO_ETIQUETA = "Etiqueta %d de %d";

    // ========== TÍTULOS Y HEADERS ==========
    public static final String TITULO_ETIQUETAS_ENTREGA = "ETIQUETAS DE ENTREGA - PEDIDO #";
    public static final String FOOTER_TOTAL_ETIQUETAS = "Total de etiquetas: ";

    // ========== FORMATO DE FECHA ==========
    public static final String FORMATO_FECHA = "dd/MM/yyyy";

    // ========== DIMENSIONES PDF ==========
    public static final float MARGEN_DOCUMENTO = 20f;
    public static final int COLUMNAS_POR_FILA = 2;

    // ========== TAMAÑOS DE FUENTE ==========
    public static final int FONT_SIZE_TITULO_DOC = 14;
    public static final int FONT_SIZE_TITULO_ETIQUETA = 16;
    public static final int FONT_SIZE_SEPARADOR = 8;
    public static final int FONT_SIZE_LABEL = 11;
    public static final int FONT_SIZE_TEXTO = 12;
    public static final int FONT_SIZE_FOOTER = 8;

    // ========== ESPACIADOS ==========
    public static final float MARGEN_BOTTOM_TITULO = 15f;
    public static final float MARGEN_BOTTOM_SAFEROUTE = 8f;
    public static final float MARGEN_BOTTOM_SEPARADOR = 8f;
    public static final float MARGEN_BOTTOM_CAMPO = 5f;
    public static final float MARGEN_BOTTOM_CONTACTO = 8f;
    public static final float MARGEN_TOP_FOOTER = 10f;
    public static final float PADDING_CELDA = 10f;
    public static final float MARGEN_CELDA = 5f;

    // ========== DIMENSIONES CELDA ==========
    public static final float MIN_HEIGHT_CELDA = 120f;
    public static final float BORDER_WIDTH = 1f;

    // ========== COLORES ==========
    public static final DeviceRgb COLOR_SAFEROUTE = new DeviceRgb(228, 107, 107);
}
