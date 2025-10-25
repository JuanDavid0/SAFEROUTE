package com.saferoute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🏷️ DTO PARA ETIQUETAS DE PEDIDO
 * 
 * Representa la información de una etiqueta individual de entrega.
 * Se genera una etiqueta por cada solicitud de un pedido entregado.
 * 
 * Contiene la información esencial para la entrega:
 * - Identificador de la solicitud
 * - Datos del cliente (nombre, dirección, contacto)
 * - Número de pedido
 * 
 * @author SafeRoute Team
 * @version 1.0
 * @since 2025-10-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtiquetaDTO {

    /**
     * ID de la solicitud asociada a esta etiqueta
     */
    private Integer idSolicitud;

    /**
     * ID del pedido
     */
    private Integer idPedido;

    /**
     * Nombre completo del cliente
     */
    private String nombreCliente;

    /**
     * Dirección de entrega
     */
    private String direccion;

    /**
     * Número de teléfono de contacto
     */
    private String telefono;

    /**
     * Número de etiqueta (orden secuencial)
     */
    private Integer numeroEtiqueta;

    /**
     * Total de etiquetas del pedido
     */
    private Integer totalEtiquetas;
}
