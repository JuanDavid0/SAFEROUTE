package com.saferoute.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para productos más vendidos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoVendidoDTO {

    private Integer idProducto;
    private String nombreProducto;
    private String descripcion;
    private String categoria;

    private Integer cantidadTotalVendida;
    private BigDecimal ingresosGenerados;
    private BigDecimal costosAsociados;
    private BigDecimal gananciaNeta;

    private Integer numeroPedidos; // En cuántos pedidos aparece
}
