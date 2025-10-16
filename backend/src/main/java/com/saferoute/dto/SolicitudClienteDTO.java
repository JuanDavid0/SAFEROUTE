package com.saferoute.dto;

import java.util.List;

public class SolicitudClienteDTO {
    // Datos personales
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String cedula;
    private String direccion;

    // Datos de la solicitud
    private Integer idPedido;
    private List<SolicitudProductoDTO> productos;

    // Getters y setters

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public List<SolicitudProductoDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<SolicitudProductoDTO> productos) {
        this.productos = productos;
    }
}
