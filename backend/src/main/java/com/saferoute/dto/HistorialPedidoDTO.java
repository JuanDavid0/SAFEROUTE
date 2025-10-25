package com.saferoute.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class HistorialPedidoDTO {
    private Integer idPedido;
    private Integer idAdmin;
    private String estadoPedido;
    private LocalDate fechaCreado;
    private LocalDate fechaCierre;
    private List<SolicitudDTO> solicitudes;
    private Integer totalSolicitudes;
    private BigDecimal precioTotal;

    // Getters y setters
    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public Integer getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(Integer idAdmin) {
        this.idAdmin = idAdmin;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(String estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public LocalDate getFechaCreado() {
        return fechaCreado;
    }

    public void setFechaCreado(LocalDate fechaCreado) {
        this.fechaCreado = fechaCreado;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public List<SolicitudDTO> getSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(List<SolicitudDTO> solicitudes) {
        this.solicitudes = solicitudes;
    }

    public Integer getTotalSolicitudes() {
        return totalSolicitudes;
    }

    public void setTotalSolicitudes(Integer totalSolicitudes) {
        this.totalSolicitudes = totalSolicitudes;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(BigDecimal precioTotal) {
        this.precioTotal = precioTotal;
    }
}