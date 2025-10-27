package com.saferoute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  DTO PARA ETIQUETAS DE PEDIDO
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
