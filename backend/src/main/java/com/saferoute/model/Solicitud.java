package com.saferoute.model;

import com.saferoute.model.enums.EstadoSolicitudEnum;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitudEnum estadoSolicitud = EstadoSolicitudEnum.PDP;

    @Column(nullable = false)
    private LocalDate fechaSolicitud = LocalDate.now();

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SolicitudProducto> productos = new HashSet<>();

    @Column(nullable = false)
    private String direccionEntrega;

    @Column(nullable = false)
    private Integer modificacionesRestantes = 2;

    // Getters y setters
    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public EstadoSolicitudEnum getEstadoSolicitud() {
        return estadoSolicitud;
    }

    public void setEstadoSolicitud(EstadoSolicitudEnum estadoSolicitud) {
        this.estadoSolicitud = estadoSolicitud;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public Set<SolicitudProducto> getProductos() {
        return productos;
    }

    public void setProductos(Set<SolicitudProducto> productos) {
        this.productos = productos;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public Integer getModificacionesRestantes() {
        return modificacionesRestantes;
    }

    public void setModificacionesRestantes(Integer modificacionesRestantes) {
        this.modificacionesRestantes = modificacionesRestantes;
    }

}
