package com.saferoute.service.helper;

import com.saferoute.model.Solicitud;
import com.saferoute.model.SolicitudProducto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Helper para cálculos financieros en Reportes
 * Aplica el principio DRY (Don't Repeat Yourself)
 */
@Component
public class CalculosFinancierosHelper {

    /**
     * Calcula el monto total de una solicitud (precio * cantidad)
     */
    public BigDecimal calcularMontoSolicitud(Solicitud solicitud) {
        return solicitud.getProductos().stream()
                .map(this::calcularMontoProducto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el monto de un producto (precio * cantidad)
     */
    public BigDecimal calcularMontoProducto(SolicitudProducto sp) {
        return sp.getPrecio().multiply(new BigDecimal(sp.getCantidadSolicitada()));
    }

    /**
     * Calcula el costo de un producto (costoUnitario * cantidad)
     */
    public BigDecimal calcularCostoProducto(SolicitudProducto sp) {
        return sp.getProducto().getCostoUnitario()
                .multiply(new BigDecimal(sp.getCantidadSolicitada()));
    }

    /**
     * Calcula ingresos totales de una solicitud
     */
    public BigDecimal calcularIngresosSolicitud(Solicitud solicitud) {
        return calcularMontoSolicitud(solicitud);
    }

    /**
     * Calcula costos totales de una solicitud
     */
    public BigDecimal calcularCostosSolicitud(Solicitud solicitud) {
        return solicitud.getProductos().stream()
                .map(this::calcularCostoProducto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula ganancia de una solicitud (ingresos - costos)
     */
    public BigDecimal calcularGananciaSolicitud(Solicitud solicitud) {
        BigDecimal ingresos = calcularIngresosSolicitud(solicitud);
        BigDecimal costos = calcularCostosSolicitud(solicitud);
        return ingresos.subtract(costos);
    }
}
