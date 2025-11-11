package com.saferoute.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para almacenar tokens OTP (One-Time Password) para autenticación de
 * clientes
 * Los tokens tienen una duración limitada y número máximo de intentos
 */
@Entity
@Table(name = "otp_token")
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_otp")
    private Integer idOtp;

    @Column(name = "cedula", nullable = false, length = 20)
    private String cedula;

    @Column(name = "telefono", nullable = false, length = 10)
    private String telefono;

    @Column(name = "codigo_otp", nullable = false, length = 6)
    private String codigoOtp;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;

    @Column(name = "verificado", nullable = false)
    private Boolean verificado = false;

    @Column(name = "usado", nullable = false)
    private Boolean usado = false;

    // Constructores
    public OtpToken() {
    }

    public OtpToken(String cedula, String telefono, String codigoOtp, LocalDateTime fechaExpiracion) {
        this.cedula = cedula;
        this.telefono = telefono;
        this.codigoOtp = codigoOtp;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaExpiracion = fechaExpiracion;
        this.intentosFallidos = 0;
        this.verificado = false;
        this.usado = false;
    }

    // Getters y Setters
    public Integer getIdOtp() {
        return idOtp;
    }

    public void setIdOtp(Integer idOtp) {
        this.idOtp = idOtp;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCodigoOtp() {
        return codigoOtp;
    }

    public void setCodigoOtp(String codigoOtp) {
        this.codigoOtp = codigoOtp;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public Integer getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(Integer intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public Boolean getVerificado() {
        return verificado;
    }

    public void setVerificado(Boolean verificado) {
        this.verificado = verificado;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }

    /**
     * Verifica si el token ha expirado
     */
    public boolean haExpirado() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }

    /**
     * Incrementa el contador de intentos fallidos
     */
    public void incrementarIntentosFallidos() {
        this.intentosFallidos++;
    }
}
