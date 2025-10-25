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

    private Integer idSolicitud;
    private Integer idPedido;
    private String nombreCliente;
    private String direccion;
    private String telefono;
    private Integer numeroEtiqueta;
    private Integer totalEtiquetas;
}
