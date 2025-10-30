package com.saferoute.dto;

public class JwtResponse {
    private final String token;
    private final String rol;
    private final Integer idUsuario;

    public JwtResponse(String token, String rol, Integer idUsuario) {
        this.token = token;
        this.rol = rol;
        this.idUsuario = idUsuario;
    }

    public String getToken() {
        return token;
    }

    public String getRol() {
        return rol;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }
}