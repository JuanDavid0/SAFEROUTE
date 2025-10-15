package com.saferoute.dto;

public class ProductoPedidoDTO {
    private Integer idProducto;
    private Integer cantidadMin;
    private Integer cantidadMax;

    // Constructores
    public ProductoPedidoDTO() {
    }

    public ProductoPedidoDTO(Integer idProducto, Integer cantidadMin, Integer cantidadMax) {
        this.idProducto = idProducto;
        this.cantidadMin = cantidadMin;
        this.cantidadMax = cantidadMax;
    }

    // Getters y Setters
    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidadMin() {
        return cantidadMin;
    }

    public void setCantidadMin(Integer cantidadMin) {
        this.cantidadMin = cantidadMin;
    }

    public Integer getCantidadMax() {
        return cantidadMax;
    }

    public void setCantidadMax(Integer cantidadMax) {
        this.cantidadMax = cantidadMax;
    }
}
