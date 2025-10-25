package com.saferoute.service.helper;

import com.saferoute.constants.ReporteConstants;
import com.saferoute.dto.reporte.ReporteIngresosDTO;
import com.saferoute.model.Solicitud;
import com.saferoute.model.SolicitudProducto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Helper para agrupamiento de datos en reportes
 * Extrae lógica de agrupación temporal
 */
@Component
@RequiredArgsConstructor
public class AgrupamientoReporteHelper {

    private final CalculosFinancierosHelper calculosHelper;

    /**
     * Agrupa solicitudes por periodo (trimestral o anual)
     */
    public List<ReporteIngresosDTO.DatosPeriodo> agruparPorPeriodo(
            List<Solicitud> solicitudes, String agrupacion) {

        Map<String, ReporteIngresosDTO.DatosPeriodo> periodosMap = new TreeMap<>();

        for (Solicitud solicitud : solicitudes) {
            String periodo = determinarPeriodo(solicitud, agrupacion);
            ReporteIngresosDTO.DatosPeriodo datos = obtenerOCrearDatosPeriodo(periodosMap, periodo);

            acumularDatosSolicitud(datos, solicitud);
        }

        return new ArrayList<>(periodosMap.values());
    }

    /**
     * Determina el periodo según el tipo de agrupación
     */
    private String determinarPeriodo(Solicitud solicitud, String agrupacion) {
        if (ReporteConstants.AGRUPACION_TRIMESTRAL.equals(agrupacion)) {
            return calcularPeriodoTrimestral(solicitud);
        } else {
            return calcularPeriodoAnual(solicitud);
        }
    }

    /**
     * Calcula periodo trimestral (formato: yyyy-Q1, yyyy-Q2, etc.)
     */
    private String calcularPeriodoTrimestral(Solicitud solicitud) {
        int year = solicitud.getFechaSolicitud().getYear();
        int month = solicitud.getFechaSolicitud().getMonthValue();
        int trimestre = ((month - 1) / 3) + 1;
        return String.format(ReporteConstants.FORMATO_TRIMESTRE, year, trimestre);
    }

    /**
     * Calcula periodo anual (formato: yyyy)
     */
    private String calcularPeriodoAnual(Solicitud solicitud) {
        int year = solicitud.getFechaSolicitud().getYear();
        return String.format(ReporteConstants.FORMATO_ANIO, year);
    }

    /**
     * Obtiene o crea datos de periodo en el mapa
     */
    private ReporteIngresosDTO.DatosPeriodo obtenerOCrearDatosPeriodo(
            Map<String, ReporteIngresosDTO.DatosPeriodo> periodosMap, String periodo) {

        return periodosMap.computeIfAbsent(periodo, k -> crearDatosPeriodoVacio(periodo));
    }

    /**
     * Crea un nuevo objeto DatosPeriodo vacío
     */
    private ReporteIngresosDTO.DatosPeriodo crearDatosPeriodoVacio(String periodo) {
        ReporteIngresosDTO.DatosPeriodo datos = new ReporteIngresosDTO.DatosPeriodo();
        datos.setPeriodo(periodo);
        datos.setIngresos(BigDecimal.ZERO);
        datos.setCostos(BigDecimal.ZERO);
        datos.setGanancia(BigDecimal.ZERO);
        datos.setCantidadSolicitudes(0);
        return datos;
    }

    /**
     * Acumula datos de una solicitud en el periodo
     */
    private void acumularDatosSolicitud(ReporteIngresosDTO.DatosPeriodo datos, Solicitud solicitud) {
        datos.setCantidadSolicitudes(datos.getCantidadSolicitudes() + 1);

        for (SolicitudProducto sp : solicitud.getProductos()) {
            BigDecimal ingresos = calculosHelper.calcularMontoProducto(sp);
            BigDecimal costos = calculosHelper.calcularCostoProducto(sp);

            datos.setIngresos(datos.getIngresos().add(ingresos));
            datos.setCostos(datos.getCostos().add(costos));
        }

        datos.setGanancia(datos.getIngresos().subtract(datos.getCostos()));
    }
}
