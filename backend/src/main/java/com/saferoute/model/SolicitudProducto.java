package com.saferoute.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "solicitud_producto")
public class SolicitudProducto {

    @EmbeddedId
    private SolicitudProductoId id = new SolicitudProductoId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idSolicitud")
    @JoinColumn(name = "id_solicitud")
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProducto")
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidadSolicitada;

    @Column(nullable = false)
    private BigDecimal precio;

    // Getters y setters
    public SolicitudProductoId getId() {
        return id;
    }

    public void setId(SolicitudProductoId id) {
        this.id = id;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Solicitud solicitud) {
        this.solicitud = solicitud;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
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

}
