package com.saferoute.dto;

import java.math.BigDecimal;

public class SolicitudProductoDTO {
    private Integer idProducto;
    private Integer cantidadSolicitada;
    private BigDecimal precio;
    private Integer modificacionesRestantes;

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(Integer cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getModificacionesRestantes() {
        return modificacionesRestantes;
    }

    public void setModificacionesRestantes(Integer modificacionesRestantes) {
        this.modificacionesRestantes = modificacionesRestantes;
    }
}
