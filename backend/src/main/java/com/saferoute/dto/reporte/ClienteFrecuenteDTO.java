package com.saferoute.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para clientes frecuentes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteFrecuenteDTO {

    private String cedula;
    private String nombreCompleto;
    private String telefono;

    private Integer totalSolicitudes;
    private Integer solicitudesPagadas;
    private BigDecimal montoTotalGastado;

    private LocalDate primeraCompra;
    private LocalDate ultimaCompra;

    private BigDecimal promedioGastoPorSolicitud;
}
