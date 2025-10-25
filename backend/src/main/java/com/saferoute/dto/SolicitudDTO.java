package com.saferoute.dto;

import java.time.LocalDate;
import java.util.List;

public class SolicitudDTO {
    private Integer idSolicitud;
    private Integer idCliente;
    private String nombreCliente;
    private Integer idPedido;
    private String estadoSolicitud;
    private String direccionEntrega;
    private LocalDate fechaSolicitud;
    private LocalDate fechaLimitePago;
    private Integer modificacionesRestantes;
    private List<SolicitudProductoDTO> productos;

    // Getters y setters
    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public String getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public void setEstadoSolicitud(String estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDate getFechaLimitePago() {
        return fechaLimitePago;
    }

    public void setFechaLimitePago(LocalDate fechaLimitePago) {
        this.fechaLimitePago = fechaLimitePago;
    }

    public Integer getModificacionesRestantes() {
        return modificacionesRestantes;
    }

    public void setModificacionesRestantes(Integer modificacionesRestantes) {
        this.modificacionesRestantes = modificacionesRestantes;
    }

    public List<SolicitudProductoDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<SolicitudProductoDTO> productos) {
        this.productos = productos;
    }

}
