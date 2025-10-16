package com.saferoute.dto;

import java.util.List;

public class SolicitudModificacionDTO {
    private String direccionEntrega; // Nuevo nombre más claro
    private String nuevaDireccion; // Mantener compatibilidad
    private Integer nuevaCantidad; // Para compatibilidad con solicitudes de 1 producto
    private List<ProductoModificacionDTO> productos; // Para múltiples productos

    public String getDireccionEntrega() {
        // Priorizar direccionEntrega, sino usar nuevaDireccion
        return direccionEntrega != null ? direccionEntrega : nuevaDireccion;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

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

    public List<ProductoModificacionDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoModificacionDTO> productos) {
        this.productos = productos;
    }

    public static class ProductoModificacionDTO {
        private Integer idProducto;
        private Integer cantidadSolicitada;

        public Integer getIdProducto() {
            return idProducto;
        }

        public void setIdProducto(Integer idProducto) {
            this.idProducto = idProducto;
        }

        public Integer getCantidadSolicitada() {
            return cantidadSolicitada;
        }

        public void setCantidadSolicitada(Integer cantidadSolicitada) {
            this.cantidadSolicitada = cantidadSolicitada;
        }
    }
}
