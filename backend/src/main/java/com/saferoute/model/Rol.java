package com.saferoute.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "tipo_rol", length = 3, nullable = false, unique = true)
    private String tipoRol;

    @Column(name = "descripcion_rol", length = 150)
    private String descripcionRol;

    public Rol() {}

    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
    public String getTipoRol() { return tipoRol; }
    public void setTipoRol(String tipoRol) { this.tipoRol = tipoRol; }
    public String getDescripcionRol() { return descripcionRol; }
    public void setDescripcionRol(String descripcionRol) { this.descripcionRol = descripcionRol; }
}