package com.saferoute.model;

import jakarta.persistence.*;

@Entity
@Table(name = "producto_pedido")
public class ProductoPedido {

    @EmbeddedId
    private ProductoPedidoId id = new ProductoPedidoId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPedido")
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProducto")
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidadMin;

    @Column
    private Integer cantidadMax;

    // Getters y setters

    public ProductoPedidoId getId() {
        return id;
    }

    public void setId(ProductoPedidoId id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
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
