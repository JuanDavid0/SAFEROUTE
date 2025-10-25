package com.saferoute.service.interfaces;

import com.saferoute.dto.EtiquetasResponseDTO;

import java.io.ByteArrayOutputStream;

/**
 * 🏷️ SERVICIO DE GENERACIÓN DE ETIQUETAS DE PEDIDO
 * 
 * Genera etiquetas de entrega para pedidos en estado ENTREGADO.
 * Una etiqueta por cada solicitud del pedido.
 * 
 * Funcionalidades:
 * - Generar etiquetas en formato JSON (para visualización web)
 * - Generar etiquetas en formato PDF (para impresión)
 * 
 * Validaciones:
 * - Solo pedidos en estado ENTREGADO
 * - Pedido debe tener solicitudes
 * 
 * @author SafeRoute Team
 * @version 1.0
 * @since 2025-10-24
 */
public interface IEtiquetaService {

    /**
     * Genera las etiquetas de un pedido en formato JSON
     * 
     * @param idPedido ID del pedido entregado
     * @return DTO con la lista de etiquetas
     * @throws RuntimeException si el pedido no existe o no está entregado
     */
    EtiquetasResponseDTO generarEtiquetasJSON(Integer idPedido);

    /**
     * Genera un PDF con las etiquetas de un pedido organizadas en cuadrícula
     * 
     * Formato del PDF:
     * - 2 etiquetas por fila (cuadrícula)
     * - Cada etiqueta contiene:
     * * Logo/Título "SAFE ROUTE"
     * * Cliente: {Nombre Completo}
     * * Dirección: {Dirección de entrega}
     * * Contacto: {Teléfono}
     * - Bordes y separadores para facilitar el corte
     * - Número de etiqueta y total (ej: 1/5, 2/5, etc.)
     * 
     * @param idPedido ID del pedido entregado
     * @return ByteArrayOutputStream con el PDF generado
     * @throws Exception si hay error en la generación del PDF
     */
    ByteArrayOutputStream generarEtiquetasPDF(Integer idPedido) throws Exception;
}
