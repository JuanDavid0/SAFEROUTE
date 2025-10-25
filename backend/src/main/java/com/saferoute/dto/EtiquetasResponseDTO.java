package com.saferoute.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtiquetasResponseDTO {

    private Integer idPedido;
    private String fechaEntrega;
    private List<EtiquetaDTO> etiquetas;
    private Integer totalEtiquetas;
}
