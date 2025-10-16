package com.saferoute.dto;

public class JwtResponse {
    private final String token;
    private final String rol;

    public JwtResponse(String token, String rol) {
        this.token = token;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public String getRol() {
        return rol;
    }
}