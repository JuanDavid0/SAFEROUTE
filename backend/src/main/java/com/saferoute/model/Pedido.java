package com.saferoute.model;

import com.saferoute.model.enums.EstadoPedidoEnum;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin", nullable = false)
    private Usuario admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedidoEnum estadoPedido = EstadoPedidoEnum.CRT;

    @Column(nullable = false)
    private LocalDate fechaCreado = LocalDate.now();

    private LocalDate fechaCierre;

    @Column(unique = true, length = 64)
    private String urlHash;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductoPedido> productos = new HashSet<>();

    // Getters y setters
    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public Usuario getAdmin() {
        return admin;
    }

    public void setAdmin(Usuario admin) {
        this.admin = admin;
    }

    public EstadoPedidoEnum getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(EstadoPedidoEnum estadoPedido) {
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

    public Set<ProductoPedido> getProductos() {
        return productos;
    }

    public void setProductos(Set<ProductoPedido> productos) {
        this.productos = productos;
    }

    public String getUrlHash() {
        return urlHash;
    }

    public void setUrlHash(String urlHash) {
        this.urlHash = urlHash;
    }

}
