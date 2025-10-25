package com.saferoute.constants;

/**
 * Constantes para el módulo de Solicitudes
 */
public final class SolicitudConstants {

    private SolicitudConstants() {
        // Constructor privado para evitar instanciación
    }

    // Mensajes de error
    public static final String ERROR_SOLICITUD_NO_ENCONTRADA = "Solicitud no encontrada";
    public static final String ERROR_CLIENTE_NO_ENCONTRADO = "Cliente no encontrado";
    public static final String ERROR_PEDIDO_NO_ENCONTRADO = "Pedido no encontrado";
    public static final String ERROR_PRODUCTO_NO_ENCONTRADO = "Producto no encontrado";
    public static final String ERROR_PRODUCTO_NO_EN_PEDIDO = "El producto no está disponible en el pedido asociado";
    public static final String ERROR_PRODUCTO_YA_EXISTE = "El producto ya existe en esta solicitud. Use modificar cantidad en su lugar.";
    public static final String ERROR_PRODUCTO_YA_EN_SOLICITUD = "El producto ya existe en esta solicitud. Use modificar cantidad en su lugar.";
    public static final String ERROR_ROL_CLI_NO_ENCONTRADO = "Rol CLI no encontrado";
    public static final String ERROR_ESTADO_NO_VALIDO = "Estado no válido. Solo se permite PGD, PDP o CAN";

    // Mensajes de log
    public static final String LOG_SOLICITUD_CREADA = "Solicitud creada - ID: %d, Pedido ID: %d, Productos: %d";
    public static final String LOG_PRODUCTO_AGREGADO = "Producto agregado a solicitud - Solicitud ID: %d, Producto: %s, Cantidad: %d, Modificaciones restantes: %d";
    public static final String LOG_PRODUCTO_ELIMINADO = "Producto eliminado de solicitud - Solicitud ID: %d, Producto: %s, Modificaciones restantes: %d";
    public static final String LOG_SOLICITUD_CANCELADA = "Solicitud cancelada - ID: %d, Pedido ID: %d";

    // Roles
    public static final String ROL_CLIENTE = "CLI";

    // Estados permitidos
    public static final String ESTADO_PGD = "PGD";
    public static final String ESTADO_PDP = "PDP";
    public static final String ESTADO_CAN = "CAN";
}
