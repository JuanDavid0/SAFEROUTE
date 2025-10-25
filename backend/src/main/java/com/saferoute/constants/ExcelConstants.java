package com.saferoute.constants;

/**
 * Constantes para el servicio de generación de Excel.
 */
public final class ExcelConstants {

    private ExcelConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes");
    }

    // ========== MENSAJES DE LOG ==========
    public static final String LOG_GENERANDO_INFORME_CLIENTE = " Generando informe de compras para cliente ID: {}";
    public static final String LOG_GENERANDO_INFORME_PEDIDO = " Generando informe consolidado para pedido ID: {}";
    public static final String LOG_INFORME_CLIENTE_GENERADO = " Informe de cliente {} generado exitosamente";
    public static final String LOG_INFORME_PEDIDO_GENERADO = " Informe de pedido {} generado exitosamente";
    public static final String LOG_ERROR_GENERANDO_INFORME_CLIENTE = " Error al generar informe de cliente {}: {}";
    public static final String LOG_ERROR_GENERANDO_INFORME_PEDIDO = " Error al generar informe de pedido {}: {}";
    public static final String LOG_CLIENTE_SIN_COMPRAS = " Cliente {} no tiene solicitudes pagadas";
    public static final String LOG_PEDIDO_SIN_SOLICITUDES = " Pedido {} no tiene solicitudes pagadas";
    public static final String LOG_PEDIDO_ESTADO_INVALIDO = " Pedido {} no está en estado ENTREGADO (estado actual: {})";

    // ========== MENSAJES DE ERROR ==========
    public static final String ERROR_CLIENTE_NO_ENCONTRADO = "Cliente no encontrado";
    public static final String ERROR_PEDIDO_NO_ENCONTRADO = "Pedido no encontrado";
    public static final String ERROR_CLIENTE_SIN_COMPRAS = "El cliente no tiene compras registradas";
    public static final String ERROR_PEDIDO_SIN_SOLICITUDES = "El pedido no tiene solicitudes pagadas";
    public static final String ERROR_PEDIDO_NO_ENTREGADO = "Solo se pueden generar informes de pedidos entregados (estado ENT)";

    // ========== TÍTULOS Y HEADERS ==========
    public static final String TITULO_INFORME_COMPRAS = " INFORME DE COMPRAS - ";
    public static final String TITULO_INFORME_PEDIDO = " INFORME DE PEDIDO #";
    public static final String PREFIJO_INFO_CLIENTE = "Cliente: ";
    public static final String PREFIJO_INFO_CEDULA = " | Cédula: ";
    public static final String PREFIJO_INFO_PEDIDO = "Pedido: #";
    public static final String PREFIJO_INFO_ESTADO = " | Estado: ";
    public static final String PREFIJO_INFO_FECHA = " | Fecha: ";
    public static final String ESTADO_ENTREGADO = "ENTREGADO";
    public static final String LABEL_TOTAL = "TOTAL:";

    // ========== NOMBRES DE COLUMNAS ==========
    public static final String[] HEADERS_INFORME_CLIENTE = {
            "Fecha", "Producto", "Cantidad", "Precio Unitario", "Precio Total"
    };

    public static final String[] HEADERS_INFORME_PEDIDO = {
            "Código Producto", "Fecha", "Nombre Producto", "Cantidad", "Precio Unitario", "Precio Total"
    };

    // ========== NOMBRES DE HOJAS ==========
    public static final String NOMBRE_HOJA_CLIENTE = "Compras Cliente";
    public static final String NOMBRE_HOJA_PEDIDO = "Informe Pedido";

    // ========== ANCHOS DE COLUMNAS (en unidades de 1/256 de un carácter)
    // ==========
    public static final int ANCHO_COLUMNA_FECHA = 3500;
    public static final int ANCHO_COLUMNA_PRODUCTO = 8000;
    public static final int ANCHO_COLUMNA_CANTIDAD = 3000;
    public static final int ANCHO_COLUMNA_PRECIO = 4500;
    public static final int ANCHO_COLUMNA_CODIGO = 4000;

    // ========== FORMATOS ==========
    public static final String FORMATO_FECHA = "dd/MM/yyyy";
    public static final String FORMATO_MONEDA = "$#,##0.00";

    // ========== TAMAÑOS DE FUENTE ==========
    public static final short FONT_SIZE_TITULO = 16;
    public static final short FONT_SIZE_TOTAL = 12;
}
