package com.saferoute.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para pedidos en curso
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEnCursoDTO {

    private Integer idPedido;
    private String estadoPedido;
    private String estadoPedidoTraducido;

    private LocalDate fechaCreacion;
    private LocalDate fechaCierre;
    private Integer diasTranscurridos;

    private Integer totalSolicitudes;
    private Integer solicitudesPendientes;
    private Integer solicitudesConfirmadas;
    private Integer solicitudesPagadas;

    private BigDecimal montoTotalEstimado;
    private BigDecimal montoPagado;

    private Integer progresoPercent; // 0-100
}
