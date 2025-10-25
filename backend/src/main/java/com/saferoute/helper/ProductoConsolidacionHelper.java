package com.saferoute.helper;

import com.saferoute.model.Solicitud;
import com.saferoute.model.SolicitudProducto;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Helper para consolidar productos en informes Excel.
 * Agrupa productos del mismo tipo y suma cantidades.
 */
@Component
public class ProductoConsolidacionHelper {

    /**
     * Consolida los productos de múltiples solicitudes.
     * Agrupa por ID de producto y suma cantidades.
     *
     * @param solicitudes Lista de solicitudes a consolidar
     * @return Mapa de productos consolidados ordenado por código
     */
    public List<ProductoConsolidado> consolidarProductos(List<Solicitud> solicitudes) {
        Map<Integer, ProductoConsolidado> productosMap = new HashMap<>();

        for (Solicitud solicitud : solicitudes) {
            for (SolicitudProducto sp : solicitud.getProductos()) {
                Integer idProducto = sp.getProducto().getIdProducto();
                ProductoConsolidado consolidado = productosMap.computeIfAbsent(
                        idProducto,
                        id -> crearProductoConsolidado(sp, solicitud.getFechaSolicitud()));

                acumularDatosProducto(consolidado, sp, solicitud.getFechaSolicitud());
            }
        }

        return ordenarPorCodigo(productosMap.values());
    }

    /**
     * Crea un nuevo producto consolidado a partir de un SolicitudProducto.
     */
    private ProductoConsolidado crearProductoConsolidado(SolicitudProducto sp, LocalDate fecha) {
        ProductoConsolidado consolidado = new ProductoConsolidado();
        consolidado.setCodigoProducto(sp.getProducto().getIdProducto());
        consolidado.setNombreProducto(sp.getProducto().getNombreProducto());
        consolidado.setPrecio(sp.getPrecio());
        consolidado.setCantidad(0);
        consolidado.setFechaPrimera(fecha);
        return consolidado;
    }

    /**
     * Acumula los datos de un producto en el consolidado.
     */
    private void acumularDatosProducto(ProductoConsolidado consolidado,
            SolicitudProducto sp,
            LocalDate fechaSolicitud) {
        consolidado.setCantidad(consolidado.getCantidad() + sp.getCantidadSolicitada());

        // Actualizar fecha si es más reciente
        if (fechaSolicitud.isAfter(consolidado.getFechaPrimera())) {
            consolidado.setFechaPrimera(fechaSolicitud);
        }
    }

    /**
     * Ordena los productos consolidados por código.
     */
    private List<ProductoConsolidado> ordenarPorCodigo(Collection<ProductoConsolidado> productos) {
        return productos.stream()
                .sorted(Comparator.comparing(ProductoConsolidado::getCodigoProducto))
                .toList();
    }

    /**
     * Clase interna para representar un producto consolidado.
     */
    @Getter
    public static class ProductoConsolidado {
        private Integer codigoProducto;
        private String nombreProducto;
        private int cantidad;
        private BigDecimal precio;
        private LocalDate fechaPrimera;

        public void setCodigoProducto(Integer codigoProducto) {
            this.codigoProducto = codigoProducto;
        }

        public void setNombreProducto(String nombreProducto) {
            this.nombreProducto = nombreProducto;
        }

        public void setCantidad(int cantidad) {
            this.cantidad = cantidad;
        }

        public void setPrecio(BigDecimal precio) {
            this.precio = precio;
        }

        public void setFechaPrimera(LocalDate fechaPrimera) {
            this.fechaPrimera = fechaPrimera;
        }

        public BigDecimal calcularPrecioTotal() {
            return precio.multiply(BigDecimal.valueOf(cantidad));
        }
    }
}
