package com.saferoute.constants;

import com.saferoute.model.enums.EstadoPedidoEnum;

/**
 * Constantes para el módulo de Reportes
 */
public final class ReporteConstants {

    private ReporteConstants() {
        // Constructor privado para evitar instanciación
    }

    // Tipos de agrupación
    public static final String AGRUPACION_TRIMESTRAL = "TRIMESTRAL";
    public static final String AGRUPACION_ANUAL = "ANUAL";

    // Mensajes de log
    public static final String LOG_GENERANDO_REPORTE_INGRESOS = " Generando reporte de ingresos desde %s hasta %s - Agrupación: %s";
    public static final String LOG_REPORTE_GENERADO = " Reporte generado: %d solicitudes, Ingresos: %s, Ganancia: %s";
    public static final String LOG_SIN_SOLICITUDES = " No se encontraron solicitudes pagadas en el rango especificado";
    public static final String LOG_PRODUCTOS_MAS_VENDIDOS = "Obteniendo top %d productos más vendidos";
    public static final String LOG_PRODUCTOS_MAYOR_GANANCIA = " Obteniendo top %d productos con mayor ganancia";
    public static final String LOG_CLIENTES_FRECUENTES = " Obteniendo top %d clientes frecuentes";
    public static final String LOG_PEDIDOS_EN_CURSO = " Obteniendo pedidos en curso";
    public static final String LOG_RESUMEN_ESTADISTICAS = " Generando resumen de estadísticas generales";
    public static final String LOG_RESULTADO_GENERADO = " %s obtenido";
    public static final String LOG_PEDIDOS_EN_CURSO_COUNT = " %d pedidos en curso obtenidos";

    // Límites por defecto
    public static final int LIMITE_DEFAULT_PRODUCTOS = 10;
    public static final int LIMITE_DEFAULT_CLIENTES = 20;

    // Formatos
    public static final String FORMATO_TRIMESTRE = "%d-Q%d";
    public static final String FORMATO_ANIO = "%d";

    // Traducciones de estados de pedido
    public static String traducirEstadoPedido(EstadoPedidoEnum estado) {
        return switch (estado) {
            case CRT -> "Creado";
            case ACT -> "Activo";
            case RTA -> "En Ruta";
            case ADU -> "En Aduanas";
            case ENT -> "Entregado";
            case CRM -> "Cancelado por Manager";
            case CRA -> "Cancelado por Admin";
            case PRD -> "Perdido";
            case RCP -> "Recuperado";
        };
    }
}
