package com.saferoute.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductoDTO {
    private Integer idProducto;

    @NotBlank
    private String nombreProducto;

    @NotBlank
    private String tipoProducto;

    @NotBlank
    private String descripcionProducto;

    @DecimalMin(value = "0.01")
    private BigDecimal precioUnitario;

    @DecimalMin(value = "0.00")
    private BigDecimal costoUnitario;

    // Getters y setters
    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public String getDescripcionProducto() {
        return descripcionProducto;
    }

    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }
}