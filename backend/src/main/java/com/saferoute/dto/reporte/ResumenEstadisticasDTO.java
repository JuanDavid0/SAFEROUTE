package com.saferoute.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para resumen general de estadísticas
 * Dashboard principal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenEstadisticasDTO {

    // Pedidos
    private Integer totalPedidos;
    private Integer pedidosActivos;
    private Integer pedidosEnCurso;
    private Integer pedidosEntregados;
    private Integer pedidosCancelados;

    // Solicitudes
    private Integer totalSolicitudes;
    private Integer solicitudesPendientes;
    private Integer solicitudesConfirmadas;
    private Integer solicitudesPagadas;
    private Integer solicitudesRechazadas;

    // Financiero
    private BigDecimal ingresosTotales;
    private BigDecimal costosTotales;
    private BigDecimal gananciaNeta;

    // Clientes
    private Integer totalClientes;
    private Integer clientesActivos; // Con al menos una solicitud

    // Productos
    private Integer totalProductos;
    private Integer productosActivos; // En pedidos activos
}
