package com.saferoute.service.interfaces;

import java.io.ByteArrayOutputStream;

/**
 * 📊 SERVICIO DE EXPORTACIÓN A EXCEL
 * 
 * Interface que define las operaciones para generar informes contables en
 * formato Excel.
 * Genera archivos .xlsx con formato profesional para:
 * - Informes de compras de clientes
 * - Informes de productos consolidados por pedido
 * 
 * RF007: Exportación a Excel
 * 
 * @author SafeRoute Team
 * @version 2.0
 * @since 2025-01-24
 */
public interface IExcelService {

    /**
     * 📄 Genera un informe Excel de las compras de un cliente específico
     * 
     * Estructura del Excel:
     * - Título: "Informe de Compras - {Nombre Cliente}"
     * - Columnas: Fecha | Producto | Cantidad | Precio Unitario | Precio Total
     * - Solo incluye solicitudes pagadas (PGD)
     * - Ordenado por fecha descendente
     * 
     * @param idCliente ID del cliente
     * @return ByteArrayOutputStream con el archivo Excel generado
     * @throws Exception si ocurre un error al generar el Excel
     */
    ByteArrayOutputStream generarInformeCliente(Integer idCliente) throws Exception;

    /**
     * 📊 Genera un informe Excel de productos consolidados de un pedido entregado
     * 
     * Estructura del Excel:
     * - Título: "Informe de Pedido #{idPedido}"
     * - Columnas: Código Producto | Fecha | Nombre Producto | Cantidad | Precio
     * Unitario | Precio Total
     * - Solo incluye pedidos con estado ENT (Entregado)
     * - Solo incluye solicitudes pagadas (PGD)
     * - Productos consolidados (suma de cantidades del mismo producto)
     * - Ordenado por código de producto
     * 
     * @param idPedido ID del pedido
     * @return ByteArrayOutputStream con el archivo Excel generado
     * @throws Exception si ocurre un error al generar el Excel o si el pedido no
     *                   está entregado
     */
    ByteArrayOutputStream generarInformePedidoConsolidado(Integer idPedido) throws Exception;
}
