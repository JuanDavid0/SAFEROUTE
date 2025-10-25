package com.saferoute.constants;

/**
 * Constantes para el servicio de gestión de Productos.
 */
public final class ProductoConstants {

    private ProductoConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes");
    }

    // ========== MENSAJES DE LOG ==========
    public static final String LOG_PRODUCTO_CREADO = "📦 Producto creado - ID: {}, Nombre: {}, Precio: ${}";
    public static final String LOG_PRODUCTO_ACTUALIZADO = "✏️ Producto actualizado - ID: {}, Nombre: {}";
    public static final String LOG_PRODUCTO_ELIMINADO = "🗑️ Producto eliminado (soft delete) - ID: {}, Nombre: {}";

    // ========== MENSAJES DE ERROR ==========
    public static final String ERROR_PRODUCTO_NO_ENCONTRADO = "Producto no encontrado";
    public static final String ERROR_PRODUCTO_NO_ENCONTRADO_POR_ID = "Producto no encontrado con ID: %d";
    public static final String ERROR_PRODUCTO_NO_ENCONTRADO_POR_NOMBRE = "Producto no encontrado con nombre: %s";

    // ========== ESTADOS DE PRODUCTO ==========
    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_INACTIVO = "INACTIVO";

    // ========== VALORES POR DEFECTO ==========
    public static final String PRINCIPAL_ANONIMO = "anonymousUser";
}
