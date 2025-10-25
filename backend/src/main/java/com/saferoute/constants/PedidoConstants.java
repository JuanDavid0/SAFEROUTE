package com.saferoute.constants;

import com.saferoute.model.enums.EstadoPedidoEnum;

/**
 * Constantes para el módulo de Pedidos
 */
public final class PedidoConstants {

    private PedidoConstants() {
        // Constructor privado para evitar instanciación
    }

    // Mensajes de error
    public static final String ERROR_PEDIDO_NO_ENCONTRADO = "Pedido no encontrado";
    public static final String ERROR_ADMIN_NO_ENCONTRADO = "Administrador no encontrado";
    public static final String ERROR_PRODUCTO_NO_ENCONTRADO = "Producto no encontrado";
    public static final String ERROR_PRODUCTO_YA_EXISTE = "El producto ya existe en este pedido. Use modificar para cambiar cantidades.";
    public static final String ERROR_PRODUCTO_NO_EN_PEDIDO = "Producto no encontrado en este pedido";
    public static final String ERROR_HASH_VACIO = "El hash no puede estar vacío";
    public static final String ERROR_HASH_INVALIDO = "Hash inválido";

    // Mensajes de validación de estado
    public static final String ERROR_SOLO_ESTADO_CRT = "Solo se pueden %s productos de pedidos en estado CREADO (CRT). Estado actual: %s";
    public static final String ERROR_PEDIDO_NO_MODIFICABLE = "No se puede modificar el pedido en estado: %s";
    public static final String ERROR_PEDIDO_ACTIVO_SOLO_FECHA = "El pedido está en estado ACTIVO. Solo se puede modificar la fecha de cierre";
    public static final String ERROR_HASH_SOLO_ACTIVO = "Solo se puede generar URL hash para pedidos en estado ACTIVO";
    public static final String ERROR_PEDIDO_NO_DISPONIBLE = "Este pedido ya no está disponible para nuevas solicitudes (Estado: %s)";

    // Mensajes de transición
    public static final String ERROR_TRANSICION_INVALIDA = "Transición de estado inválida: no se puede cambiar de %s a %s. Estados permitidos desde %s: %s";
    public static final String MSG_NINGUNO_ESTADO_FINAL = "ninguno (estado final)";

    // Mensajes de log
    public static final String LOG_PEDIDO_CREADO = "Pedido creado - ID: %d, Estado: %s, Productos: %d";
    public static final String LOG_HASH_GENERADO = "URL hash generada para pedido ID: %d - Hash: %s";

    // Estados permitidos para operaciones
    public static final EstadoPedidoEnum ESTADO_CREADO = EstadoPedidoEnum.CRT;
    public static final EstadoPedidoEnum ESTADO_ACTIVO = EstadoPedidoEnum.ACT;
    public static final EstadoPedidoEnum ESTADO_CERRADO_MANUAL = EstadoPedidoEnum.CRM;
}
