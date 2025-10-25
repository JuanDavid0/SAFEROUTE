package com.saferoute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 🏷️ DTO PARA RESPUESTA DE ETIQUETAS
 * 
 * Contiene la colección completa de etiquetas de un pedido
 * junto con información general del pedido.
 * 
 * @author SafeRoute Team
 * @version 1.0
 * @since 2025-10-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtiquetasResponseDTO {

    /**
     * ID del pedido
     */
    private Integer idPedido;

    /**
     * Fecha de entrega del pedido
     */
    private String fechaEntrega;

    /**
     * Lista de etiquetas generadas
     */
    private List<EtiquetaDTO> etiquetas;

    /**
     * Total de etiquetas generadas
     */
    private Integer totalEtiquetas;
}
