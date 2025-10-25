package com.saferoute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistroRequest {
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden exceder 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Los nombres solo pueden contener letras y espacios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Los apellidos solo pueden contener letras y espacios")
    private String apellidos;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe contener exactamente 10 dígitos numéricos")
    private String telefono;

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe contener exactamente 10 dígitos numéricos")
    private String cedula;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 150, message = "La dirección no puede exceder 150 caracteres")
    private String direccion;

    @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres")
    private String contrasenia;

    public RegistroRequest() {
    }

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

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
}