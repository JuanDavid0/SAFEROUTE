package com.saferoute.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario_rol")
public class UsuarioRol {

    @EmbeddedId
    private UsuarioRolId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idRol")
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    public UsuarioRol() {}

    public UsuarioRol(Rol rol, Usuario usuario) {
        this.rol = rol;
        this.usuario = usuario;
        this.id = new UsuarioRolId(
            rol != null ? rol.getIdRol() : null,
            usuario != null ? usuario.getIdUsuario() : null
        );
    }

    public UsuarioRolId getId() { return id; }
    public void setId(UsuarioRolId id) { this.id = id; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}