package com.saferoute.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UsuarioRolId implements Serializable {

    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    public UsuarioRolId() {}

    public UsuarioRolId(Integer idRol, Integer idUsuario) {
        this.idRol = idRol;
        this.idUsuario = idUsuario;
    }

    public Integer getIdRol() { return idRol; }
    public Integer getIdUsuario() { return idUsuario; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioRolId that)) return false;
        return Objects.equals(idRol, that.idRol) && Objects.equals(idUsuario, that.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRol, idUsuario);
    }
}