package com.saferoute.dto;

import java.math.BigDecimal;
import java.util.List;

public class ConsolidacionDTO {
    private Integer idPedido;
    private String nuevoEstado;
    private Integer totalSolicitudes;
    private BigDecimal montoTotal;
    private List<ConsolidacionProductoDTO> productos;
    private String mensaje;
    
    // Getters y setters
    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }
    public String getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(String nuevoEstado) { this.nuevoEstado = nuevoEstado; }
    public Integer getTotalSolicitudes() { return totalSolicitudes; }
    public void setTotalSolicitudes(Integer totalSolicitudes) { this.totalSolicitudes = totalSolicitudes; }
    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public List<ConsolidacionProductoDTO> getProductos() { return productos; }
    public void setProductos(List<ConsolidacionProductoDTO> productos) { this.productos = productos; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}