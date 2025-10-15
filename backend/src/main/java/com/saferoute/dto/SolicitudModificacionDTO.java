package com.saferoute.dto;

public class SolicitudModificacionDTO {
    private String nuevaDireccion;
    private Integer nuevaCantidad;

    public String getNuevaDireccion() {
        return nuevaDireccion;
    }

    public void setNuevaDireccion(String nuevaDireccion) {
        this.nuevaDireccion = nuevaDireccion;
    }

    public Integer getNuevaCantidad() {
        return nuevaCantidad;
    }

    public void setNuevaCantidad(Integer nuevaCantidad) {
        this.nuevaCantidad = nuevaCantidad;
    }
}
