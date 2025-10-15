package com.saferoute.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SolicitudProductoId implements Serializable {
    private Integer idSolicitud;
    private Integer idProducto;

    // Getters, setters, equals, hashCode
    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SolicitudProductoId that = (SolicitudProductoId) o;
        return Objects.equals(idSolicitud, that.idSolicitud) && Objects.equals(idProducto, that.idProducto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSolicitud, idProducto);
    }
}
